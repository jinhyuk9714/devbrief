package com.devbrief.ingest;

import com.devbrief.domain.Source;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class NewsSourceCatalog {
    public List<SourceSpec> defaults() {
        return List.of(
                new SourceSpec("GitHub Trending", "API", "https://github.com/trending?since=daily", "Open Source"),
                new SourceSpec("Hacker News", "RSS", "https://news.ycombinator.com/rss", "Developer Tools"),
                new SourceSpec("arXiv AI", "RSS", "https://rss.arxiv.org/rss/cs.AI", "AI Models"),
                new SourceSpec("OpenAI Blog", "RSS", "https://openai.com/news/rss.xml", "AI Models"),
                new SourceSpec("Anthropic News", "RSS", "https://www.anthropic.com/news/rss.xml", "AI Models"),
                new SourceSpec("Google Developers", "RSS", "https://developers.googleblog.com/feeds/posts/default", "Cloud"),
                new SourceSpec("Microsoft Developer Blog", "RSS", "https://devblogs.microsoft.com/feed/", "Developer Tools"),
                new SourceSpec("Meta Engineering", "RSS", "https://engineering.fb.com/feed/", "Open Source")
        );
    }

    public List<ParsedArticle> demoArticlesFor(Source source) {
        Instant now = Instant.now();
        Map<String, List<String>> titles = Map.of(
                "Open Source", List.of(
                        "MCP server adoption rises across coding agents",
                        "New MCP server registry helps teams standardize tools"
                ),
                "AI Models", List.of(
                        "New model context features reshape agent workflows",
                        "Open-weight reasoning models push local AI experiments"
                ),
                "Developer Tools", List.of(
                        "Browser automation tools add better trace replay",
                        "Type-safe workflow builders expose integrations as agent tools"
                ),
                "Cloud", List.of(
                        "Edge runtimes add AI cache controls for streaming apps",
                        "Managed Postgres providers improve vector indexing defaults"
                ),
                "Security", List.of(
                        "Secret scanning moves earlier into AI coding workflows",
                        "Supply chain teams tighten GitHub Actions provenance"
                )
        );
        return titles.getOrDefault(source.getCategory(), titles.get("Developer Tools")).stream()
                .map(title -> new ParsedArticle(
                        source.getId(),
                        source.getCategory(),
                        title,
                        source.getUrl() + "#" + title.toLowerCase().replaceAll("[^a-z0-9]+", "-"),
                        source.getName(),
                        now,
                        title + " with practical implications for developer teams shipping AI-enabled software."
                ))
                .toList();
    }

    public record SourceSpec(String name, String type, String url, String category) {
    }
}

