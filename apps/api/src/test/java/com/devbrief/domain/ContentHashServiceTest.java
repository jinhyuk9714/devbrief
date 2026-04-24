package com.devbrief.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContentHashServiceTest {

    @Test
    void normalizesEquivalentUrlsAndTitlesIntoSameHash() {
        ContentHashService service = new ContentHashService();

        String first = service.hash("  New Open Model Ships  ", "https://example.com/post?utm_source=hn");
        String second = service.hash("new open model ships", "https://example.com/post");

        assertThat(first).isEqualTo(second);
    }
}

