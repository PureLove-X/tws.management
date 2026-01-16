package tech.purelove.twsmanagement.extensions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextProcessor {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private TextProcessor() {}

    public static Component parse(String input, Player viewer, Player sender) {
        return parse(input, viewer, sender, null);
    }
    public static Component parse(
            String input,
            Player viewer,
            Player sender,
            Map<String, String> extraPlaceholders
    ) {
        String replaced = replacePlaceholders(input, sender, extraPlaceholders);
        replaced = expandHex(replaced);
        Parser p = new Parser(replaced, viewer, sender);
        return p.parseAll();
    }

    private static String expandHex(String input) {
        Matcher m = Pattern.compile("&#([A-Fa-f0-9]{6})").matcher(input);
        StringBuffer sb = new StringBuffer();

        while (m.find()) {
            String hex = m.group(1);
            StringBuilder legacy = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                legacy.append('&').append(c);
            }
            m.appendReplacement(sb, legacy.toString());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /* =====================
       Parser
       ===================== */

    private static final class Parser {
        private final String s;
        private final Player viewer;
        private final Player sender;
        private int i = 0;

        Parser(String s, Player viewer, Player sender) {
            this.s = s;
            this.viewer = viewer;
            this.sender = sender;
        }

        Component parseAll() {
            return parseUntil(-1);
        }

        private Component parseUntil(int stopDepth) {
            Component out = Component.empty();
            while (i < s.length()) {
                char c = s.charAt(i);

                if (c == '<') {
                    Component tag = tryParseTag();
                    if (tag != null) {
                        out = out.append(tag);
                        continue;
                    }

                    out = out.append(parseColors("<"));
                    i++;
                    continue;
                }

                if (c == '>' && stopDepth >= 0) {

                    return out;
                }

                int start = i;
                while (i < s.length()) {
                    char ch = s.charAt(i);
                    if (ch == '<') break;
                    if (stopDepth >= 0 && ch == '>') break;
                    i++;
                }
                out = out.append(parseColors(s.substring(start, i)));
            }
            return out;
        }
        private String readUntil(char stop) {
            int start = i;
            while (i < s.length() && s.charAt(i) != stop) i++;
            return s.substring(start, i);
        }
        private String readArgUntil(char stop) {
            if (i < s.length() && s.charAt(i) == '"') {
                return readQuoted();
            }
            return readUntil(stop);
        }

        private Component tryParseTag() {
            int start = i;
            if (s.charAt(i) != '<') return null;
            i++;

            String name = readTagName();
            if (name == null || i >= s.length() || s.charAt(i) != ':') {
                i = start;
                return null;
            }
            i++;
            skipWhitespace();

            String left = readArgUntil('|');
            if (left == null) {
                i = start;
                return null;
            }
            skipWhitespace();

            if (i >= s.length() || s.charAt(i) != '|') {
                i = start;
                return null;
            }
            i++;
            skipWhitespace();


            int depth = 1;
            int bodyStart = i;

            while (i < s.length()) {
                char c = s.charAt(i);

                if (c == '<') depth++;
                else if (c == '>') depth--;

                if (depth == 0) {
                    String bodyRaw = s.substring(bodyStart, i);
                    i++;

                    Component body = new Parser(bodyRaw, viewer, sender).parseAll();
                    return applyTag(name, left, body);
                }

                i++;
            }

            i = start;
            return null;
        }


        private Component applyTag(String rawName, String left, Component rightComp) {
            String name = rawName.toLowerCase(Locale.ROOT);

            return switch (name) {
                case "hover" -> {
                    Component hoverComp = new Parser(left, viewer, sender).parseAll();
                    yield rightComp.hoverEvent(HoverEvent.showText(hoverComp));
                }
                case "click" -> {
                    ClickEvent click = left.startsWith("/")
                            ? ClickEvent.runCommand(left)
                            : ClickEvent.suggestCommand(left);
                    yield rightComp.clickEvent(click);
                }
                case "link" -> {
                    yield Component.empty()
                            .append(rightComp)
                            .clickEvent(ClickEvent.openUrl(left))
                            .hoverEvent(HoverEvent.showText(parseColors(left)));
                }

                case "gradient" -> {
                    String[] split = left.split(":");
                    if (split.length != 2) yield rightComp;

                    TextColor start = TextColor.fromHexString(split[0]);
                    TextColor end = TextColor.fromHexString(split[1]);
                    if (start == null || end == null) yield rightComp;

                    yield applyGradient(rightComp, start, end);
                }
                case "viewermode" -> {
                    boolean isSelf = viewer != null && sender != null && viewer.getUniqueId().equals(sender.getUniqueId());

                    String chosen = isSelf ? left : extractPlainText(rightComp);

                    yield new Parser(chosen, viewer, sender).parseAll();
                }

                default -> parseColors("<" + rawName + ":\"" + left + "\"|...>");
            };
        }
        private String extractPlainText(Component component) {
            StringBuilder sb = new StringBuilder();

            if (component instanceof TextComponent tc) {
                sb.append(tc.content());
            }

            for (Component child : component.children()) {
                sb.append(extractPlainText(child));
            }

            return sb.toString();
        }

        private String readTagName() {
            int start = i;
            while (i < s.length()) {
                char c = s.charAt(i);
                if (Character.isLetter(c)) {
                    i++;
                } else {
                    break;
                }
            }
            if (i == start) return null;
            return s.substring(start, i);
        }

        private void skipWhitespace() {
            while (i < s.length()) {
                char c = s.charAt(i);
                if (c == ' ' || c == '\t' || c == '\n' || c == '\r') i++;
                else break;
            }
        }

        private String readQuoted() {
            if (i >= s.length() || s.charAt(i) != '"') return null;
            i++; // consume opening quote

            StringBuilder sb = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i);

                if (c == '\\' && i + 1 < s.length()) {
                    // allow escaping \" and \\ and \n
                    char n = s.charAt(i + 1);
                    if (n == '"' || n == '\\') {
                        sb.append(n);
                        i += 2;
                        continue;
                    }
                    if (n == 'n') {
                        sb.append('\n');
                        i += 2;
                        continue;
                    }
                }

                if (c == '"') {
                    i++;
                    return sb.toString();
                }

                sb.append(c);
                i++;
            }
            return null;
        }
    }

    /* =====================
       Placeholders
       ===================== */

    private static String replacePlaceholders(
            String s,
            Player player,
            Map<String, String> extra
    ) {
        String out = s
                .replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId().toString())
                .replace("%player_display_name%", LEGACY.serialize(player.displayName()));

        if (extra != null) {
            for (var entry : extra.entrySet()) {
                out = out.replace(
                        "%" + entry.getKey() + "%",
                        entry.getValue()
                );
            }
        }

        return out;
    }


    /* =====================
       Color parsing
       ===================== */

    private static Component parseColors(String input) {
        return LEGACY.deserialize(input);
    }


    /* =====================
       Gradient application
       ===================== */

    private static Component applyGradient(Component input, TextColor start, TextColor end) {
        int total = visibleTextLength(input);
        if (total <= 0) return input;

        Index idx = new Index();
        return gradientRebuild(input, start, end, idx, total);
    }

    private static int visibleTextLength(Component c) {
        int len = 0;
        if (c instanceof TextComponent tc) {
            len += tc.content().length();
        }
        for (Component child : c.children()) {
            len += visibleTextLength(child);
        }
        return len;
    }

    private static Component gradientRebuild(Component original, TextColor start, TextColor end, Index idx, int total) {

        Component rebuilt = Component.empty();

        if (original instanceof TextComponent tc) {
            String content = tc.content();

            Component base = Component.text("")
                    .style(tc.style())
                    .clickEvent(tc.clickEvent())
                    .hoverEvent(tc.hoverEvent());

            if (!content.isEmpty()) {
                Component textOut = Component.empty();

                for (int j = 0; j < content.length(); j++) {
                    float t = (total == 1) ? 0f : ((float) idx.value / (float) (total - 1));
                    TextColor col = interpolate(start, end, t);

                    textOut = textOut.append(
                            Component.text(String.valueOf(content.charAt(j)))
                                    .style(tc.style())
                                    .color(col)
                                    .clickEvent(tc.clickEvent())
                                    .hoverEvent(tc.hoverEvent())
                    );

                    idx.value++;
                }

                rebuilt = base.append(textOut);
            } else {
                rebuilt = base;
            }
        } else {
            rebuilt = original.children(List.of());
        }

        if (!original.children().isEmpty()) {
            Component childrenOut = Component.empty();
            for (Component child : original.children()) {
                childrenOut = childrenOut.append(gradientRebuild(child, start, end, idx, total));
            }
            rebuilt = rebuilt.append(childrenOut);
        }

        return rebuilt;
    }


    private static TextColor interpolate(TextColor a, TextColor b, float t) {
        int r = (int) (a.red() + t * (b.red() - a.red()));
        int g = (int) (a.green() + t * (b.green() - a.green()));
        int bl = (int) (a.blue() + t * (b.blue() - a.blue()));
        return TextColor.color(r, g, bl);
    }

    private static final class Index {
        int value = 0;
    }
}
