package me.bramhaag.jshell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JShellManager {

  private final HashMap<UUID, JShellWrapper> shells = new HashMap<>();

  /**
   * Private default constructor to make sure the class is never instantiated outside of this
   * class.
   */
  private JShellManager() {
  }

  /**
   * Create a new shell for a player.
   *
   * @param player the player
   */
  @NotNull
  public JShellWrapper newShell(@NotNull Player player, @Nullable List<String> imports,
      @Nullable List<JShellVariable> variables) {
    JShellWrapper shell = new JShellWrapper.Builder(player)
        .addImports(Objects.requireNonNullElse(imports, new ArrayList<>()))
        .addVariables(Objects.requireNonNullElse(variables, new ArrayList<>()))
        .build();

    this.shells.put(player.getUniqueId(), shell);
    return shell;
  }

  /**
   * Get a player's shell.
   *
   * @param player the player
   * @return player's shell
   */
  @NotNull
  public Optional<JShellWrapper> getShell(@NotNull Player player) {
    return Optional.ofNullable(this.shells.get(player.getUniqueId()));
  }

  /**
   * Remove a player's shell.
   *
   * @param uuid player's UUID
   */
  public void removeShell(@NotNull UUID uuid) {
    this.shells.remove(uuid);
  }

  /**
   * Get instance of {@link JShellManager}.
   *
   * @return instance of {@link JShellManager}
   */
  @NotNull
  public static JShellManager getInstance() {
    return JShellManagerHolder.INSTANCE;
  }

  /**
   * Holder class for {@link JShellManager}'s instance.
   */
  private static class JShellManagerHolder {

    private static final JShellManager INSTANCE = new JShellManager();
  }
}
