package me.bramhaag.jshell;

import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JShellWrapper {

    private Player player;
    private JShell shell;

    private boolean active = false;

    private String codeBuffer = "";

    private JShellWrapper(Player player, JShell shell) {
        this.player = player;
        this.shell = shell;
    }

    public void eval(@NotNull String code) {
        player.sendMessage(String.format("%s> %s", "".equals(codeBuffer) ? "jshell" : "...", code));

        code = complete(code);
        if(code == null) {
            return;
        }

        List<SnippetEvent> events = shell.eval(code);
        events.forEach(this::handleSnippetEvent);
    }

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

    private void handleSnippetEvent(SnippetEvent event) {
        List<Diag> diagnostics = shell.diagnostics(event.snippet()).collect(Collectors.toList());
        player.sendMessage("=> " + event.value());
        diagnostics.forEach(d -> showDiagnostic(event.snippet().source(), d));
        System.out.println(event.toString());
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public static class Builder {
        private List<String> imports = new ArrayList<>();
        private Map<String, String> variables = new LinkedHashMap<>();

        private Player player;

        public Builder(@NotNull Player player) {
            this.player = player;
        }

        public Builder addImports(@NotNull String... imports) {
            this.imports.addAll(Arrays.asList(imports));

            return this;
        }

        public Builder addVariable(@NotNull String key, @NotNull String value) {
            this.variables.put(key, value);

            return this;
        }

        public JShellWrapper build() {
            JShell shell = JShell.builder().build();

            addImports("java.util.*", "java.io.*", "java.math.*", "java.net.*", "java.util.concurrent.*", "java.util.prefs.*", "java.util.regex.*", "java.util.stream.*");

            imports.stream()
                    .map(i -> "import " + i + ";").forEach(shell::eval);

            variables.entrySet().stream()
                    .map(e -> String.format("%s %s = %s;", e.getValue().getClass().getTypeName(), e.getKey(), e.getValue()))
                    .forEach(shell::eval);

            return new JShellWrapper(player, shell);
        }
    }
}
