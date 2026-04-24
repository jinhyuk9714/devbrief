package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.Source;
import com.devbrief.domain.TopicCluster;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicSummaryProviderTest {

    @Test
    void createsStableDemoSummaryWithActionItems() {
        Source source = Source.create("OpenAI Blog", "RSS", "https://example.com/feed", "AI Models");
        Article article = Article.create(source, "New model context features", "https://example.com/a", "OpenAI",
                Instant.parse("2026-04-24T09:00:00Z"), "New context APIs help developers ship agentic workflows.", "hash-1");
        TopicCluster cluster = TopicCluster.create("New model context features", "AI Models", 92, List.of(article));

        SummaryProvider provider = new DeterministicSummaryProvider();

        GeneratedBriefing generated = provider.generate(cluster, List.of(article));

        assertThat(generated.summary()).contains("모델 컨텍스트 기능이 에이전트 워크플로를 바꾸는 중");
        assertThat(generated.whyItMatters()).contains("AI 모델");
        assertThat(generated.actionItems()).hasSize(3);
        assertThat(generated.riskNotes()).isNotEmpty();
    }

    @Test
    void createsArticleSpecificFallbackSummariesForSameCategory() {
        Source firstSource = Source.create("OpenAI Blog", "RSS", "https://example.com/openai", "AI Models");
        Article firstArticle = Article.create(firstSource, "Realtime agents add durable context", "https://example.com/1", "OpenAI",
                Instant.parse("2026-04-24T08:00:00Z"), "Agents can preserve task state across longer coding sessions.", "hash-2");
        TopicCluster firstCluster = TopicCluster.create("Realtime agent context", "AI Models", 92, List.of(firstArticle));

        Source secondSource = Source.create("Google Developers", "RSS", "https://example.com/google", "AI Models");
        Article secondArticle = Article.create(secondSource, "Small Gemini models improve browser automation", "https://example.com/2", "Google",
                Instant.parse("2026-04-24T09:00:00Z"), "Browser automation benchmarks improve on lower latency model variants.", "hash-3");
        TopicCluster secondCluster = TopicCluster.create("Gemini browser automation", "AI Models", 90, List.of(secondArticle));

        SummaryProvider provider = new DeterministicSummaryProvider();

        GeneratedBriefing first = provider.generate(firstCluster, firstCluster.getArticles());
        GeneratedBriefing second = provider.generate(secondCluster, secondCluster.getArticles());

        assertThat(first.summary()).contains("OpenAI Blog", "Realtime agents add durable context");
        assertThat(second.summary()).contains("Google Developers", "Small Gemini models improve browser automation");
        assertThat(first.summary()).isNotEqualTo(second.summary());
    }

    @Test
    void usesArticleSignalsInWhyAndActionItems() {
        Source source = Source.create("GitHub Trending", "API", "https://github.com/trending", "Open Source");
        Article article = Article.create(source, "Vector cache library adds provenance checks", "https://example.com/vector", "GitHub",
                Instant.parse("2026-04-24T08:00:00Z"), "The library links cache entries to source repositories and freshness metadata.", "hash-4");
        TopicCluster cluster = TopicCluster.create("Vector cache provenance", "Open Source", 88, List.of(article));

        SummaryProvider provider = new DeterministicSummaryProvider();

        GeneratedBriefing generated = provider.generate(cluster, cluster.getArticles());

        assertThat(generated.whyItMatters()).contains("GitHub Trending", "Vector cache library adds provenance checks");
        assertThat(generated.actionItems()).anySatisfy(action -> assertThat(action).contains("Vector cache library adds provenance checks"));
    }
}
