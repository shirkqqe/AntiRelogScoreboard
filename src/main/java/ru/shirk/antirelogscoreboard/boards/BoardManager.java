package ru.shirk.antirelogscoreboard.boards;

import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BoardManager {

    private final @NonNull BoardsContainer<UUID, Board> map = new BoardsContainer<>();

    public void show(final Player player, final String startEnemy, final int time) {
        if (map.containsKey(player.getUniqueId()) || player.hasPermission("antirelog.bypass")) return;
        final Board board = new Board(player);
        map.put(player.getUniqueId(), board);
        board.showScoreboard(time, startEnemy);
    }

    public @Nullable Board getFrom(final Player player) {
        final Collection<Board> boards = map.cloneValues();
        for (Board board : boards) {
            if (board == null) continue;
            if (!board.getPlayer().equals(player)) continue;
            return board;
        }
        return null;
    }

    public void reset(final Player player) {
        final Board board = getFrom(player);
        if (board == null) return;
        board.resetScoreboard();
        map.remove(player.getUniqueId());
    }

    public void removeAll(final String name) {
        final Collection<Board> boards = map.cloneValues();
        for (Board board : boards) {
            if (board == null) continue;
            board.removeEnemy(name);
            try {
                map.replace(board.getPlayer().getUniqueId(), board);
            } catch (Exception ignored) {
            }
        }
    }

    public void resetAll() {
        final Collection<Board> boards = map.cloneValues();
        for (Board board : boards) {
            if (board == null) continue;
            board.resetScoreboard();
            map.remove(board.getPlayer().getUniqueId());
        }
    }
}
