package ru.shirk.antirelogscoreboard.boards;

import lombok.Getter;
import lombok.NonNull;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.shirk.antirelogscoreboard.AntiRelogScoreboard;
import ru.shirk.antirelogscoreboard.configs.Configuration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Board {

    @Getter
    private final Player player;
    private final TabPlayer tabPlayer;
    private final ScoreboardManager scoreboardManager;
    private final Configuration config = AntiRelogScoreboard.getConfigurationManager().getConfig("settings.yml");
    @Getter
    private final Set<String> enemies = new HashSet<>();

    public Board(final Player player) {
        this.scoreboardManager = TabAPI.getInstance().getScoreboardManager();
        if (scoreboardManager == null) {
            throw new IllegalStateException("Enable Scoreboards in TAB plugin.");
        }
        this.player = player;
        this.tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
    }

    public void showScoreboard(final int time, final String startEnemy) {
        if (scoreboardManager.hasCustomScoreboard(tabPlayer)) return;
        enemies.add(startEnemy);
        final Scoreboard scoreboard = scoreboardManager.createScoreboard(
                player.getName(), AntiRelogScoreboard.getConfigurationManager().getConfig("settings.yml")
                        .c("scoreboard.title"), buildEnemies(time)
        );
        scoreboardManager.showScoreboard(tabPlayer, scoreboard);
    }

    public void updateScoreboard(final int time) {
        final Scoreboard scoreboard = scoreboardManager.createScoreboard(
                player.getName(), AntiRelogScoreboard.getConfigurationManager().getConfig("settings.yml")
                        .c("scoreboard.title"), buildEnemies(time)
        );
        scoreboardManager.showScoreboard(tabPlayer, scoreboard);
    }

    public void resetScoreboard() {
        scoreboardManager.resetScoreboard(tabPlayer);
    }

    public void add(@NonNull String name) {
        enemies.add(name);
    }

    public void remove(@NonNull String name) {
        enemies.remove(name);
    }

    public List<String> buildEnemies(final int time) {
        if (enemies.isEmpty()) return List.of(" Пусто");
        final List<String> lines = new ArrayList<>(AntiRelogScoreboard.getConfigurationManager()
                .getConfig("settings.yml").cl("scoreboard.lines"));
        final String[] enemies = this.enemies.toArray(new String[0]);
        final List<String> enemiesLines = new ArrayList<>();
        if (this.enemies.size() == 1) {
            for (String enemy : enemies) {
                final Player p = Bukkit.getPlayer(enemy);
                if (p == null) continue;
                enemiesLines.add(config.c("enemiesFormat.one")
                        .replace("{player}", p.getName())
                        .replace("{ping}", p.getPing() + "")
                        .replace("{health}", String.valueOf((int) p.getHealth()))
                );
            }
        } else {
            for (int i = 0; i < this.enemies.size(); i++) {
                final Player p = Bukkit.getPlayer(enemies[i]);
                if (p == null) continue;
                if (i == this.enemies.size() - 1) {
                    enemiesLines.add(config.c("enemiesFormat.one")
                            .replace("{player}", p.getName())
                            .replace("{ping}", p.getPing() + "")
                            .replace("{health}", String.valueOf((int) p.getHealth()))
                    );
                    continue;
                }
                enemiesLines.add(config.c("enemiesFormat.next").replace("{player}", p.getName())
                        .replace("{ping}", p.getPing() + "").replace("{health}",
                                String.valueOf((int) p.getHealth())));
            }
        }
        return lines.stream().map(line -> line.replace("{seconds}", String.valueOf(time))
                .replace("{enemies}", String.join("\n", enemiesLines))
                .replace("{player}", player.getName())
                .replace("{ping}", String.valueOf(player.getPing()))).collect(Collectors.toList());
    }
}
