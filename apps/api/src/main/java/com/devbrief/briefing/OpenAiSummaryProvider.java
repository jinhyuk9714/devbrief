package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.TopicCluster;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Primary
@Component
public class OpenAiSummaryProvider implements SummaryProvider {
    private final OpenAiBriefingClient client;
    private final DeterministicSummaryProvider fallback;
    private final String apiKey;
    private final String model;

    public OpenAiSummaryProvider(OpenAiBriefingClient client,
                                 DeterministicSummaryProvider fallback,
                                 @Value("${devbrief.openai.api-key:}") String apiKey,
                                 @Value("${devbrief.openai.model:gpt-4o-mini}") String model) {
        this.client = client;
        this.fallback = fallback;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public GeneratedBriefing generate(TopicCluster cluster, List<Article> articles) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback.generate(cluster, articles);
        }
        try {
            return client.generate(new OpenAiSummaryRequest(model, cluster, articles));
        } catch (Exception ignored) {
            return fallback.generate(cluster, articles);
        }
    }
}
