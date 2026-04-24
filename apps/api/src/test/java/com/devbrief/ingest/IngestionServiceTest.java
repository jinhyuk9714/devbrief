package com.devbrief.ingest;

import com.devbrief.domain.*;
import com.devbrief.ops.RedisGateway;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IngestionServiceTest {

    @Test
    void fetchFailureMessageRemovesLargeHtmlBodies() {
        String message = IngestionService.fetchFailureMessage(
                new IllegalArgumentException("404 Not Found: \"<html><body>" + "x".repeat(2_000) + "</body></html>\"")
        );

        assertThat(message).isEqualTo("수집 실패 후 대체 데이터를 사용했습니다: 404 Not Found");
    }

    @Test
    void recordsSourceResultsAndContinuesWhenOneSourceFails() {
        Source failing = Source.create("Broken Feed", "RSS", "https://example.com/broken.xml", "Developer Tools");
        Source working = Source.create("Working Feed", "RSS", "https://example.com/working.xml", "AI Models");
        SourceRepository sourceRepository = mock(SourceRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        RedisGateway redisGateway = mock(RedisGateway.class);
        NewsSourceCatalog catalog = mock(NewsSourceCatalog.class);

        when(sourceRepository.findByEnabledTrueOrderByNameAsc()).thenReturn(List.of(failing, working));
        when(catalog.defaults()).thenReturn(List.of());
        when(catalog.demoArticlesFor(failing)).thenThrow(new IllegalStateException("boom " + "x".repeat(2_000)));
        when(catalog.demoArticlesFor(working)).thenReturn(List.of(new ParsedArticle(
                null,
                "AI Models",
                "Working model update",
                "https://example.com/working",
                "Working Feed",
                Instant.parse("2026-04-24T09:00:00Z"),
                "A working source still imports after another source fails."
        )));
        when(articleRepository.existsByContentHash(any())).thenReturn(false);

        IngestionService service = new IngestionService(
                sourceRepository,
                articleRepository,
                new ContentHashService(),
                catalog,
                mock(RssFeedParser.class),
                new GitHubTrendingParser(),
                redisGateway,
                false
        );

        IngestionService.IngestionResult result = service.run();

        assertThat(result.sourcesChecked()).isEqualTo(2);
        assertThat(result.articlesImported()).isEqualTo(1);
        assertThat(result.failedSources()).contains("Broken Feed");
        assertThat(result.sourceResults()).extracting(IngestionService.SourceResult::sourceName)
                .containsExactly("Broken Feed", "Working Feed");
        assertThat(result.sourceResults().get(0).status()).isEqualTo(SourceFetchStatus.FAILED);
        assertThat(result.sourceResults().get(0).message()).hasSizeLessThanOrEqualTo(1_000);
        assertThat(result.sourceResults().get(1).status()).isEqualTo(SourceFetchStatus.DEMO);
        assertThat(failing.getLastFetchStatus()).isEqualTo(SourceFetchStatus.FAILED);
        assertThat(failing.getLastFetchMessage()).hasSizeLessThanOrEqualTo(1_000);
        assertThat(working.getLastFetchStatus()).isEqualTo(SourceFetchStatus.DEMO);
        assertThat(working.getLastArticleCount()).isEqualTo(1);
        assertThat(working.isLastUsedFallback()).isTrue();
        verify(articleRepository).save(any(Article.class));
    }
}
