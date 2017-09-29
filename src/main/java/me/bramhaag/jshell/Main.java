package me.bramhaag.jshell;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getCommand("jshell").setExecutor(new JShellCommand(this));

        saveDefaultConfig();
    }
}
