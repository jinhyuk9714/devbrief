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
}
