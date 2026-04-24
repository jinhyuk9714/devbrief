package com.devbrief.ingest;

import java.time.Instant;

public record ParsedArticle(
        Long sourceId,
        String category,
        String title,
        String url,
        String author,
        Instant publishedAt,
        String excerpt
) {
}

