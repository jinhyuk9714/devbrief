package com.devbrief.i18n;

import com.devbrief.domain.Article;
import com.devbrief.domain.Source;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KoreanDisplayTextTest {

    @Test
    void translatesKnownCategoriesAndDemoClusterTitles() {
        KoreanDisplayText displayText = new KoreanDisplayText();

        assertThat(displayText.category("AI Models")).isEqualTo("AI 모델");
        assertThat(displayText.category("Developer Tools")).isEqualTo("개발 도구");
        assertThat(displayText.briefingTitle("MCP server adoption rises across coding agents"))
                .isEqualTo("코딩 에이전트 전반으로 MCP 서버 도입 확산");
    }

    @Test
    void keepsUnknownArticleTitlesUnchanged() {
        KoreanDisplayText displayText = new KoreanDisplayText();

        assertThat(displayText.briefingTitle("A brand new upstream article title"))
                .isEqualTo("A brand new upstream article title");
    }

    @Test
    void rewritesOldFallbackCopyAndCleansArxivExcerptSignals() {
        KoreanDisplayText displayText = new KoreanDisplayText();
        Source source = Source.create("arXiv AI", "RSS", "https://arxiv.org/rss/cs.AI", "AI Models");
        Article article = Article.create(source, "HiPO: Hierarchical Preference Optimization for Adaptive Reasoning in LLMs",
                "https://arxiv.org/abs/2604.20140", "arXiv AI", Instant.parse("2026-04-24T09:00:00Z"),
                "arXiv:2604.20140v1 Announce Type: new Abstract: Direct Preference Optimization improves adaptive reasoning traces for language models.",
                "hipo");

        String summary = displayText.summary(
                "JTPRO: A Joint Tool-Prompt Reflective Optimization Framework for Language Agents",
                "AI Models",
                List.of(article),
                "JTPRO: A Joint Tool-Prompt Reflective Optimization Framework for Language Agents: arXiv AI 원문 'HiPO: Hierarchical Preference Optimization for Adaptive Reasoning in LLMs'에서 포착된 신호입니다."
        );
        List<String> actions = displayText.actionItems(
                "AI Models",
                List.of(article),
                List.of("arXiv AI의 'HiPO: Hierarchical Preference Optimization for Adaptive Reasoning in LLMs' 원문을 열어 현재 스택에 미치는 영향 표시하기")
        );

        assertThat(summary).doesNotContain("포착된 신호입니다", "arXiv:2604", "Announce Type");
        assertThat(summary).contains("단일 원문 브리핑", "Direct Preference Optimization");
        assertThat(actions.getFirst()).doesNotContain("현재 스택에 미치는 영향 표시하기", "Announce Type");
        assertThat(actions.getFirst()).contains("영향을 정리하기", "Direct Preference Optimization");
    }
}
