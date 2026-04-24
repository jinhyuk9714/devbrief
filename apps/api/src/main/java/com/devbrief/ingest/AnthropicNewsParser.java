package com.devbrief.ingest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AnthropicNewsParser {

    public List<ParsedArticle> parse(String html, Long sourceId, String category) {
        var document = Jsoup.parse(html, "https://www.anthropic.com");
        Map<String, ParsedArticle> articles = new LinkedHashMap<>();
        for (Element link : document.select("a[href^=/news/]")) {
            String title = title(link);
            String url = link.absUrl("href");
            if (title.isBlank() || url.isBlank()) {
                continue;
            }
            articles.putIfAbsent(url, new ParsedArticle(
                    sourceId,
                    category,
                    title,
                    url,
                    "Anthropic",
                    Instant.now(),
                    excerpt(link)
            ));
        }
        if (articles.isEmpty()) {
            throw new IllegalArgumentException("Anthropic 뉴스 항목을 찾지 못했습니다.");
        }
        return List.copyOf(articles.values());
    }

    private String title(Element link) {
        Element heading = link.selectFirst("h1, h2, h3, h4");
        String value = heading == null ? link.text() : heading.text();
        return clean(value);
    }

    private String excerpt(Element link) {
        Element paragraph = link.selectFirst("p");
        String value = paragraph == null ? "Anthropic 뉴스룸에서 감지된 AI 모델 및 개발자 워크플로 신호입니다." : paragraph.text();
        String cleaned = clean(value);
        if (cleaned.length() <= 180) {
            return cleaned;
        }
        return cleaned.substring(0, 177).trim() + "...";
    }

    private String clean(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }
}
