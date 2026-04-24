package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.Source;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterScoringServiceTest {

    @Test
    void groupsSimilarArticlesAndRanksHighSignalDeveloperTopics() {
        Source source = Source.create("GitHub Trending", "API", "https://github.com/trending", "Open Source");
        Article first = Article.create(source, "MCP server adoption rises", "https://example.com/1", "GitHub",
                Instant.parse("2026-04-24T08:00:00Z"), "MCP servers are becoming common in coding tools.", "a");
        Article second = Article.create(source, "New MCP server registry launches", "https://example.com/2", "GitHub",
                Instant.parse("2026-04-24T09:00:00Z"), "A registry helps developers connect agents.", "b");
        Article lowSignal = Article.create(source, "Company newsletter issue 14", "https://example.com/3", "Team",
                Instant.parse("2026-04-20T09:00:00Z"), "Monthly housekeeping notes.", "c");

        ClusterScoringService service = new ClusterScoringService();

        var clusters = service.cluster(List.of(first, second, lowSignal));

        assertThat(clusters).isNotEmpty();
        assertThat(clusters.getFirst().getTitle()).contains("MCP");
        assertThat(clusters.getFirst().getArticleCount()).isEqualTo(2);
        assertThat(clusters.getFirst().getScore()).isGreaterThan(70);
    }
}

