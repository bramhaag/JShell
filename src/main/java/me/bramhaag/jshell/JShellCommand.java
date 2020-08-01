package me.bramhaag.jshell;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JShellCommand implements CommandExecutor {

  private final List<String> defaultImports;
  private final List<JShellVariable> defaultVariables;

  public JShellCommand(@NotNull FileConfiguration config) {
    this.defaultImports = config.getStringList("imports");
    this.defaultVariables = config.getConfigurationSection("variables").getKeys(false).stream()
        .map(s -> "variables." + s)
        .map(config::getConfigurationSection)
        .map(JShellVariable::new)
        .collect(Collectors.toList());
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
      @NotNull String label, @NotNull String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("This command can only be executed by players!");
      return true;
    }

    Player player = (Player) sender;

    if (!player.isOp() || !player.hasPermission("jshell.execute")) {
      player.sendMessage(Colors.ERROR_COLOR + "You don't have permission to execute this command!");
      return true;
    }

    JShellWrapper shell = JShellManager.getInstance().getShell(player)
        .orElseGet(
            () -> JShellManager.getInstance().newShell(player, defaultImports, defaultVariables));

    shell.setActive(true);
    player.sendMessage(Colors.SUCCESS_COLOR + "Welcome to JShell -- Version " + System
        .getProperty("java.version"));
    player.sendMessage(Colors.INFO_COLOR + "Type " + ChatColor.RED + "/exit" + Colors.INFO_COLOR
        + " to exit JShell");

    if (args.length > 0) {
      shell.eval(String.join(" ", args));
    }

    return true;
  }
}
