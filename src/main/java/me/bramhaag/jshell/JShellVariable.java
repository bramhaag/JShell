package me.bramhaag.jshell;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class JShellVariable {

    private String type;
    private String name;
    private String value;

    public JShellVariable(@NotNull ConfigurationSection section) {
        System.out.println("new! " + section.getName());
        this.type = section.getString("type");
        this.name = section.getName();
        this.value = section.getString("value");
    }

    @NotNull
    public String getType() {
        return type;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getValue() {
        return value;
    }
}
