package ru.shirk.antirelogscoreboard.boards;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoardManager {

    private final Map<UUID, Board> map = new HashMap<>();

    public void show(final Player player, final String startEnemy, final int time) {
        if (map.containsKey(player.getUniqueId()) || player.hasPermission("antirelog.bypass")) return;
        final Board board = new Board(player);
        map.put(player.getUniqueId(), board);
        board.showScoreboard(time, startEnemy);
    }

    public Board getFrom(final Player player) {
        for (Board board : map.values()) {
            if (board.getPlayer().equals(player)) {
                return board;
            }
        }
        return null;
    }

    public void reset(final Player player) {
        if (getFrom(player) == null) return;
        getFrom(player).resetScoreboard();
        map.remove(player.getUniqueId());
    }

    public void removeAll(final String name) {
        for (Board board : map.values()) {
            board.remove(name);
        }
    }

    public void resetAll() {
        for (Board board : map.values()) {
            board.resetScoreboard();
            map.remove(board.getPlayer().getUniqueId());
        }
    }
}
