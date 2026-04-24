package com.devbrief.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {
    @Test
    void trimsPersistedTextFieldsToColumnLimits() {
        Source source = Source.create("arXiv AI", "RSS", "https://example.com/feed", "AI Models");

        Article article = Article.create(
                source,
                "t".repeat(600),
                "https://example.com/" + "u".repeat(1_200),
                "a".repeat(600),
                Instant.parse("2026-04-24T00:00:00Z"),
                "e".repeat(600),
                "hash"
        );

        assertThat(article.getTitle()).hasSize(500);
        assertThat(article.getUrl()).hasSize(1_000);
        assertThat(article.getAuthor()).hasSize(255);
        assertThat(article.getExcerpt()).hasSize(500);
    }
}
