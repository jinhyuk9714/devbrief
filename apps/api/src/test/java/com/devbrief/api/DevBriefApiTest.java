package com.devbrief.api;

import com.devbrief.DevBriefApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DevBriefApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DevBriefApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void exposesTodayBriefingsAndDetails() throws Exception {
        generateBriefings();

        mockMvc.perform(get("/api/briefings/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.briefings.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.briefings[0].category").value("오픈소스"))
                .andExpect(jsonPath("$.briefings[0].summary").exists())
                .andExpect(jsonPath("$.briefings[0].whyItMatters").value(org.hamcrest.Matchers.containsString("오픈소스")))
                .andExpect(jsonPath("$.briefings[0].sourceCount", greaterThanOrEqualTo(1)));
    }

    @Test
    void exposesTrendAndSourceStatus() throws Exception {
        generateBriefings();

        mockMvc.perform(get("/api/trends?range=week"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.range").value("week"))
                .andExpect(jsonPath("$.trends[0].category").value("AI 모델"));

        mockMvc.perform(get("/api/sources/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sources.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.sources[0].category").value("AI 모델"))
                .andExpect(jsonPath("$.sources[0].lastFetchStatus").value("DEMO"))
                .andExpect(jsonPath("$.sources[0].lastArticleCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.sources[0].lastUsedFallback").value(true))
                .andExpect(jsonPath("$.cache.redis").exists());
    }

    @Test
    void allowsLocalhostAndLoopbackFrontendOrigins() throws Exception {
        mockMvc.perform(options("/api/admin/ingest/run")
                        .header("Origin", "http://127.0.0.1:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:3000"));
    }

    private void generateBriefings() throws Exception {
        mockMvc.perform(post("/api/admin/ingest/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.articlesImported", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.sourceResults.length()", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.sourceResults[0].sourceName").exists())
                .andExpect(jsonPath("$.sourceResults[0].status").value("DEMO"))
                .andExpect(jsonPath("$.sourceResults[0].fetchedCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.sourceResults[0].importedCount", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.sourceResults[0].fallbackUsed").value(true))
                .andExpect(jsonPath("$.sourceResults[0].message").exists());

        mockMvc.perform(post("/api/admin/briefings/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.briefingsGenerated", greaterThanOrEqualTo(1)));
    }
}
