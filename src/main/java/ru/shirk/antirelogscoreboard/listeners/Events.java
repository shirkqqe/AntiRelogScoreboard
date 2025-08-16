package ru.shirk.antirelogscoreboard.listeners;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.leymooo.antirelog.event.PvpStartedEvent;
import ru.leymooo.antirelog.event.PvpStoppedEvent;
import ru.leymooo.antirelog.event.PvpTimeUpdateEvent;
import ru.shirk.antirelogscoreboard.AntiRelogScoreboard;
import ru.shirk.antirelogscoreboard.boards.Board;
import ru.shirk.antirelogscoreboard.boards.BoardManager;

import java.util.ConcurrentModificationException;

public class Events implements Listener {
    private final @NonNull AntiRelogScoreboard plugin;
    private final @NonNull BoardManager boardManager;

    public Events(@NonNull AntiRelogScoreboard plugin) {
        this.plugin = plugin;
        this.boardManager = plugin.getBoardManager();
    }

    @EventHandler
    private void onStartPVP(PvpStartedEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String attackerName = event.getAttacker().getName();
            String defenderName = event.getDefender().getName();
            int pvpTime = event.getPvpTime();
            switch (event.getPvpStatus()) {
                case ALL_NOT_IN_PVP -> {
                    boardManager.show(event.getAttacker(), defenderName, pvpTime);
                    boardManager.show(event.getDefender(), attackerName, pvpTime);
                }
                case ATTACKER_IN_PVP -> {
                    Board board = boardManager.getFrom(event.getAttacker());
                    if (board != null) {
                        board.addEnemy(defenderName);
                    }
                    boardManager.show(event.getDefender(), attackerName, pvpTime);
                }
                case DEFENDER_IN_PVP -> {
                    Board board = boardManager.getFrom(event.getDefender());
                    if (board != null) {
                        board.addEnemy(attackerName);
                    }
                    boardManager.show(event.getAttacker(), defenderName, pvpTime);
                }
            }
        });
    }

    @EventHandler
    private void onPVP(PvpTimeUpdateEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Board board = null;

            try {
                board = boardManager.getFrom(event.getPlayer());
            } catch (ConcurrentModificationException e) {
                plugin.getSLF4JLogger().warn("Снова эта ошибка xd (ConcurrentModificationException)");
            }

            if (board == null) return;

            if (event.getDamagedPlayer() != null && !event.getPlayer().equals(event.getDamagedPlayer())) {
                board.addEnemy(event.getDamagedPlayer().getName());
            } else if (event.getDamagedBy() != null && !event.getPlayer().equals(event.getDamagedBy())) {
                board.addEnemy(event.getDamagedBy().getName());
            }

            board.updateScoreboard(event.getNewTime());
        });
    }

    @EventHandler
    private void onStopPVP(PvpStoppedEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boardManager.removeAll(event.getPlayer().getName());
            boardManager.reset(event.getPlayer());
        });
    }
}
