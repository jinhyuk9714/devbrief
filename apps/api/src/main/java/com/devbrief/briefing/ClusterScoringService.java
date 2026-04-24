package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.TopicCluster;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClusterScoringService {
    private static final List<String> HIGH_SIGNAL_TERMS = List.of(
            "ai", "agent", "agents", "mcp", "model", "models", "security", "github",
            "open-source", "opensource", "kubernetes", "postgres", "redis", "browser", "workflow"
    );

    public List<TopicCluster> cluster(List<Article> articles) {
        Map<String, List<Article>> grouped = articles.stream()
                .collect(Collectors.groupingBy(this::clusterKey, LinkedHashMap::new, Collectors.toList()));

        return grouped.values().stream()
                .map(group -> TopicCluster.create(titleFor(group), group.get(0).getSource().getCategory(), score(group), group))
                .sorted(Comparator.comparingInt(TopicCluster::getScore).reversed()
                        .thenComparing(TopicCluster::getLastSeenAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private String clusterKey(Article article) {
        String text = (article.getTitle() + " " + article.getExcerpt()).toLowerCase(Locale.ROOT);
        if (text.contains("mcp")) {
            return article.getSource().getCategory() + ":mcp";
        }
        if (text.contains("agent")) {
            return article.getSource().getCategory() + ":agent";
        }
        if (text.contains("model")) {
            return article.getSource().getCategory() + ":model";
        }
        String[] words = text.replaceAll("[^a-z0-9가-힣\\s-]", " ").split("\\s+");
        return article.getSource().getCategory() + ":" + Arrays.stream(words)
                .filter(word -> word.length() > 3)
                .limit(2)
                .collect(Collectors.joining("-"));
    }

    private String titleFor(List<Article> group) {
        return group.stream()
                .max(Comparator.comparingInt(article -> signalCount(article.getTitle() + " " + article.getExcerpt())))
                .map(Article::getTitle)
                .orElse("Developer news update");
    }

    private int score(List<Article> group) {
        int base = 42;
        int volume = Math.min(group.size() * 18, 36);
        int signal = Math.min(group.stream().mapToInt(article -> signalCount(article.getTitle() + " " + article.getExcerpt())).sum() * 8, 32);
        int recency = group.stream()
                .map(Article::getPublishedAt)
                .filter(Objects::nonNull)
                .mapToInt(published -> Duration.between(published, Instant.now()).toHours() <= 48 ? 12 : 3)
                .max()
                .orElse(3);
        return Math.min(100, base + volume + signal + recency);
    }

    private int signalCount(String value) {
        String lowered = value.toLowerCase(Locale.ROOT);
        int count = 0;
        for (String term : HIGH_SIGNAL_TERMS) {
            if (lowered.contains(term)) {
                count++;
            }
        }
        return count;
    }
}

