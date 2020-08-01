package me.bramhaag.jshell;

import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener implements Listener {

  private final Main plugin;

  public PlayerListener(@NotNull Main plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent e) {
    boolean shouldCancel = handleMessage(e.getPlayer(), e.getMessage());
    e.setCancelled(shouldCancel);
  }

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent e) {
    boolean shouldCancel = handleMessage(e.getPlayer(), e.getMessage());
    e.setCancelled(shouldCancel);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent e) {
    JShellManager.getInstance().removeShell(e.getPlayer().getUniqueId());
  }

  private boolean handleMessage(Player player, String message) {
    Optional<JShellWrapper> shell = JShellManager.getInstance()
        .getShell(player)
        .filter(JShellWrapper::isActive);

    if (shell.isEmpty()) {
      return false;
    }

    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> shell.get().eval(message));
    return true;
  }
}
