package com.devbrief.ingest;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class GitHubTrendingParser {

    public List<ParsedArticle> parse(String html, Long sourceId, String category) {
        var document = Jsoup.parse(html, "https://github.com");
        List<ParsedArticle> articles = document.select("article.Box-row").stream()
                .map(article -> {
                    var link = article.selectFirst("h2 a[href]");
                    if (link == null) {
                        return null;
                    }
                    String title = link.text().replaceAll("\\s+", "").trim();
                    String url = link.absUrl("href");
                    String author = title.contains("/") ? title.substring(0, title.indexOf('/')) : "GitHub";
                    var description = article.selectFirst("p");
                    String excerpt = description == null || description.text().isBlank()
                            ? "GitHub Trending에서 감지된 개발자 관심 신호입니다."
                            : description.text();
                    return new ParsedArticle(sourceId, category, title, url, author, Instant.now(), excerpt);
                })
                .filter(ParsedArticle.class::isInstance)
                .map(ParsedArticle.class::cast)
                .toList();
        if (articles.isEmpty()) {
            throw new IllegalArgumentException("GitHub Trending 항목을 찾지 못했습니다.");
        }
        return articles;
    }
}
