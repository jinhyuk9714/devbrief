package com.devbrief.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SourceTest {
    @Test
    void trimsFetchMessagesToColumnLimit() {
        Source source = Source.create("arXiv AI", "RSS", "https://example.com/feed", "AI Models");

        source.markFetchResult(
                SourceFetchStatus.FALLBACK,
                "수집 실패: " + "x".repeat(2_000),
                0,
                true,
                Instant.parse("2026-04-24T00:00:00Z")
        );

        assertThat(source.getLastFetchMessage()).hasSize(1_000);
    }
}
