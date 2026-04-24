package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.Source;
import com.devbrief.domain.TopicCluster;
import com.devbrief.i18n.KoreanDisplayText;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiSummaryProviderTest {

    @Test
    void usesOpenAiGeneratedKoreanBriefingWhenApiKeyExists() {
        TopicCluster cluster = cluster();
        OpenAiBriefingClient client = request -> new GeneratedBriefing(
                "여러 출처가 MCP 도구 표준화를 다루고 있습니다.",
                "개발팀의 에이전트 도구 연결 방식이 바뀌기 때문입니다.",
                List.of("MCP 서버 레지스트리가 반복적으로 언급됩니다.", "도구 권한 설계가 함께 논의됩니다."),
                List.of("내부 API 하나를 read-only MCP로 감싸보기", "권한 로그를 먼저 설계하기"),
                List.of("초기 벤더 발표는 실제 SDK 안정성과 다를 수 있습니다.")
        );
        SummaryProvider provider = new OpenAiSummaryProvider(client, new DeterministicSummaryProvider(new KoreanDisplayText()), "sk-test", "gpt-4o-mini");

        GeneratedBriefing generated = provider.generate(cluster, cluster.getArticles());

        assertThat(generated.summary()).contains("MCP 도구 표준화");
        assertThat(generated.actionItems()).contains("내부 API 하나를 read-only MCP로 감싸보기");
    }

    @Test
    void fallsBackToDeterministicSummaryWhenApiKeyIsMissingOrClientFails() {
        TopicCluster cluster = cluster();
        OpenAiBriefingClient client = request -> {
            throw new IllegalStateException("API unavailable");
        };
        SummaryProvider missingKeyProvider = new OpenAiSummaryProvider(client, new DeterministicSummaryProvider(new KoreanDisplayText()), "", "gpt-4o-mini");
        SummaryProvider failingProvider = new OpenAiSummaryProvider(client, new DeterministicSummaryProvider(new KoreanDisplayText()), "sk-test", "gpt-4o-mini");

        assertThat(missingKeyProvider.generate(cluster, cluster.getArticles()).summary()).contains("코딩 에이전트 전반으로 MCP 서버 도입 확산");
        assertThat(failingProvider.generate(cluster, cluster.getArticles()).summary()).contains("코딩 에이전트 전반으로 MCP 서버 도입 확산");
    }

    private TopicCluster cluster() {
        Source source = Source.create("Hacker News", "RSS", "https://news.ycombinator.com/rss", "Open Source");
        Article article = Article.create(source, "MCP server adoption rises across coding agents", "https://example.com/mcp", "HN",
                Instant.parse("2026-04-24T09:00:00Z"), "MCP servers are becoming the common tool layer for coding agents.", "hash-openai-1");
        return TopicCluster.create("MCP server adoption rises across coding agents", "Open Source", 88, List.of(article));
    }
}
