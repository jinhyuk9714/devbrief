package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.TopicCluster;

import java.util.List;

public interface SummaryProvider {
    GeneratedBriefing generate(TopicCluster cluster, List<Article> articles);
}

