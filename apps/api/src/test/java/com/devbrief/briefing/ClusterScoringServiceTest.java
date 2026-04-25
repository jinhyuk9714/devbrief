package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.Source;
import com.devbrief.domain.TopicCluster;
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

    @Test
    void groupsByTitleSimilarityWithoutOverGroupingGenericAgentMentions() {
        Source source = Source.create("Hacker News", "RSS", "https://news.ycombinator.com/rss", "Developer Tools");
        Article firstTrace = Article.create(source, "Browser automation trace replay improves debugging", "https://example.com/trace-1", "HN",
                Instant.parse("2026-04-24T08:00:00Z"), "Trace replay helps developers reproduce browser failures in CI.", "trace-1");
        Article secondTrace = Article.create(source, "Trace replay shortens browser debugging loops", "https://example.com/trace-2", "HN",
                Instant.parse("2026-04-24T09:00:00Z"), "Browser automation tools use traces to explain failures.", "trace-2");
        Article unrelatedAgent = Article.create(source, "Agent runtime adds billing controls", "https://example.com/runtime", "HN",
                Instant.parse("2026-04-24T10:00:00Z"), "A hosted agent runtime changes spend management for teams.", "runtime");

        ClusterScoringService service = new ClusterScoringService();

        var clusters = service.cluster(List.of(firstTrace, secondTrace, unrelatedAgent));

        assertThat(clusters).anySatisfy(cluster -> {
            assertThat(cluster.getTitle()).containsIgnoringCase("trace");
            assertThat(cluster.getArticleCount()).isEqualTo(2);
        });
        assertThat(clusters).anySatisfy(cluster -> {
            assertThat(cluster.getTitle()).contains("Agent runtime");
            assertThat(cluster.getArticleCount()).isEqualTo(1);
        });
    }

    @Test
    void putsTitleRepresentativeArticleFirstInCluster() {
        Source source = Source.create("GitHub Trending", "API", "https://github.com/trending", "Open Source");
        Article firstSeen = Article.create(source, "MCP adoption note", "https://example.com/mcp-note", "GitHub",
                Instant.parse("2026-04-24T08:00:00Z"), "MCP use is growing across teams.", "mcp-note");
        Article representative = Article.create(source, "MCP security workflow model for coding agents", "https://example.com/mcp-security", "GitHub",
                Instant.parse("2026-04-24T09:00:00Z"), "MCP servers connect agent workflow security checks to developer tools.", "mcp-security");

        ClusterScoringService service = new ClusterScoringService();

        var clusters = service.cluster(List.of(firstSeen, representative));

        TopicCluster cluster = clusters.getFirst();
        assertThat(cluster.getTitle()).isEqualTo(representative.getTitle());
        assertThat(cluster.getArticles().getFirst().getTitle()).isEqualTo(representative.getTitle());
    }

    @Test
    void usesWeightedTermSimilarityForRelatedSearchStories() {
        Source source = Source.create("Hacker News", "RSS", "https://news.ycombinator.com/rss", "Developer Tools");
        Article first = Article.create(source, "Semantic rerank pipeline improves code search", "https://example.com/search-1", "HN",
                Instant.parse("2026-04-24T08:00:00Z"), "Teams use hybrid search rerank signals for pull request navigation.", "search-1");
        Article second = Article.create(source, "Code search reranking pipeline gets production tuning", "https://example.com/search-2", "HN",
                Instant.parse("2026-04-24T09:00:00Z"), "Hybrid retrieval rerank reduces duplicate results for repositories.", "search-2");
        Article unrelated = Article.create(source, "Runtime agent billing controls reach hosted IDEs", "https://example.com/billing", "HN",
                Instant.parse("2026-04-24T10:00:00Z"), "Teams track agent spend limits and monthly invoices in coding environments.", "billing");

        ClusterScoringService service = new ClusterScoringService();

        var clusters = service.cluster(List.of(first, second, unrelated));

        assertThat(clusters).anySatisfy(cluster -> {
            assertThat(cluster.getTitle()).contains("search");
            assertThat(cluster.getArticleCount()).isEqualTo(2);
        });
        assertThat(clusters).anySatisfy(cluster -> {
            assertThat(cluster.getTitle()).contains("billing");
            assertThat(cluster.getArticleCount()).isEqualTo(1);
        });
    }

    @Test
    void keepsSimilarTermsSeparateAcrossCategories() {
        Source tools = Source.create("Hacker News", "RSS", "https://news.ycombinator.com/rss", "Developer Tools");
        Source security = Source.create("Security Blog", "RSS", "https://example.com/security", "Security");
        Article toolsArticle = Article.create(tools, "Code search rerank pipeline improves debugging", "https://example.com/tools", "HN",
                Instant.parse("2026-04-24T08:00:00Z"), "Hybrid search rerank helps teams find repository failures.", "tools");
        Article securityArticle = Article.create(security, "Code search rerank reveals secret exposure", "https://example.com/security", "Security",
                Instant.parse("2026-04-24T09:00:00Z"), "Hybrid search rerank highlights leaked credentials in repositories.", "security");

        ClusterScoringService service = new ClusterScoringService();

        var clusters = service.cluster(List.of(toolsArticle, securityArticle));

        assertThat(clusters).hasSize(2);
        assertThat(clusters).extracting(TopicCluster::getCategory)
                .containsExactlyInAnyOrder("Developer Tools", "Security");
    }
}
