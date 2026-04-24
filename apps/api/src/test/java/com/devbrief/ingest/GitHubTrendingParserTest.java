package com.devbrief.ingest;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubTrendingParserTest {

    @Test
    void parsesTrendingRepositoriesIntoArticleSignals() {
        String html = """
                <html>
                  <body>
                    <article class="Box-row">
                      <h2>
                        <a href="/openai/codex">
                          openai / codex
                        </a>
                      </h2>
                      <p>Cloud coding agent for developer workflows.</p>
                    </article>
                    <article class="Box-row">
                      <h2>
                        <a href="/modelcontextprotocol/servers">
                          modelcontextprotocol / servers
                        </a>
                      </h2>
                      <p>Reference MCP servers for production tool integrations.</p>
                    </article>
                  </body>
                </html>
                """;

        GitHubTrendingParser parser = new GitHubTrendingParser();

        var articles = parser.parse(html, 7L, "Open Source");

        assertThat(articles).hasSize(2);
        assertThat(articles.getFirst().title()).isEqualTo("openai/codex");
        assertThat(articles.getFirst().url()).isEqualTo("https://github.com/openai/codex");
        assertThat(articles.getFirst().author()).isEqualTo("openai");
        assertThat(articles.getFirst().excerpt()).contains("Cloud coding agent");
    }

    @Test
    void parsesRealisticTrendingFixture() throws Exception {
        String html = new String(Objects.requireNonNull(getClass().getResourceAsStream("/fixtures/github-trending.html")).readAllBytes(), StandardCharsets.UTF_8);

        GitHubTrendingParser parser = new GitHubTrendingParser();

        var articles = parser.parse(html, 8L, "Open Source");

        assertThat(articles).hasSize(2);
        assertThat(articles.getFirst().title()).isEqualTo("browser-tools/trace-replay");
        assertThat(articles.getFirst().url()).isEqualTo("https://github.com/browser-tools/trace-replay");
        assertThat(articles.getFirst().excerpt()).contains("Replay browser automation traces");
        assertThat(articles.get(1).title()).isEqualTo("secure-ai/provenance-actions");
    }
}
