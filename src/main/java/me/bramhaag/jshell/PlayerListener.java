package me.bramhaag.jshell;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        JShellWrapper shell = JShellManager.getInstance().getShell(e.getPlayer().getUniqueId());

        if(shell != null && shell.isActive()) {
            shell.eval(e.getMessage());
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        JShellManager.getInstance().removeShell(e.getPlayer().getUniqueId());
    }
}
