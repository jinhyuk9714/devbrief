package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.Source;
import com.devbrief.domain.TopicCluster;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenAiResponsesClientTest {

    @Test
    void parsesResponsesApiOutputTextJson() throws Exception {
        String responseBody = """
                {
                  "output": [
                    {
                      "type": "message",
                      "content": [
                        {
                          "type": "output_text",
                          "text": "{\\"summary\\":\\"요약\\",\\"whyItMatters\\":\\"중요한 이유\\",\\"keyPoints\\":[\\"핵심 1\\",\\"핵심 2\\"],\\"actionItems\\":[\\"실행 1\\",\\"실행 2\\"],\\"riskNotes\\":[\\"주의 1\\"]}"
                        }
                      ]
                    }
                  ]
                }
                """;

        GeneratedBriefing generated = OpenAiResponsesClient.parseGeneratedBriefing(responseBody);

        assertThat(generated.summary()).isEqualTo("요약");
        assertThat(generated.whyItMatters()).isEqualTo("중요한 이유");
        assertThat(generated.keyPoints()).containsExactly("핵심 1", "핵심 2");
        assertThat(generated.actionItems()).containsExactly("실행 1", "실행 2");
        assertThat(generated.riskNotes()).containsExactly("주의 1");
    }

    @Test
    void rejectsUnderSpecifiedJsonBriefings() {
        String responseBody = """
                {
                  "output_text": "{\\"summary\\":\\"요약\\",\\"whyItMatters\\":\\"중요한 이유\\",\\"keyPoints\\":[\\"핵심 1\\"],\\"actionItems\\":[\\"실행 1\\"],\\"riskNotes\\":[\\"주의 1\\"]}"
                }
                """;

        assertThatThrownBy(() -> OpenAiResponsesClient.parseGeneratedBriefing(responseBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("keyPoints");
    }

    @Test
    void acceptsKoreanGroundedBriefingsWhenRequestContextIsProvided() throws Exception {
        String responseBody = responseBody("""
                {"summary":"Hacker News의 'MCP server adoption rises across coding agents'는 코딩 에이전트들이 MCP 서버를 공통 도구 계층으로 받아들이는 흐름을 보여줍니다.","whyItMatters":"개발팀은 에이전트가 내부 도구에 접근하는 권한과 감사 로그를 다시 설계해야 하므로 중요합니다.","keyPoints":["Hacker News 원문 제목 'MCP server adoption rises across coding agents'가 같은 도구 계층 신호를 제공합니다.","excerpt의 'common tool layer for coding agents' 단서가 도구 표준화 흐름을 뒷받침합니다."],"actionItems":["현재 내부 API 하나를 read-only MCP server로 감싸 권한 범위를 기록하세요.","에이전트 도구 호출 로그에 source, 권한, 실패 사유 필드를 추가하세요."],"riskNotes":["초기 트렌드는 실제 SDK 안정성과 다를 수 있으므로 원문 날짜와 저장소 상태를 함께 확인하세요."]}
                """);

        GeneratedBriefing generated = OpenAiResponsesClient.parseGeneratedBriefing(responseBody, request());

        assertThat(generated.summary()).contains("Hacker News", "MCP server adoption rises across coding agents");
        assertThat(generated.actionItems()).allSatisfy(action -> assertThat(action).containsPattern("[가-힣]"));
    }

    @Test
    void rejectsEnglishOnlyBriefingsWhenRequestContextIsProvided() {
        String responseBody = responseBody("""
                {"summary":"Hacker News reports MCP server adoption across coding agents.","whyItMatters":"Developer teams need to rethink how agents call internal tools.","keyPoints":["Hacker News mentions MCP server adoption.","The excerpt says MCP is a common tool layer."],"actionItems":["Wrap one internal API as a read-only MCP server.","Add audit logs for tool calls."],"riskNotes":["Early trends may not reflect stable SDK behavior."]}
                """);

        assertThatThrownBy(() -> OpenAiResponsesClient.parseGeneratedBriefing(responseBody, request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("한국어");
    }

    @Test
    void rejectsGenericBriefingsWithoutOriginalEvidence() {
        String responseBody = responseBody("""
                {"summary":"이 소식은 개발자에게 중요한 변화입니다.","whyItMatters":"팀의 기술 선택과 실험 우선순위에 영향을 줄 수 있기 때문입니다.","keyPoints":["여러 출처에서 비슷한 흐름이 보입니다.","개발자는 최신 동향을 확인해야 합니다."],"actionItems":["공식 문서를 읽고 현재 스택에 미치는 영향을 검토하세요.","작은 실험을 만들어 장단점을 기록하세요."],"riskNotes":["초기 발표는 실제 운영 안정성과 다를 수 있습니다."]}
                """);

        assertThatThrownBy(() -> OpenAiResponsesClient.parseGeneratedBriefing(responseBody, request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원문");
    }

    @Test
    void rejectsTranslatedSourceOrTitleWhenRequestContextIsProvided() {
        String responseBody = responseBody("""
                {"summary":"해커뉴스의 '코딩 에이전트 전반의 MCP 서버 도입'은 에이전트 도구 표준화 흐름을 보여줍니다.","whyItMatters":"개발팀은 내부 도구 권한과 감사 로그를 다시 설계해야 하므로 중요합니다.","keyPoints":["해커뉴스 원문이 MCP 도구 계층 확산을 다룹니다.","excerpt의 common tool layer 단서가 흐름을 뒷받침합니다."],"actionItems":["내부 API 하나를 read-only MCP server로 감싸 권한 범위를 기록하세요.","도구 호출 로그에 권한과 실패 사유 필드를 추가하세요."],"riskNotes":["초기 트렌드는 실제 SDK 안정성과 다를 수 있습니다."]}
                """);

        assertThatThrownBy(() -> OpenAiResponsesClient.parseGeneratedBriefing(responseBody, request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("원문");
    }

    private static OpenAiSummaryRequest request() {
        Source source = Source.create("Hacker News", "RSS", "https://news.ycombinator.com/rss", "Open Source");
        Article article = Article.create(source, "MCP server adoption rises across coding agents", "https://example.com/mcp", "HN",
                Instant.parse("2026-04-24T09:00:00Z"), "MCP servers are becoming the common tool layer for coding agents.", "hash-openai-1");
        TopicCluster cluster = TopicCluster.create("MCP server adoption rises across coding agents", "Open Source", 88, List.of(article));
        return new OpenAiSummaryRequest("gpt-4o-mini", cluster, cluster.getArticles());
    }

    private static String responseBody(String outputText) {
        return """
                {
                  "output_text": %s
                }
                """.formatted(toJsonString(outputText));
    }

    private static String toJsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
