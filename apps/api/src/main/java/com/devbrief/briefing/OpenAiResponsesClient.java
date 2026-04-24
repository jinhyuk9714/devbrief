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

@Component
public class OpenAiResponsesClient implements OpenAiBriefingClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
            return parseGeneratedBriefing(body);
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
                                        AI/개발 뉴스 브리핑 편집자입니다. 반드시 한국어 JSON만 반환하세요.
                                        Markdown이나 설명 문장 없이 JSON object 하나만 반환하세요.
                                        필수 필드: summary, whyItMatters, keyPoints, actionItems, riskNotes.
                                        keyPoints와 actionItems는 각각 2-4개, riskNotes는 1-3개를 작성하세요.
                                        summary는 무슨 일인지, whyItMatters는 왜 중요한지, actionItems는 개발자가 바로 해볼 일을 분리하세요.
                                        원문 출처와 제목을 근거로 쓰고, source 이름과 원문 제목은 번역하지 마세요.
                                        원문 전문을 재게시하지 말고 짧은 신호와 판단만 요약하세요.
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
        builder.append("원문 신호:\n");
        for (Article article : request.articles().stream().limit(8).toList()) {
            builder.append("- [").append(article.getSource().getName()).append("] ")
                    .append(article.getTitle()).append(" / ")
                    .append(article.getExcerpt()).append(" / ")
                    .append(article.getUrl()).append('\n');
        }
        return builder.toString();
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
}
