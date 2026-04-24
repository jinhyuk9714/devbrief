package com.devbrief.briefing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> OpenAiResponsesClient.parseGeneratedBriefing(responseBody))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("keyPoints");
    }
}
