package ru.shirk.antirelogscoreboard.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import ru.leymooo.antirelog.event.PvpStartedEvent;
import ru.leymooo.antirelog.event.PvpStoppedEvent;
import ru.leymooo.antirelog.event.PvpTimeUpdateEvent;
import ru.shirk.antirelogscoreboard.AntiRelogScoreboard;
import ru.shirk.antirelogscoreboard.boards.Board;

public class Events implements Listener {
    @EventHandler
    private void onStartPVP(final PvpStartedEvent event) {
        switch (event.getPvpStatus()) {
            case ALL_NOT_IN_PVP -> {
                AntiRelogScoreboard.getBoardManager().show(event.getAttacker(), event.getDefender().getName(),
                        event.getPvpTime());
                AntiRelogScoreboard.getBoardManager().show(event.getDefender(), event.getAttacker().getName(),
                        event.getPvpTime());
            }
            case ATTACKER_IN_PVP -> {
                final Board board = AntiRelogScoreboard.getBoardManager().getFrom(event.getAttacker());
                if (board != null) {
                    board.add(event.getDefender().getName());
                }
                AntiRelogScoreboard.getBoardManager().show(event.getDefender(), event.getAttacker().getName(),
                        event.getPvpTime());
            }
            case DEFENDER_IN_PVP -> {
                final Board board = AntiRelogScoreboard.getBoardManager().getFrom(event.getDefender());
                if (board != null) {
                    AntiRelogScoreboard.getBoardManager().getFrom(event.getDefender()).add(event.getAttacker().getName());
                }
                AntiRelogScoreboard.getBoardManager().show(event.getAttacker(), event.getDefender().getName(),
                        event.getPvpTime());
            }
        }
    }

    @EventHandler
    private void onPVP(final PvpTimeUpdateEvent event) {
        final Board board = AntiRelogScoreboard.getBoardManager().getFrom(event.getPlayer());
        if (board == null) return;
        if (event.getDamagedPlayer() != null && !event.getPlayer().equals(event.getDamagedPlayer())) {
            board.add(event.getDamagedPlayer().getName());
        } else if (event.getDamagedBy() != null && !event.getPlayer().equals(event.getDamagedBy())) {
            board.add(event.getDamagedBy().getName());
        }
        board.updateScoreboard(event.getNewTime());
    }

    @EventHandler
    private void onStopPVP(final PvpStoppedEvent event) {
        AntiRelogScoreboard.getBoardManager().removeAll(event.getPlayer().getName());
        AntiRelogScoreboard.getBoardManager().reset(event.getPlayer());
    }
}
