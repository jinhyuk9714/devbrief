package com.devbrief.api;

import com.devbrief.DevBriefApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = DevBriefApplication.class, properties = "devbrief.admin-token=test-token")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerAuthTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void rejectsAdminMutationWhenConfiguredTokenIsMissingOrWrong() throws Exception {
        mockMvc.perform(post("/api/admin/ingest/run"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("관리 토큰이 필요합니다")));

        mockMvc.perform(post("/api/admin/briefings/generate")
                        .header("X-Admin-Token", "wrong-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("관리 토큰이 필요합니다")));
    }

    @Test
    void allowsAdminMutationWithConfiguredToken() throws Exception {
        mockMvc.perform(post("/api/admin/ingest/run")
                        .header("X-Admin-Token", "test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourcesChecked", greaterThanOrEqualTo(1)));
    }
}
