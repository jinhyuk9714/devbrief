package com.devbrief.i18n;

import org.junit.jupiter.api.Test;

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
}
