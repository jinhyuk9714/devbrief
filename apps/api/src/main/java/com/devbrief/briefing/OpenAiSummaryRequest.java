package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.TopicCluster;

import java.util.List;

public record OpenAiSummaryRequest(
        String model,
        TopicCluster cluster,
        List<Article> articles
) {
}
