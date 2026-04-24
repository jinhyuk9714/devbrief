package com.devbrief.briefing;

import com.devbrief.domain.Article;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class BriefingQualityValidator {
    private static final Pattern HANGUL = Pattern.compile("[가-힣]");
    private static final List<String> GENERIC_PHRASES = List.of(
            "개발자에게 중요한 변화입니다",
            "여러 출처에서 비슷한 흐름이 보입니다",
            "최신 동향을 확인해야 합니다",
            "현재 스택에 미치는 영향을 검토하세요",
            "작은 실험을 만들어 장단점을 기록하세요"
    );
    private static final List<String> ACTION_HINTS = List.of(
            "하세요", "하기", "확인", "검토", "기록", "추가", "비교", "만들", "실험", "테스트", "분리", "정리", "측정", "표시"
    );

    private BriefingQualityValidator() {
    }

    static void validate(GeneratedBriefing briefing, OpenAiSummaryRequest request) {
        validateKorean(briefing);
        validateActionItems(briefing.actionItems());
        validateNotGeneric(briefing);
        if (request == null || request.articles() == null || request.articles().isEmpty()) {
            return;
        }

        Article lead = request.articles().getFirst();
        String combined = combinedText(briefing);
        String sourceName = lead.getSource().getName();
        String title = lead.getTitle();
        if (sourceName != null && !sourceName.isBlank() && !combined.contains(sourceName)) {
            throw new IllegalArgumentException("원문 source 이름을 번역하지 말고 그대로 포함해야 합니다.");
        }
        if (title != null && !title.isBlank() && !combined.contains(title)) {
            throw new IllegalArgumentException("원문 title을 번역하지 말고 그대로 포함해야 합니다.");
        }
    }

    private static void validateKorean(GeneratedBriefing briefing) {
        for (String field : fields(briefing)) {
            if (!HANGUL.matcher(field).find()) {
                throw new IllegalArgumentException("OpenAI 브리핑은 한국어 문장을 포함해야 합니다.");
            }
        }
    }

    private static void validateActionItems(List<String> actionItems) {
        for (String action : actionItems) {
            boolean actionable = ACTION_HINTS.stream().anyMatch(action::contains);
            if (action.length() < 12 || !actionable) {
                throw new IllegalArgumentException("actionItems는 바로 실행 가능한 한국어 동사형 문장이어야 합니다.");
            }
        }
    }

    private static void validateNotGeneric(GeneratedBriefing briefing) {
        String combined = combinedText(briefing);
        if (GENERIC_PHRASES.stream().anyMatch(combined::contains)) {
            throw new IllegalArgumentException("원문 근거가 부족한 일반론 브리핑입니다.");
        }
    }

    private static String combinedText(GeneratedBriefing briefing) {
        return String.join("\n", fields(briefing));
    }

    private static List<String> fields(GeneratedBriefing briefing) {
        List<String> fields = new ArrayList<>();
        fields.add(briefing.summary());
        fields.add(briefing.whyItMatters());
        fields.addAll(briefing.keyPoints());
        fields.addAll(briefing.actionItems());
        fields.addAll(briefing.riskNotes());
        return fields;
    }
}
