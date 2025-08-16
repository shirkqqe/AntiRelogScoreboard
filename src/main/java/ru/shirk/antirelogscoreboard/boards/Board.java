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

import java.util.*;
import java.util.stream.Collectors;

public class Board {

    @Getter
    private final @NonNull Player player;
    private final @NonNull TabPlayer tabPlayer;
    private final @NonNull ScoreboardManager scoreboardManager;
    private final @NonNull Configuration config = AntiRelogScoreboard.getConfigurationManager().getConfig("settings.yml");
    private final @NonNull HashSet<String> enemies = new HashSet<>();

    public Board(@NonNull Player player) {
        this.scoreboardManager = Objects.requireNonNull(TabAPI.getInstance().getScoreboardManager());
        this.player = player;
        this.tabPlayer = Objects.requireNonNull(TabAPI.getInstance().getPlayer(player.getUniqueId()));
    }

    public void showScoreboard(final int time, @NonNull String startEnemy) {
        if (scoreboardManager.hasCustomScoreboard(tabPlayer)) return;
        enemies.add(startEnemy);
        final Scoreboard scoreboard = scoreboardManager.createScoreboard(
                player.getName(), config.c("scoreboard.title"), buildEnemies(time)
        );
        scoreboardManager.showScoreboard(tabPlayer, scoreboard);
    }

    public void updateScoreboard(int time) {
        final Scoreboard scoreboard = scoreboardManager.createScoreboard(
                player.getName(),
                config.c("scoreboard.title"),
                buildEnemies(time)
        );
        try {
            scoreboardManager.showScoreboard(tabPlayer, scoreboard);
        } catch (Exception ignored) {}
    }

    public void resetScoreboard() {
        Bukkit.getScheduler().runTaskLater(AntiRelogScoreboard.getInstance(),
                () -> scoreboardManager.resetScoreboard(tabPlayer), 10L);
    }

    public void addEnemy(@NonNull String name) {
        enemies.add(name);
    }

    public void removeEnemy(@NonNull String name) {
        enemies.remove(name);
    }

    public @NonNull List<String> buildEnemies(final int time) {
        List<String> lines = config.cl("scoreboard.lines").stream()
                .map(line -> line.replace("{seconds}", String.valueOf(time))
                        .replace("{player}", player.getName())
                        .replace("{ping}", String.valueOf(player.getPing())))
                .collect(Collectors.toCollection(ArrayList::new));

        int enemiesIndex = lines.indexOf("{enemies}");
        if (enemiesIndex == -1) return lines;

        if (enemies.isEmpty()) {
            List<Integer> indexes = config.getFile().getIntegerList("scoreboard.removingLinesIfNoEnemies");
            if (indexes.isEmpty()) {
                lines.set(enemiesIndex, config.c("enemiesFormat.empty"));
                return lines;
            }
            indexes.stream().filter(index -> index >= 0).sorted(Collections.reverseOrder()).forEach(lines::remove);
            return lines;
        }

        final List<String> enemiesList = getSortedEnemyList();
        lines.remove(enemiesIndex);
        lines.addAll(enemiesIndex, enemiesList);
        return lines;
    }

    private @NonNull List<String> getSortedEnemyList() {
        final String[] enemies = this.enemies.toArray(new String[0]);
        final List<String> enemiesLines = new ArrayList<>(enemies.length);
        final String oneFormat = config.c("enemiesFormat.one");
        final String nextFormat = config.c("enemiesFormat.next");

        for (int i = 0; i < enemies.length; i++) {
            if (enemies[i] == null) continue;
            Player p = Bukkit.getPlayer(enemies[i]);
            if (p == null) continue;
            enemiesLines.add(((enemies.length == 1 || i == enemies.length - 1) ? oneFormat : nextFormat)
                    .replace("{player}", p.getName())
                    .replace("{ping}", String.valueOf(p.getPing()))
                    .replace("{health}", String.valueOf((int) p.getHealth())));
        }

        return enemiesLines;
    }
}
