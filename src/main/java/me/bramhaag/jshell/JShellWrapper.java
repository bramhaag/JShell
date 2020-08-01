package me.bramhaag.jshell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JShellWrapper {

  private static final Pattern MESSAGE_SPLIT_PATTERN = Pattern.compile("\\r?\\n");
  private static final Pattern LINE_BREAK_PATTERN = Pattern.compile("\\R");

  private final Player player;
  private final JShell shell;

  private boolean active = false;

  private String codeBuffer = "";

  private JShellWrapper(@NotNull Player player, @NotNull JShell shell) {
    this.player = player;
    this.shell = shell;
  }

  /**
   * Evaluate code
   *
   * @param code code to evaluate
   */
  public void eval(@NotNull String code) {
    player.sendMessage(String
        .format("%s%s> %s%s", Colors.PREFIX_COLOR, codeBuffer.isEmpty() ? "JShell" : "...",
            ChatColor.RESET, code));

    if (code.startsWith("/")) {
      processCommand(code);
      return;
    }

    code = complete(code);
    if (code == null) {
      return;
    }

    List<SnippetEvent> events = shell.eval(code);
    events.forEach(this::processSnippetEvent);
  }

  /**
   * Process commands
   *
   * @param command command to process
   */
  private void processCommand(@NotNull String command) {
    command = command.substring(1);

    switch (command.toLowerCase()) {
      case "exit" -> {
        setActive(false);
        player.sendMessage(Colors.INFO_COLOR + "Exited JShell");
      }
      case "clear" -> {
        setActive(false);
        JShellManager.getInstance().removeShell(player.getUniqueId());
        player.sendMessage(Colors.INFO_COLOR + "Exited and cleared JShell");
      }
      case "variables" -> shell.variables()
          .forEach(v -> player.sendMessage("|  " + v.source().trim()));
      case "imports" -> shell.imports()
          .forEach(i -> player.sendMessage("|  " + i.fullname().trim()));
      default -> player.sendMessage(Colors.ERROR_COLOR + "Command not found");
    }
  }

  /**
   * Complete possibly incomplete code
   *
   * @param code code to complete
   * @return completed code or null when code is too incomplete to complete
   */
  @Nullable
  private String complete(@NotNull String code) {
    switch (shell.sourceCodeAnalysis().analyzeCompletion(code).completeness()) {
      case COMPLETE, UNKNOWN, COMPLETE_WITH_SEMI -> {
        code = String.format("%s %s", codeBuffer, code);
        codeBuffer = "";
        return code;
      }
      case DEFINITELY_INCOMPLETE, CONSIDERED_INCOMPLETE -> {
        codeBuffer += code;
        return null;
      }
      default -> {
        return null;
      }
    }
  }

  /**
   * Check code for errors
   *
   * @param event event to check
   */
  private void processSnippetEvent(SnippetEvent event) {
    if (event.value() == null) {
      player.sendMessage(Colors.ERROR_COLOR + "=> Error");
      shell.diagnostics(event.snippet())
          .forEach(d -> showDiagnostic(event.snippet().source(), d));
      return;
    }

    player.sendMessage(Colors.PREFIX_COLOR + "=> " + ChatColor.RESET + event.value());
  }

  /**
   * Show diagnostics for code. These are used to describe errors that happen during evaluation.
   *
   * @param source code to check
   * @param diag   diagnostic of code
   */
  private void showDiagnostic(String source, Diag diag) {
    // Print error message
    Arrays.stream(MESSAGE_SPLIT_PATTERN.split(diag.getMessage(null)))
        .filter(line -> !line.trim().startsWith("location:"))
        .map(line -> Colors.ERROR_COLOR + line)
        .forEach(player::sendMessage);

    // Determine start and end positions of erroneous source code line
    int startPosition = (int) diag.getStartPosition();
    int endPosition = (int) diag.getEndPosition();

    Matcher lineBreakMatcher = LINE_BREAK_PATTERN.matcher(source);

    int startLinePosition = 0;
    int endLinePosition = -2;

    while (lineBreakMatcher.find(startLinePosition)) {
      endLinePosition = lineBreakMatcher.start();
      if (endLinePosition >= startPosition) {
        break;
      }

      startLinePosition = lineBreakMatcher.end();
    }

    if (endLinePosition < startPosition) {
      endLinePosition = source.length();
    }

    // Print erroneous source code line
    player.sendMessage(Colors.ERROR_COLOR + source.substring(startLinePosition, endLinePosition));

    // Create error indicators (^ for single character errors, ^---^ for multi character errors)
    StringBuilder sb = new StringBuilder();
    int start = startPosition - startLinePosition;
    sb.append(" ".repeat(Math.max(0, start)))
        .append('^');

    boolean multiline = endPosition > endLinePosition;
    int end = (multiline ? endLinePosition : endPosition) - startLinePosition - 1;
    if (end > start) {
      sb.append("-".repeat(Math.max(0, end - (start + 1))))
          .append(multiline ? "-..." : '^');
    }

    // Print error indicators
    player.sendMessage(Colors.ERROR_COLOR + sb.toString());
  }

  /**
   * Check if shell is currently in use
   *
   * @return if shell is in use
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Set shell in use
   *
   * @param active in use
   */
  public void setActive(boolean active) {
    this.active = active;
  }


  /**
   * Builder class for {@link JShellWrapper}
   */
  public static class Builder {

    private final List<String> imports = new ArrayList<>();
    private final List<JShellVariable> variables = new LinkedList<>();

    private final Player player;

    /**
     * @param player player who owns the shell
     */
    public Builder(@NotNull Player player) {
      this.player = player;
    }

    /**
     * Add imports
     *
     * @param imports imports to add
     * @return Builder instance for chaining
     */
    public Builder addImports(@NotNull List<String> imports) {
      this.imports.addAll(imports);

      return this;
    }

    /**
     * Add variables
     *
     * @param variables variables to add
     * @return Builder instance for chaining
     */
    public Builder addVariables(@NotNull List<JShellVariable> variables) {
      this.variables.addAll(variables);

      return this;
    }

    /**
     * Build {@link JShellWrapper} using properties set in other methods from this class
     *
     * @return {@link JShellWrapper} with properties defined in other methods
     */
    @NotNull
    public JShellWrapper build() {
      JShell shell = JShell.builder().build();

      imports.stream()
          .map(i -> "import " + i + ";")
          .forEach(shell::eval);

      variables.stream()
          .map(e -> String.format("%s %s = %s;", e.getType(), e.getName(), e.getValue()))
          .forEach(shell::eval);

      return new JShellWrapper(player, shell);
    }
  }

  @Override
  public String toString() {
    return "JShellWrapper{" +
        "player=" + player +
        ", shell=" + shell +
        ", active=" + active +
        ", codeBuffer='" + codeBuffer + '\'' +
        '}';
  }
}
