package com.andrei1058.bedwars.teamselector.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.gameplay.TeamAssignEvent;
import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.teamselector.Main;
import com.andrei1058.bedwars.teamselector.teamselector.ArenaPreferences;
import com.andrei1058.bedwars.teamselector.teamselector.TeamManager;
import com.andrei1058.bedwars.teamselector.teamselector.TeamSelectorGUI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ArenaListener implements Listener {

    @EventHandler
    public void onBwArenaJoin(PlayerJoinArenaEvent e) {
        if (e.isCancelled()) return;
        if (e.isSpectator()) return;
        if (e.getArena() == null) return;
        if (e.getArena().getStatus() == GameState.waiting || e.getArena().getStatus() == GameState.starting) {
            Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
                if (e.getArena().isPlayer(e.getPlayer()) || e.getArena().getStatus() != GameState.playing){
                    TeamSelectorGUI.giveItem(e.getPlayer(), null);
                }
            }, 30L);
        }
    }

    @EventHandler
    //Remove player from team
    public void onBwArenaLeave(PlayerLeaveArenaEvent e) {
        IArena a = e.getArena();
        if (a.getStatus() == GameState.waiting || a.getStatus() == GameState.starting){
            TeamManager.getInstance().onQuit(a, e.getPlayer());
        }
    }

    @EventHandler
    public void onAssign(TeamAssignEvent e) {
        if (e.isCancelled()) return;

        ITeam team = TeamManager.getInstance().getPlayerTeam(e.getPlayer(), e.getArena());
        if (team != null && team.getMembers().size() >= e.getArena().getMaxInTeam()){
            e.setCancelled(true);
            team.addPlayers(e.getPlayer());
        }
    }

    @EventHandler
    public void onStatusChange(GameStateChangeEvent e) {
        if (e.getNewState() == GameState.starting) {

            ArenaPreferences pref = TeamManager.getInstance().getArena(e.getArena());
            if (pref == null) return;

            // do not start with a single team
            int size = e.getArena().getPlayers().size();
            int teams = pref.getTeamsCount();
            int members = pref.getMembersCount();
            if (size - members <= 0 && teams == 1) {
                e.getArena().setStatus(GameState.waiting);
            }
        }
        if (e.getNewState() == GameState.playing || e.getNewState() == GameState.restarting){
            TeamManager.getInstance().clearArenaCache(e.getArena());
        }
    }
}
