package com.devbrief.ingest;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RssFeedParserTest {

    @Test
    void parsesRssItemsWithoutRepublishingFullContent() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                  <channel>
                    <title>Example Feed</title>
                    <item>
                      <title>Agents get better at code review</title>
                      <link>https://example.com/agents-review</link>
                      <author>dev@example.com</author>
                      <pubDate>Fri, 24 Apr 2026 10:00:00 GMT</pubDate>
                      <description>This update explains how agent review workflows improved across IDEs with detailed implementation examples that should be shortened.</description>
                    </item>
                  </channel>
                </rss>
                """;

        RssFeedParser parser = new RssFeedParser();

        var articles = parser.parse(xml, 42L, "AI Models");

        assertThat(articles).hasSize(1);
        assertThat(articles.getFirst().title()).isEqualTo("Agents get better at code review");
        assertThat(articles.getFirst().url()).isEqualTo("https://example.com/agents-review");
        assertThat(articles.getFirst().publishedAt()).isEqualTo(Instant.parse("2026-04-24T10:00:00Z"));
        assertThat(articles.getFirst().excerpt()).hasSizeLessThanOrEqualTo(180);
    }
}

