package me.bramhaag.jshell;

import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JShellWrapper {

    private Player player;
    private volatile JShell shell;

    private boolean active = false;

    private String codeBuffer = "";

    private JShellWrapper(@NotNull Player player, @Nullable JShell shell) {
        this.player = player;
        this.shell = shell;
    }

    /**
     * Evaluate code
     * @param code code to evaluate
     */
    public void eval(@NotNull String code) {
        player.sendMessage(String.format("%s> %s", "".equals(codeBuffer) ? "jshell" : "...", code));

        if(code.startsWith("/")) {
            processCommand(code);
            return;
        }

        code = complete(code);
        if(code == null) {
            return;
        }

        List<SnippetEvent> events = shell.eval(code);
        events.forEach(this::processSnippetEvent);
    }

    /**
     * Process commands
     * @param command command to process
     */
    private void processCommand(@NotNull String command) {
        command = command.substring(1);

        switch (command.toLowerCase()) {
            case "exit":
                setActive(false);
                player.sendMessage(ChatColor.YELLOW + "Exited JShell");
                break;
            case "variables":
                shell.variables().forEach(v -> player.sendMessage("|  " + v.source().trim()));
                break;
            case "imports":
                shell.imports().forEach(i -> player.sendMessage("|  " + i.fullname().trim()));
                break;
        }
    }

    /**
     * Complete possibly incomplete code
     * @param code code to complete
     * @return completed code or null when code is too incomplete to complete
     */
    @Nullable
    private String complete(@NotNull String code) {
        switch(shell.sourceCodeAnalysis().analyzeCompletion(code).completeness()) {
            case EMPTY:
                return null;
            case DEFINITELY_INCOMPLETE:
            case CONSIDERED_INCOMPLETE:
                codeBuffer += code;
                return null;
            case COMPLETE:
            case UNKNOWN:
            case COMPLETE_WITH_SEMI:
                code = String.format("%s %s", codeBuffer, code);
                codeBuffer = "";
                return code;
            default:
                return null;
        }
    }

    /**
     * Check code for errors
     * @param event event to check
     */
    private void processSnippetEvent(SnippetEvent event) {
        player.sendMessage("=> " + event.value());

        shell.diagnostics(event.snippet())
                .forEach(d -> showDiagnostic(event.snippet().source(), d));
    }

    /**
     * Show diagnostics for code
     * @param source code to check
     * @param diag diagnostic of code
     */
    private void showDiagnostic(String source, Diag diag) {
        Arrays.stream(diag.getMessage(null).split("\\r?\\n"))
                .filter(line -> !line.trim().startsWith("location:"))
                .forEach(player::sendMessage);

        int pstart = (int) diag.getStartPosition();
        int pend = (int) diag.getEndPosition();

        //todo
        Matcher m = Pattern.compile("\\R").matcher(source);

        int pstartl = 0;
        int pendl = -2;

        while (m.find(pstartl)) {
            pendl = m.start();
            if (pendl >= pstart) {
                break;
            }

            pstartl = m.end();
        }

        if (pendl < pstart) {
            pendl = source.length();
        }

        player.sendMessage(source.substring(pstartl, pendl));

        StringBuilder sb = new StringBuilder();
        int start = pstart - pstartl;
        for (int i = 0; i < start; ++i) {
            sb.append(' ');
        }
        sb.append('^');
        boolean multiline = pend > pendl;
        int end = (multiline ? pendl : pend) - pstartl - 1;
        if (end > start) {
            for (int i = start + 1; i < end; ++i) {
                sb.append('-');
            }

            sb.append(multiline ? "-..." : '^');
        }

        player.sendMessage(sb.toString());
    }

    /**
     * Check if shell is currently in use
     * @return if shell is in use
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set shell in use
     * @param active in use
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Check if shell is null, meaning shell is still being created
     * @return if shell is null
     */
    @NotNull
    public Boolean isEmpty() {
        return shell == null;
    }

    /**
     * Builder class for {@link JShellWrapper}
     */
    public static class Builder {
        private List<String> imports = new ArrayList<>();
        private List<JShellVariable> variables = new LinkedList<>();

        private Player player;

        /**
         * @param player player who owns the shell
         */
        public Builder(@NotNull Player player) {
            this.player = player;
        }

        /**
         * Add imports
         * @param imports imports to add
         * @return Builder instance for chaining
         */
        public Builder addImports(@NotNull List<String> imports) {
            this.imports.addAll(imports);

            return this;
        }

        /**
         * Add variables
         * @param variables variables to add
         * @return Builder instance for chaining
         */
        public Builder addVariables(@NotNull List<JShellVariable> variables) {
            this.variables.addAll(variables);

            return this;
        }

        /**
         * Build {@link JShellWrapper} using properties set in other methods from this class
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
                    .peek(System.out::println)
                    .forEach(shell::eval);

            return new JShellWrapper(player, shell);
        }

        /**
         * Create a {@link JShellWrapper} with an empty shell
         * @return {@link JShellWrapper} with an empty shell
         */
        @NotNull
        public JShellWrapper newEmpty() {
            return new JShellWrapper(player, null);
        }
    }
}
