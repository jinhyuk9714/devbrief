package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.TopicCluster;
import com.devbrief.i18n.KoreanDisplayText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeterministicSummaryProvider implements SummaryProvider {
    private final KoreanDisplayText displayText;

    DeterministicSummaryProvider() {
        this(new KoreanDisplayText());
    }

    @Autowired
    public DeterministicSummaryProvider(KoreanDisplayText displayText) {
        this.displayText = displayText;
    }

    @Override
    public GeneratedBriefing generate(TopicCluster cluster, List<Article> articles) {
        String summary = displayText.summary(cluster.getTitle(), cluster.getCategory(), null);
        String why = displayText.whyItMatters(cluster.getCategory(), null);
        List<String> keyPoints = displayText.keyPoints(articles, List.of());
        List<String> actions = displayText.actionItems(cluster.getCategory());
        List<String> risks = displayText.riskNotes();
        return new GeneratedBriefing(summary, why, keyPoints, actions, risks);
    }
}
