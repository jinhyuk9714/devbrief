package com.devbrief.briefing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BriefingQualityValidatorTest {

    @Test
    void acceptsRepresentativeGroundedKoreanBriefings() {
        for (var cluster : BriefingQualityFixtures.representativeClusters()) {
            var request = new OpenAiSummaryRequest("gpt-4o-mini", cluster, cluster.getArticles());
            var briefing = BriefingQualityFixtures.groundedBriefingFor(cluster);

            assertThatCode(() -> BriefingQualityValidator.validate(briefing, request))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void rejectsEnglishOnlyBriefings() {
        var cluster = BriefingQualityFixtures.representativeClusters().getFirst();
        var request = new OpenAiSummaryRequest("gpt-4o-mini", cluster, cluster.getArticles());
        var briefing = new GeneratedBriefing(
                "OpenAI Blog reports a model context update.",
                "Developer teams need to review tool choices.",
                List.of("The source mentions context APIs.", "The update may affect agents."),
                List.of("Review the original source.", "Run a small spike."),
                List.of("Early announcements can be unstable.")
        );

        assertThatThrownBy(() -> BriefingQualityValidator.validate(briefing, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("한국어");
    }

    @Test
    void rejectsGenericBriefingsWithoutOriginalEvidence() {
        var cluster = BriefingQualityFixtures.representativeClusters().getFirst();
        var request = new OpenAiSummaryRequest("gpt-4o-mini", cluster, cluster.getArticles());
        var briefing = new GeneratedBriefing(
                "이 소식은 개발자에게 중요한 변화입니다.",
                "팀의 기술 선택과 실험 우선순위에 영향을 줄 수 있기 때문입니다.",
                List.of("여러 출처에서 비슷한 흐름이 보입니다.", "개발자는 최신 동향을 확인해야 합니다."),
                List.of("공식 문서를 읽고 현재 스택에 미치는 영향을 검토하세요.", "작은 실험을 만들어 장단점을 기록하세요."),
                List.of("초기 발표는 실제 운영 안정성과 다를 수 있습니다.")
        );

        assertThatThrownBy(() -> BriefingQualityValidator.validate(briefing, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원문");
    }

    @Test
    void rejectsTranslatedSourceOrTitle() {
        var cluster = BriefingQualityFixtures.representativeClusters().getFirst();
        var request = new OpenAiSummaryRequest("gpt-4o-mini", cluster, cluster.getArticles());
        var briefing = new GeneratedBriefing(
                "오픈AI 블로그의 '실시간 모델 컨텍스트가 에이전트 코딩 워크플로를 확장'은 모델 컨텍스트 신호입니다.",
                "개발팀은 도구 선택과 검증 루프에 미치는 영향을 확인해야 합니다.",
                List.of("원문은 컨텍스트 API 신호를 제공합니다.", "기사 묶음이 실험 우선순위 조정을 요구합니다."),
                List.of("원문을 열어 현재 스택에 미치는 영향을 기록하세요.", "작은 spike 브랜치에서 관련 워크플로를 재현하세요."),
                List.of("초기 발표는 실제 운영 안정성과 다를 수 있습니다.")
        );

        assertThatThrownBy(() -> BriefingQualityValidator.validate(briefing, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원문");
    }

    @Test
    void rejectsNonActionableActionItems() {
        var cluster = BriefingQualityFixtures.representativeClusters().getFirst();
        var request = new OpenAiSummaryRequest("gpt-4o-mini", cluster, cluster.getArticles());
        var briefing = BriefingQualityFixtures.groundedBriefingFor(cluster);
        var nonActionable = new GeneratedBriefing(
                briefing.summary(),
                briefing.whyItMatters(),
                briefing.keyPoints(),
                List.of("중요한 변화입니다.", "팀 영향이 큽니다."),
                briefing.riskNotes()
        );

        assertThatThrownBy(() -> BriefingQualityValidator.validate(nonActionable, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("actionItems");
    }
}
