package com.devbrief.i18n;

import com.devbrief.domain.Article;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class KoreanDisplayText {
    private static final Map<String, String> CATEGORIES = Map.of(
            "AI Models", "AI 모델",
            "Open Source", "오픈소스",
            "Developer Tools", "개발 도구",
            "Security", "보안",
            "Cloud", "클라우드"
    );

    private static final Map<String, String> DEMO_TITLES = Map.ofEntries(
            Map.entry("MCP server adoption rises across coding agents", "코딩 에이전트 전반으로 MCP 서버 도입 확산"),
            Map.entry("New MCP server registry helps teams standardize tools", "새 MCP 서버 레지스트리로 팀 도구 표준화 가속"),
            Map.entry("New model context features reshape agent workflows", "모델 컨텍스트 기능이 에이전트 워크플로를 바꾸는 중"),
            Map.entry("New model context features", "모델 컨텍스트 기능이 에이전트 워크플로를 바꾸는 중"),
            Map.entry("Open-weight reasoning models push local AI experiments", "오픈 웨이트 추론 모델로 로컬 AI 실험 확대"),
            Map.entry("Browser automation tools add better trace replay", "브라우저 자동화 도구의 트레이스 재생 강화"),
            Map.entry("Type-safe workflow builders expose integrations as agent tools", "타입 안전 워크플로 빌더가 통합 기능을 에이전트 도구로 공개"),
            Map.entry("Edge runtimes add AI cache controls for streaming apps", "엣지 런타임에 스트리밍 AI 캐시 제어 추가"),
            Map.entry("Managed Postgres providers improve vector indexing defaults", "관리형 Postgres의 벡터 인덱싱 기본값 개선"),
            Map.entry("Secret scanning moves earlier into AI coding workflows", "AI 코딩 워크플로 초기에 시크릿 스캐닝 배치"),
            Map.entry("Supply chain teams tighten GitHub Actions provenance", "공급망 팀의 GitHub Actions 출처 검증 강화")
    );

    public String category(String value) {
        return CATEGORIES.getOrDefault(value, value);
    }

    public String briefingTitle(String value) {
        return DEMO_TITLES.getOrDefault(value, value);
    }

    public String summary(String title, String category, String storedSummary) {
        if (storedSummary != null && !storedSummary.isBlank() && !looksLikeDeterministicEnglish(title, storedSummary)) {
            return storedSummary;
        }
        return "%s: %s".formatted(briefingTitle(title), summaryBody(category));
    }

    public String whyItMatters(String category, String storedWhyItMatters) {
        if (storedWhyItMatters != null && !storedWhyItMatters.contains(category)) {
            return storedWhyItMatters;
        }
        return switch (category) {
            case "AI Models" -> "AI 모델 흐름은 모델 선택, 컨텍스트 설계, 에이전트 검증 방식에 바로 영향을 줍니다.";
            case "Open Source" -> "오픈소스 흐름은 팀이 표준 도구 계층과 운영 가능한 실험 범위를 다시 정하는 신호입니다.";
            case "Developer Tools" -> "개발 도구 흐름은 테스트, 디버깅, 배포 전 검증 루프를 더 짧게 만들 수 있습니다.";
            case "Security" -> "보안 흐름은 AI가 만든 코드와 자동화된 워크플로의 위험을 더 이른 단계에서 줄이는 데 중요합니다.";
            case "Cloud" -> "클라우드 흐름은 작은 팀도 인프라 복잡도를 낮추면서 AI 기능을 운영에 올릴 기회를 줍니다.";
            default -> "%s 흐름은 개발자가 도구 선택과 실험 우선순위를 조정해야 하는 신호입니다.".formatted(category(category));
        };
    }

    public List<String> keyPoints(List<Article> articles, List<String> storedKeyPoints) {
        if (articles.isEmpty()) {
            return storedKeyPoints;
        }
        return articles.stream()
                .limit(3)
                .map(article -> "%s 원문에서 관련 신호를 확인했습니다: %s"
                        .formatted(article.getSource().getName(), article.getTitle()))
                .toList();
    }

    public List<String> actionItems(String category) {
        return switch (category) {
            case "AI Models" -> List.of(
                    "현재 에이전트 워크플로에서 컨텍스트 회수 기준을 명시하기",
                    "작은 spike 브랜치에서 모델 기능 변화를 재현해보기",
                    "비용, 지연, 실패 로그를 함께 남겨 다음 실험 기준 만들기"
            );
            case "Open Source" -> List.of(
                    "관련 저장소와 공식 문서를 확인하고 현재 스택 영향 표시하기",
                    "내부 API 하나를 read-only 도구 인터페이스로 감싸보기",
                    "권한, 감사 로그, freshness 필드를 체크리스트에 추가하기"
            );
            case "Security" -> List.of(
                    "AI 코딩 흐름 초기에 시크릿 스캔과 정책 검사를 배치하기",
                    "실패 메시지가 개발자 행동으로 이어지도록 문구를 정리하기",
                    "출처와 날짜를 함께 기록해 과장된 신호를 걸러내기"
            );
            default -> List.of(
                    "원문과 공식 문서를 확인하고 현재 스택 영향 표시하기",
                    "작은 spike 브랜치에서 변경점을 재현해보기",
                    "장점, 위험, 다음 실험을 팀 공유용 3줄로 남기기"
            );
        };
    }

    public List<String> riskNotes() {
        return List.of(
                "초기 발표나 트렌딩 저장소는 실제 운영 안정성과 다를 수 있습니다.",
                "원문 출처와 날짜를 함께 확인해야 오래된 정보나 과장을 줄일 수 있습니다."
        );
    }

    private String summaryBody(String category) {
        return switch (category) {
            case "AI Models" -> "모델 기능 변화가 에이전트 설계와 개발자 워크플로에 미치는 영향이 커지고 있습니다.";
            case "Open Source" -> "개발자가 쓰는 도구와 에이전트 연결 방식이 공통 표준 쪽으로 움직이고 있습니다.";
            case "Developer Tools" -> "테스트, 브라우저 자동화, 워크플로 도구가 실제 디버깅 루프를 줄이는 방향으로 개선되고 있습니다.";
            case "Security" -> "AI 코딩과 자동화 파이프라인에서 보안 검사를 더 이른 단계로 옮기는 흐름이 보입니다.";
            case "Cloud" -> "클라우드 기본 기능이 AI 애플리케이션 운영과 실험 비용을 낮추는 쪽으로 정리되고 있습니다.";
            default -> "개발자가 도구 선택과 실험 우선순위를 조정해야 하는 신호입니다.";
        };
    }

    private boolean looksLikeDeterministicEnglish(String title, String storedSummary) {
        return storedSummary.startsWith(title + ":") || storedSummary.contains("practical implications for developer teams");
    }
}
