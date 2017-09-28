package me.bramhaag.jshell;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JShellCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.getLabel().equalsIgnoreCase("jshell")) {
            return true;
        }

        if(!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by players!");
            return true;
        }

        Player player = (Player) sender;

        JShellWrapper shell = JShellManager.getInstance().getShell(player.getUniqueId());
        if(shell == null) {
            player.sendMessage("JShell session initialized");
            shell = JShellManager.getInstance().newShell(player);
        } else {
            player.sendMessage("JShell session continued");
        }

        shell.setActive(true);

        if(args.length > 0) {
            shell.eval(String.join(" ", args));
            shell.setActive(false);
            player.sendMessage("JShell session paused");
        }
        return true;
    }
}
