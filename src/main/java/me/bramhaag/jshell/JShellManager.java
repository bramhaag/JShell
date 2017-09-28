package me.bramhaag.jshell;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class JShellManager {

    private HashMap<UUID, JShellWrapper> shells = new HashMap<>();

    /**
     * Private default constructor to make sure the class is never instantiated
     * outside of this class
     */
    private JShellManager() {}

    /**
     * Create a new shell for a player
     * @param player the player
     */
    public JShellWrapper newShell(@NotNull Player player) {
        JShellWrapper shell = new JShellWrapper.Builder(player).build();
        shells.put(player.getUniqueId(), shell);
        return shell;
    }

    /**
     * Get a player's shell
     * @param uuid player's UUID
     * @return player's shell
     */
    @Nullable
    public JShellWrapper getShell(@NotNull UUID uuid) {
        return shells.get(uuid);
    }

    /**
     * Remove a player's shell
     * @param uuid player's UUID
     */
    public void removeShell(@NotNull UUID uuid) {
        shells.remove(uuid);
    }

    private static class JShellManagerHolder {
        private static final JShellManager INSTANCE = new JShellManager();
    }

    /**
     * Get instance of {@link JShellManager}
     * @return instance of {@link JShellManager}
     */
    public static JShellManager getInstance() {
        return JShellManagerHolder.INSTANCE;
    }
}
