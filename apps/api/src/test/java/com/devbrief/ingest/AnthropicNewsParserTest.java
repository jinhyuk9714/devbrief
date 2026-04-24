package com.devbrief.ingest;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AnthropicNewsParserTest {

    @Test
    void parsesAnthropicNewsroomHtmlLinks() throws Exception {
        String html = Files.readString(Path.of("src/test/resources/fixtures/anthropic-news.html"));

        var articles = new AnthropicNewsParser().parse(html, 12L, "AI Models");

        assertThat(articles).hasSize(2);
        assertThat(articles.getFirst().sourceId()).isEqualTo(12L);
        assertThat(articles.getFirst().category()).isEqualTo("AI Models");
        assertThat(articles.getFirst().title()).isEqualTo("Claude Opus 4.7");
        assertThat(articles.getFirst().url()).isEqualTo("https://www.anthropic.com/news/claude-opus-4-7");
        assertThat(articles.getFirst().author()).isEqualTo("Anthropic");
        assertThat(articles.getFirst().excerpt()).contains("long-running agent workflows");
        assertThat(articles).extracting(ParsedArticle::title)
                .doesNotContain("Research link outside newsroom");
    }
}
