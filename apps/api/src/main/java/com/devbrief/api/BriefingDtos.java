package com.devbrief.api;

import com.devbrief.domain.Article;
import com.devbrief.domain.Briefing;
import com.devbrief.domain.TopicCluster;
import com.devbrief.i18n.KoreanDisplayText;

import java.time.Instant;
import java.util.ArrayList;
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
            int articleCount,
            String summary,
            String whyItMatters,
            List<String> actionItems
    ) {
        static BriefingSummary from(Briefing briefing, KoreanDisplayText displayText) {
            TopicCluster cluster = briefing.getCluster();
            List<Article> articles = articlesForDisplay(cluster);
            return new BriefingSummary(
                    briefing.getId(),
                    displayText.briefingTitle(cluster.getTitle()),
                    displayText.category(cluster.getCategory()),
                    cluster.getScore(),
                    briefing.getReadingMinutes(),
                    (int) articles.stream().map(article -> article.getSource().getName()).distinct().count(),
                    cluster.getArticleCount(),
                    displayText.summary(cluster.getTitle(), cluster.getCategory(), articles, briefing.getSummary()),
                    displayText.whyItMatters(cluster.getCategory(), articles, briefing.getWhyItMatters()),
                    displayText.actionItems(cluster.getCategory(), articles, briefing.getActionItems())
            );
        }
    }

    public record BriefingDetail(
            Long id,
            String title,
            String category,
            int importance,
            int readingMinutes,
            int sourceCount,
            int articleCount,
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
            List<Article> articles = articlesForDisplay(cluster);
            List<ArticleLink> sources = articles.stream().map(ArticleLink::from).toList();
            List<TimelineItem> timeline = articles.stream()
                    .map(article -> new TimelineItem(article.getPublishedAt(), article.getSource().getName(), article.getTitle()))
                    .toList();
            return new BriefingDetail(
                    briefing.getId(),
                    displayText.briefingTitle(cluster.getTitle()),
                    displayText.category(cluster.getCategory()),
                    cluster.getScore(),
                    briefing.getReadingMinutes(),
                    (int) articles.stream().map(article -> article.getSource().getName()).distinct().count(),
                    cluster.getArticleCount(),
                    displayText.summary(cluster.getTitle(), cluster.getCategory(), articles, briefing.getSummary()),
                    displayText.whyItMatters(cluster.getCategory(), articles, briefing.getWhyItMatters()),
                    displayText.keyPoints(articles, briefing.getKeyPoints()),
                    displayText.actionItems(cluster.getCategory(), articles, briefing.getActionItems()),
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

    private static List<Article> articlesForDisplay(TopicCluster cluster) {
        List<Article> articles = new ArrayList<>(cluster.getArticles());
        int representativeIndex = -1;
        for (int index = 0; index < articles.size(); index++) {
            if (articles.get(index).getTitle().equals(cluster.getTitle())) {
                representativeIndex = index;
                break;
            }
        }
        if (representativeIndex <= 0) {
            return articles;
        }
        Article representative = articles.remove(representativeIndex);
        articles.add(0, representative);
        return articles;
    }
}
