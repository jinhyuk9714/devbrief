package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.Source;
import com.devbrief.domain.TopicCluster;

import java.time.Instant;
import java.util.List;

final class BriefingQualityFixtures {
    private BriefingQualityFixtures() {
    }

    static List<TopicCluster> representativeClusters() {
        return List.of(
                cluster(
                        "OpenAI Blog",
                        "RSS",
                        "AI Models",
                        "Realtime model context expands agent coding workflows",
                        "Realtime context APIs preserve task state across multi-step coding sessions.",
                        "https://example.com/ai-models",
                        "AI model context"
                ),
                cluster(
                        "Hacker News",
                        "RSS",
                        "Developer Tools",
                        "Browser trace replay shortens debugging loops",
                        "Trace replay lets teams reproduce flaky browser failures in CI.",
                        "https://example.com/developer-tools",
                        "Browser trace replay"
                ),
                cluster(
                        "GitHub Blog",
                        "RSS",
                        "Security",
                        "GitHub Actions provenance blocks untrusted releases",
                        "Provenance checks connect build artifacts to source commits and release workflows.",
                        "https://example.com/security",
                        "GitHub Actions provenance"
                )
        );
    }

    static GeneratedBriefing groundedBriefingFor(TopicCluster cluster) {
        Article lead = cluster.getArticles().getFirst();
        String source = lead.getSource().getName();
        String title = lead.getTitle();
        String excerpt = lead.getExcerpt();
        return new GeneratedBriefing(
                "%s의 '%s'는 %s 신호를 보여주는 뉴스입니다.".formatted(source, title, excerpt),
                "개발팀은 이 변화가 도구 선택, 검증 루프, 운영 위험에 어떤 영향을 주는지 확인해야 합니다.",
                List.of(
                        "%s 원문 '%s'가 %s 단서를 제공합니다.".formatted(source, title, excerpt),
                        "같은 카테고리의 기사 묶음이 실험 우선순위 조정을 요구합니다."
                ),
                List.of(
                        "%s 원문을 열어 %s 단서가 현재 스택에 미치는 영향을 기록하세요.".formatted(source, excerpt),
                        "작은 spike 브랜치에서 관련 워크플로를 재현하고 실패 로그를 정리하세요."
                ),
                List.of("초기 발표는 실제 운영 안정성과 다를 수 있으므로 원문 날짜와 릴리스 상태를 확인하세요.")
        );
    }

    private static TopicCluster cluster(String sourceName, String type, String category, String title, String excerpt, String url, String clusterTitle) {
        Source source = Source.create(sourceName, type, url + "/feed", category);
        Article article = Article.create(source, title, url, sourceName,
                Instant.parse("2026-04-24T09:00:00Z"), excerpt, "hash-" + category.replaceAll("[^A-Za-z]", "").toLowerCase());
        return TopicCluster.create(clusterTitle, category, 90, List.of(article));
    }
}
