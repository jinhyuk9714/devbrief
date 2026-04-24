package com.devbrief.api;

import com.devbrief.domain.Article;
import com.devbrief.domain.Briefing;
import com.devbrief.domain.TopicCluster;
import com.devbrief.i18n.KoreanDisplayText;

import java.time.Instant;
import java.util.List;

public final class BriefingDtos {
    private BriefingDtos() {
    }

    public record TodayResponse(Instant generatedAt, List<BriefingSummary> briefings) {
    }

    public record BriefingSummary(
            Long id,
            String title,
            String category,
            int importance,
            int readingMinutes,
            int sourceCount,
            String summary,
            String whyItMatters,
            List<String> actionItems
    ) {
        static BriefingSummary from(Briefing briefing, KoreanDisplayText displayText) {
            TopicCluster cluster = briefing.getCluster();
            return new BriefingSummary(
                    briefing.getId(),
                    displayText.briefingTitle(cluster.getTitle()),
                    displayText.category(cluster.getCategory()),
                    cluster.getScore(),
                    briefing.getReadingMinutes(),
                    (int) cluster.getArticles().stream().map(article -> article.getSource().getName()).distinct().count(),
                    displayText.summary(cluster.getTitle(), cluster.getCategory(), cluster.getArticles(), briefing.getSummary()),
                    displayText.whyItMatters(cluster.getCategory(), briefing.getWhyItMatters()),
                    briefing.getActionItems()
            );
        }
    }

    public record BriefingDetail(
            Long id,
            String title,
            String category,
            int importance,
            int readingMinutes,
            String summary,
            String whyItMatters,
            List<String> keyPoints,
            List<String> actionItems,
            List<String> riskNotes,
            List<ArticleLink> sources,
            List<TimelineItem> timeline
    ) {
        static BriefingDetail from(Briefing briefing, KoreanDisplayText displayText) {
            TopicCluster cluster = briefing.getCluster();
            List<ArticleLink> sources = cluster.getArticles().stream().map(ArticleLink::from).toList();
            List<TimelineItem> timeline = cluster.getArticles().stream()
                    .map(article -> new TimelineItem(article.getPublishedAt(), article.getSource().getName(), article.getTitle()))
                    .toList();
            return new BriefingDetail(
                    briefing.getId(),
                    displayText.briefingTitle(cluster.getTitle()),
                    displayText.category(cluster.getCategory()),
                    cluster.getScore(),
                    briefing.getReadingMinutes(),
                    displayText.summary(cluster.getTitle(), cluster.getCategory(), cluster.getArticles(), briefing.getSummary()),
                    displayText.whyItMatters(cluster.getCategory(), briefing.getWhyItMatters()),
                    displayText.keyPoints(cluster.getArticles(), briefing.getKeyPoints()),
                    briefing.getActionItems(),
                    briefing.getRiskNotes(),
                    sources,
                    timeline
            );
        }
    }

    public record ArticleLink(String title, String url, String sourceName, String author, Instant publishedAt, String excerpt) {
        static ArticleLink from(Article article) {
            return new ArticleLink(
                    article.getTitle(),
                    article.getUrl(),
                    article.getSource().getName(),
                    article.getAuthor(),
                    article.getPublishedAt(),
                    article.getExcerpt()
            );
        }
    }

    public record TimelineItem(Instant at, String source, String title) {
    }
}
