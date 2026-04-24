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
                                        JSON 필드: summary, whyItMatters, keyPoints, actionItems, riskNotes.
                                        원문 제목과 출처를 근거로 무슨 일인지, 왜 중요한지, 개발자가 해볼 일을 분리하세요.
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
                textArray(briefing, "keyPoints"),
                textArray(briefing, "actionItems"),
                textArray(briefing, "riskNotes")
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

    private static List<String> textArray(JsonNode node, String field) {
        List<String> values = new ArrayList<>();
        for (JsonNode item : node.path(field)) {
            if (item.isTextual() && !item.asText().isBlank()) {
                values.add(item.asText());
            }
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException(field + " 목록이 비어 있습니다.");
        }
        return values;
    }
}
