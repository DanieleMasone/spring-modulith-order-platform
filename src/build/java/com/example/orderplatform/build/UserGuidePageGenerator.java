package com.example.orderplatform.build;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UserGuidePageGenerator {

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern UNORDERED_ITEM = Pattern.compile("^\\*\\s+(.+)$");
    private static final Pattern ORDERED_ITEM = Pattern.compile("^\\d+\\.\\s+(.+)$");

    private final Map<String, Integer> headingIds = new HashMap<>();
    private final StringBuilder html = new StringBuilder();
    private final StringBuilder paragraph = new StringBuilder();
    private boolean inCodeBlock;
    private boolean inUnorderedList;
    private boolean inOrderedList;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: UserGuidePageGenerator <source.md> <target.html>");
        }

        Path source = Path.of(args[0]);
        Path target = Path.of(args[1]);
        String body = new UserGuidePageGenerator().render(Files.readAllLines(source, StandardCharsets.UTF_8));
        Files.createDirectories(target.getParent());
        Files.writeString(target, page(body), StandardCharsets.UTF_8);
    }

    private String render(List<String> lines) {
        for (String line : lines) {
            renderLine(line);
        }
        closeParagraph();
        closeLists();
        if (inCodeBlock) {
            html.append("</code></pre>\n");
        }
        return html.toString();
    }

    private void renderLine(String line) {
        if (line.startsWith("```")) {
            if (inCodeBlock) {
                html.append("</code></pre>\n");
                inCodeBlock = false;
            } else {
                closeParagraph();
                closeLists();
                html.append("<pre><code>");
                inCodeBlock = true;
            }
            return;
        }

        if (inCodeBlock) {
            html.append(escapeHtml(line)).append('\n');
            return;
        }

        if (line.isBlank()) {
            closeParagraph();
            closeLists();
            return;
        }

        Matcher heading = HEADING.matcher(line);
        if (heading.matches()) {
            closeParagraph();
            closeLists();
            int level = heading.group(1).length();
            String text = heading.group(2).trim();
            html.append("<h").append(level).append(" id=\"").append(slug(text)).append("\">")
                    .append(renderInline(text))
                    .append("</h").append(level).append(">\n");
            return;
        }

        Matcher unordered = UNORDERED_ITEM.matcher(line);
        if (unordered.matches()) {
            closeParagraph();
            if (inOrderedList) {
                html.append("</ol>\n");
                inOrderedList = false;
            }
            if (!inUnorderedList) {
                html.append("<ul>\n");
                inUnorderedList = true;
            }
            html.append("<li>").append(renderInline(unordered.group(1).trim())).append("</li>\n");
            return;
        }

        Matcher ordered = ORDERED_ITEM.matcher(line);
        if (ordered.matches()) {
            closeParagraph();
            if (inUnorderedList) {
                html.append("</ul>\n");
                inUnorderedList = false;
            }
            if (!inOrderedList) {
                html.append("<ol>\n");
                inOrderedList = true;
            }
            html.append("<li>").append(renderInline(ordered.group(1).trim())).append("</li>\n");
            return;
        }

        closeLists();
        if (!paragraph.isEmpty()) {
            paragraph.append(' ');
        }
        paragraph.append(line.trim());
    }

    private void closeParagraph() {
        if (!paragraph.isEmpty()) {
            html.append("<p>").append(renderInline(paragraph.toString())).append("</p>\n");
            paragraph.setLength(0);
        }
    }

    private void closeLists() {
        if (inUnorderedList) {
            html.append("</ul>\n");
            inUnorderedList = false;
        }
        if (inOrderedList) {
            html.append("</ol>\n");
            inOrderedList = false;
        }
    }

    private String slug(String text) {
        String base = text.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (base.isBlank()) {
            base = "section";
        }
        int count = headingIds.merge(base, 1, Integer::sum);
        return count == 1 ? base : base + "-" + count;
    }

    private static String renderInline(String text) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < text.length()) {
            char current = text.charAt(index);
            if (current == '`') {
                int end = text.indexOf('`', index + 1);
                if (end > index) {
                    result.append("<code>")
                            .append(escapeHtml(text.substring(index + 1, end)))
                            .append("</code>");
                    index = end + 1;
                    continue;
                }
            }
            if (current == '[') {
                int labelEnd = text.indexOf(']', index + 1);
                int urlStart = labelEnd + 1;
                if (labelEnd > index && urlStart < text.length() && text.charAt(urlStart) == '(') {
                    int urlEnd = text.indexOf(')', urlStart + 1);
                    if (urlEnd > urlStart) {
                        String label = text.substring(index + 1, labelEnd);
                        String url = text.substring(urlStart + 1, urlEnd);
                        result.append("<a href=\"")
                                .append(escapeAttribute(url))
                                .append("\">")
                                .append(escapeHtml(label))
                                .append("</a>");
                        index = urlEnd + 1;
                        continue;
                    }
                }
            }
            result.append(escapeHtml(Character.toString(current)));
            index++;
        }
        return result.toString();
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String escapeAttribute(String text) {
        return escapeHtml(text).replace("\"", "&quot;");
    }

    private static String page(String body) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <meta name="description" content="User Guide for Spring Modulith Order Platform.">
                  <title>User Guide | Spring Modulith Order Platform</title>
                  <style>
                    :root {
                      color-scheme: light dark;
                      --bg: #f6f8fb;
                      --surface: #ffffff;
                      --surface-soft: #eef3f7;
                      --text: #142033;
                      --muted: #56647a;
                      --line: #d7dee9;
                      --accent: #0f766e;
                      --accent-strong: #0b5d56;
                      --code-bg: #eaf0f6;
                      --shadow: 0 18px 42px rgb(20 32 51 / 0.10);
                    }
                    @media (prefers-color-scheme: dark) {
                      :root {
                        --bg: #0e141d;
                        --surface: #151d29;
                        --surface-soft: #101722;
                        --text: #eef3f8;
                        --muted: #aeb8c8;
                        --line: #2b3749;
                        --accent: #2dd4bf;
                        --accent-strong: #5eead4;
                        --code-bg: #0b111a;
                        --shadow: 0 22px 50px rgb(0 0 0 / 0.28);
                      }
                    }
                    *, *::before, *::after { box-sizing: border-box; }
                    html { -webkit-text-size-adjust: 100%; }
                    body {
                      min-width: 320px;
                      margin: 0;
                      background: linear-gradient(180deg, var(--bg) 0%, var(--surface-soft) 100%);
                      color: var(--text);
                      font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                      line-height: 1.65;
                    }
                    a {
                      color: var(--accent);
                      text-decoration-thickness: 0.08em;
                      text-underline-offset: 0.22em;
                    }
                    a:hover { color: var(--accent-strong); }
                    a:focus-visible {
                      outline: 3px solid #fbbf24;
                      outline-offset: 4px;
                      border-radius: 6px;
                    }
                    .topbar {
                      border-bottom: 1px solid var(--line);
                      background: color-mix(in srgb, var(--surface) 92%, transparent);
                    }
                    .nav {
                      width: min(100% - 32px, 1080px);
                      margin: 0 auto;
                      padding: 16px 0;
                      display: flex;
                      align-items: center;
                      justify-content: space-between;
                      gap: 16px;
                    }
                    .brand {
                      color: var(--text);
                      font-weight: 800;
                      text-decoration: none;
                    }
                    .links {
                      display: flex;
                      flex-wrap: wrap;
                      justify-content: flex-end;
                      gap: 10px 16px;
                      font-size: 0.94rem;
                      font-weight: 700;
                    }
                    main {
                      width: min(100% - 32px, 900px);
                      margin: 0 auto;
                      padding: 44px 0 64px;
                    }
                    article {
                      border: 1px solid var(--line);
                      border-radius: 8px;
                      background: var(--surface);
                      box-shadow: var(--shadow);
                      padding: clamp(24px, 5vw, 52px);
                    }
                    h1, h2, h3, h4, h5, h6 {
                      margin: 0;
                      line-height: 1.18;
                      color: var(--text);
                      letter-spacing: 0;
                    }
                    h1 {
                      font-size: clamp(2.1rem, 5vw, 3.8rem);
                      margin-bottom: 20px;
                    }
                    h2 {
                      margin-top: 40px;
                      padding-top: 28px;
                      border-top: 1px solid var(--line);
                      font-size: clamp(1.45rem, 3vw, 2rem);
                    }
                    h3 { margin-top: 28px; font-size: 1.25rem; }
                    p, ul, ol, pre { margin: 16px 0 0; }
                    p, li { color: var(--muted); }
                    ul, ol { padding-left: 1.35rem; }
                    li + li { margin-top: 6px; }
                    code {
                      padding: 0.12rem 0.3rem;
                      border-radius: 5px;
                      background: var(--code-bg);
                      color: var(--text);
                      font-size: 0.92em;
                    }
                    pre {
                      max-width: 100%;
                      overflow-x: auto;
                      border: 1px solid var(--line);
                      border-radius: 8px;
                      background: var(--code-bg);
                      padding: 16px;
                    }
                    pre code {
                      padding: 0;
                      background: transparent;
                      white-space: pre;
                    }
                    .footer {
                      width: min(100% - 32px, 900px);
                      margin: 0 auto;
                      padding: 0 0 36px;
                      color: var(--muted);
                      font-size: 0.92rem;
                      text-align: center;
                    }
                    @media (max-width: 700px) {
                      .nav {
                        align-items: flex-start;
                        flex-direction: column;
                      }
                      .links {
                        justify-content: flex-start;
                      }
                      main { padding-top: 28px; }
                      article { padding: 22px; }
                    }
                  </style>
                </head>
                <body>
                <header class="topbar">
                  <div class="nav">
                    <a class="brand" href="../">Spring Modulith Order Platform</a>
                    <nav class="links" aria-label="Documentation">
                      <a href="../openapi/">OpenAPI</a>
                      <a href="../openapi/openapi.json">OpenAPI JSON</a>
                      <a href="../javadoc/">Javadoc</a>
                      <a href="../jacoco/">JaCoCo</a>
                      <a href="https://github.com/DanieleMasone/spring-modulith-order-platform">GitHub</a>
                    </nav>
                  </div>
                </header>
                <main>
                  <article>
                """ + body + """
                  </article>
                </main>
                <footer class="footer">
                  Generated by Maven from <code>docs/user-guide.md</code>. Do not commit this HTML output.
                </footer>
                </body>
                </html>
                """;
    }
}
