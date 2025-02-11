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

    public void showScoreboard(final int time, @NonNull String startEnemy) {
        if (scoreboardManager.hasCustomScoreboard(tabPlayer)) return;
        enemies.add(startEnemy);
        final Scoreboard scoreboard = scoreboardManager.createScoreboard(
                player.getName(), config.c("scoreboard.title"), buildEnemies(time)
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
        List<String> lines = new ArrayList<>(new ArrayList<>(config.cl("scoreboard.lines")).stream().map(
                line -> line.replace("{seconds}", String.valueOf(time))
                        .replace("{player}", player.getName())
                        .replace("{ping}", String.valueOf(player.getPing()))
        ).toList());

        int enemiesIndex = lines.indexOf("{enemies}");
        if (enemiesIndex == -1) return lines;
        if (enemies.isEmpty()) {
            lines.remove(enemiesIndex);
            lines.add(enemiesIndex, config.c("enemiesFormat.empty"));
            return lines;
        }
        final ArrayList<String> enemiesList = getSortedEnemyList();
        lines.remove(enemiesIndex);
        lines.addAll(enemiesIndex, enemiesList);
        return lines;
    }

    private @NonNull ArrayList<String> getSortedEnemyList() {
        final String[] enemies = this.enemies.toArray(new String[0]);
        final ArrayList<String> enemiesLines = new ArrayList<>();
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
            return enemiesLines;
        }
        for (int i = 0; i < this.enemies.size(); i++) {
            final Player p = Bukkit.getPlayer(enemies[i]);
            if (p == null) continue;
            if (i == this.enemies.size() - 1) {
                enemiesLines.add(config.c("enemiesFormat.one")
                        .replace("{player}", p.getName())
                        .replace("{ping}", String.valueOf(p.getPing()))
                        .replace("{health}", String.valueOf((int) p.getHealth()))
                );
                continue;
            }
            enemiesLines.add(config.c("enemiesFormat.next")
                    .replace("{player}", p.getName())
                    .replace("{ping}", String.valueOf(p.getPing()))
                    .replace("{health}", String.valueOf((int) p.getHealth())));
        }
        return enemiesLines;
    }

    private int getIndexOfEnemiesLines(@NonNull List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            final String line = list.get(i);
            if (line == null || !line.contains("{enemies}")) continue;
            return i;
        }
        return -1;
    }
}
