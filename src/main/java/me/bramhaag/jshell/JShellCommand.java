package me.bramhaag.jshell;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class JShellCommand implements CommandExecutor {

    private Main plugin;

    public JShellCommand(@NotNull Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players!");
            return true;
        }

        Player player = (Player) sender;

        if(player.hasPermission("jshell.execute")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to execute this command!");
            return true;
        }

        JShellWrapper shell = JShellManager.getInstance().getShell(player.getUniqueId());
        if(shell == null) {
            player.sendMessage(ChatColor.GREEN + "JShell session initialized");

            List<String> imports = plugin.getConfig().getStringList("imports");
            List<JShellVariable> variables = plugin.getConfig().getConfigurationSection("variables").getKeys(false).stream()
                    .map(s -> plugin.getConfig().getConfigurationSection("variables." + s))
                    .map(JShellVariable::new)
                    .collect(Collectors.toList());

            shell = JShellManager.getInstance().newShell(player, imports, variables);
        } else if (!shell.isActive()){
            player.sendMessage(ChatColor.GREEN + "JShell session continued");
        } else {
            player.sendMessage(ChatColor.RED + "JShell session is already active!");
            return true;
        }

        shell.setActive(true);

        if(args.length > 0) {
            shell.eval(String.join(" ", args));
            shell.setActive(false);

            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Type " + ChatColor.RED + "/exit" + ChatColor.YELLOW + " to exit JShell");

        return true;
    }
}
