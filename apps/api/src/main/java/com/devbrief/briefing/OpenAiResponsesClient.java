package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class OpenAiResponsesClient implements OpenAiBriefingClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
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

    private final RestClient restClient;
    private final String apiKey;

    public OpenAiResponsesClient(RestClient.Builder builder,
                                 @Value("${devbrief.openai.api-key:}") String apiKey,
                                 @Value("${devbrief.openai.base-url:https://api.openai.com/v1}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public GeneratedBriefing generate(OpenAiSummaryRequest request) {
        String body = restClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(requestBody(request))
                .retrieve()
                .body(String.class);
        try {
            return parseGeneratedBriefing(body, request);
        } catch (Exception ex) {
            throw new IllegalArgumentException("OpenAI 요약 응답을 파싱하지 못했습니다.", ex);
        }
    }

    private Map<String, Object> requestBody(OpenAiSummaryRequest request) {
        return Map.of(
                "model", request.model(),
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        AI/개발 뉴스 브리핑 편집자입니다. 반드시 한국어 JSON object 하나만 반환하세요.
                                        Markdown이나 설명 문장 없이 JSON object 하나만 반환하세요.
                                        필수 필드: summary, whyItMatters, keyPoints, actionItems, riskNotes.
                                        keyPoints와 actionItems는 각각 2-4개, riskNotes는 1-3개를 작성하세요.
                                        summary는 1-2문장으로 무슨 일이 있었는지 설명하고, lead source 이름과 lead title 원문을 그대로 포함하세요.
                                        whyItMatters는 개발자의 도구 선택, 아키텍처, 보안, 비용, 검증 루프 중 무엇이 바뀌는지 구체적으로 쓰세요.
                                        keyPoints는 원문 title/source/excerpt 신호를 근거로만 작성하세요.
                                        actionItems는 개발자가 오늘 바로 해볼 일을 동사형 한국어 문장으로 쓰세요.
                                        riskNotes는 과장, 오래된 정보, 초기 발표, 운영 안정성 중 확인해야 할 점을 쓰세요.
                                        source 이름과 원문 제목은 절대 번역하지 마세요. 원문 전문을 재게시하지 말고 짧은 신호와 판단만 요약하세요.
                                        """
                        ),
                        Map.of("role", "user", "content", prompt(request))
                ),
                "text", Map.of("format", Map.of("type", "json_object"))
        );
    }

    private String prompt(OpenAiSummaryRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("클러스터 제목: ").append(request.cluster().getTitle()).append('\n');
        builder.append("카테고리: ").append(request.cluster().getCategory()).append('\n');
        builder.append("점수: ").append(request.cluster().getScore()).append('\n');
        request.articles().stream().findFirst().ifPresent(article -> {
            builder.append("lead source 원문 유지: ").append(article.getSource().getName()).append('\n');
            builder.append("lead title 원문 유지: ").append(article.getTitle()).append('\n');
        });
        builder.append("작성 규칙:\n");
        builder.append("- summary나 keyPoints에는 lead source와 lead title을 원문 그대로 포함하세요.\n");
        builder.append("- 일반론 대신 excerpt의 단서를 최소 1개 반영하세요.\n");
        builder.append("- actionItems는 확인하세요, 기록하세요, 비교하세요처럼 바로 실행 가능한 한국어 동사형으로 쓰세요.\n");
        builder.append("원문 신호:\n");
        for (Article article : request.articles().stream().limit(8).toList()) {
            builder.append("- [").append(article.getSource().getName()).append("] ")
                    .append(article.getTitle()).append(" / ")
                    .append(article.getExcerpt()).append(" / ")
                    .append(article.getUrl()).append('\n');
        }
        return builder.toString();
    }

    static GeneratedBriefing parseGeneratedBriefing(String responseBody, OpenAiSummaryRequest request) throws Exception {
        GeneratedBriefing briefing = parseGeneratedBriefing(responseBody);
        validateAgainstRequest(briefing, request);
        return briefing;
    }

    static GeneratedBriefing parseGeneratedBriefing(String responseBody) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(responseBody);
        String outputText = root.path("output_text").isTextual() ? root.path("output_text").asText() : null;
        if (outputText == null || outputText.isBlank()) {
            outputText = findOutputText(root);
        }
        if (outputText == null || outputText.isBlank()) {
            throw new IllegalArgumentException("output_text가 비어 있습니다.");
        }
        JsonNode briefing = OBJECT_MAPPER.readTree(outputText);
        return new GeneratedBriefing(
                requiredText(briefing, "summary"),
                requiredText(briefing, "whyItMatters"),
                textArray(briefing, "keyPoints", 2),
                textArray(briefing, "actionItems", 2),
                textArray(briefing, "riskNotes", 1)
        );
    }

    private static String findOutputText(JsonNode root) {
        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText()) && content.path("text").isTextual()) {
                    return content.path("text").asText();
                }
            }
        }
        return null;
    }

    private static String requiredText(JsonNode node, String field) {
        String value = node.path(field).asText("");
        if (value.isBlank()) {
            throw new IllegalArgumentException(field + " 값이 비어 있습니다.");
        }
        return value;
    }

    private static List<String> textArray(JsonNode node, String field, int minSize) {
        List<String> values = new ArrayList<>();
        for (JsonNode item : node.path(field)) {
            String value = item.isTextual() ? item.asText().trim() : "";
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        if (values.size() < minSize) {
            throw new IllegalArgumentException(field + " 목록은 최소 %d개 필요합니다.".formatted(minSize));
        }
        return values;
    }

    private static void validateAgainstRequest(GeneratedBriefing briefing, OpenAiSummaryRequest request) {
        validateKorean(briefing);
        validateActionItems(briefing.actionItems());
        validateNotGeneric(briefing);
        if (request == null || request.articles() == null || request.articles().isEmpty()) {
            return;
        }

        Article lead = request.articles().get(0);
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
        List<String> fields = new ArrayList<>();
        fields.add(briefing.summary());
        fields.add(briefing.whyItMatters());
        fields.addAll(briefing.keyPoints());
        fields.addAll(briefing.actionItems());
        fields.addAll(briefing.riskNotes());
        for (String field : fields) {
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
        List<String> fields = new ArrayList<>();
        fields.add(briefing.summary());
        fields.add(briefing.whyItMatters());
        fields.addAll(briefing.keyPoints());
        fields.addAll(briefing.actionItems());
        fields.addAll(briefing.riskNotes());
        return String.join("\n", fields);
    }
}
