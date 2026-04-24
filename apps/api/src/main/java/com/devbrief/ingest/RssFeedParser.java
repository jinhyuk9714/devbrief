package com.devbrief.ingest;

import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class RssFeedParser {
    public List<ParsedArticle> parse(String xml, Long sourceId, String category) {
        try {
            SyndFeedInput input = new SyndFeedInput();
            var feed = input.build(new XmlReader(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
            return feed.getEntries().stream()
                    .map(entry -> new ParsedArticle(
                            sourceId,
                            category,
                            entry.getTitle(),
                            entry.getLink(),
                            entry.getAuthor(),
                            entry.getPublishedDate() == null ? Instant.now() : entry.getPublishedDate().toInstant(),
                            excerpt(entry.getDescription() == null ? "" : entry.getDescription().getValue())
                    ))
                    .toList();
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unable to parse RSS feed", ex);
        }
    }

    private String excerpt(String value) {
        String cleaned = value.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= 180) {
            return cleaned;
        }
        return cleaned.substring(0, 177).trim() + "...";
    }
}

