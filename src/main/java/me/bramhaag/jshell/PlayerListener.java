package me.bramhaag.jshell;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

    private Main plugin;

    public PlayerListener(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        JShellWrapper shell = JShellManager.getInstance().getShell(e.getPlayer().getUniqueId());

        if(shell == null) {
            return;
        }

        e.setCancelled(true);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> executeEvaluation(e.getPlayer(), e.getMessage()));
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        JShellWrapper shell = JShellManager.getInstance().getShell(e.getPlayer().getUniqueId());

        if(shell == null || !shell.isActive()) {
            return;
        }

        e.setCancelled(true);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> executeEvaluation(e.getPlayer(), e.getMessage()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        JShellManager.getInstance().removeShell(e.getPlayer().getUniqueId());
    }

    private void executeEvaluation(@NotNull Player player, @NotNull String message) {
        JShellWrapper shell = JShellManager.getInstance().getShell(player.getUniqueId());

        if(shell == null) {
            return;
        }

        while (shell.isEmpty()) {
            try {
                synchronized (shell.isEmpty()) {
                    shell.isEmpty().wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if(!shell.isActive()) {
            return;
        }

        shell.eval(message);
    }
}
