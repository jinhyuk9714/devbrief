package com.devbrief.briefing;

import com.devbrief.domain.Article;
import com.devbrief.domain.TopicCluster;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
public class ClusterScoringService {
    private static final List<String> HIGH_SIGNAL_TERMS = List.of(
            "ai", "agent", "agents", "mcp", "model", "models", "security", "github",
            "open-source", "opensource", "kubernetes", "postgres", "redis", "browser", "workflow"
    );
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "from", "into", "onto", "over", "under", "across", "that",
            "this", "these", "those", "their", "there", "about", "after", "before", "using", "uses",
            "use", "new", "adds", "add", "added", "improves", "improve", "better", "update", "updates",
            "developer", "developers", "team", "teams", "tool", "tools", "agent", "agents", "model",
            "models", "news", "issue", "monthly", "company", "launches", "helps", "help", "changes"
    );
    private static final double SIMILARITY_THRESHOLD = 0.34;

    public List<TopicCluster> cluster(List<Article> articles) {
        List<ClusterGroup> groups = new ArrayList<>();
        for (Article article : articles) {
            ArticleSignal signal = signalFor(article);
            Optional<ClusterGroup> match = groups.stream()
                    .filter(group -> group.matches(article, signal))
                    .findFirst();
            if (match.isPresent()) {
                match.get().add(article, signal);
            } else {
                groups.add(new ClusterGroup(article, signal));
            }
        }

        return groups.stream()
                .map(group -> TopicCluster.create(titleFor(group.articles), group.category, score(group.articles), group.articles))
                .sorted(Comparator.comparingInt(TopicCluster::getScore).reversed()
                        .thenComparing(TopicCluster::getLastSeenAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private ArticleSignal signalFor(Article article) {
        Set<String> tokens = tokens(article);
        return new ArticleSignal(anchorKey(tokens), tokens);
    }

    private Set<String> tokens(Article article) {
        String text = (safe(article.getTitle()) + " " + safe(article.getExcerpt())).toLowerCase(Locale.ROOT);
        String[] words = text.replaceAll("[^a-z0-9가-힣\\s-]", " ").split("\\s+");
        Set<String> tokens = new LinkedHashSet<>();
        for (String word : words) {
            String token = normalize(word);
            if (token.length() < 3 || STOP_WORDS.contains(token)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private String normalize(String value) {
        String token = value.trim();
        if (token.length() > 4 && token.endsWith("ies")) {
            return token.substring(0, token.length() - 3) + "y";
        }
        if (token.length() > 4 && token.endsWith("es")) {
            return token.substring(0, token.length() - 2);
        }
        if (token.length() > 4 && token.endsWith("s")) {
            return token.substring(0, token.length() - 1);
        }
        return token;
    }

    private String anchorKey(Set<String> tokens) {
        if (tokens.contains("mcp")) {
            return "mcp";
        }
        if (tokens.contains("browser") && tokens.contains("trace") && tokens.contains("replay")) {
            return "browser-trace-replay";
        }
        if (tokens.contains("github") && tokens.contains("action") && tokens.contains("provenance")) {
            return "github-actions-provenance";
        }
        if (tokens.contains("vector") && (tokens.contains("indexing") || tokens.contains("cache"))) {
            return "vector-data";
        }
        if (tokens.contains("secret") && (tokens.contains("scanning") || tokens.contains("scan"))) {
            return "secret-scanning";
        }
        if (tokens.contains("postgres") && tokens.contains("vector")) {
            return "postgres-vector";
        }
        if (tokens.contains("edge") && tokens.contains("cache")) {
            return "edge-cache";
        }
        return "";
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

    private static double similarity(Set<String> first, Set<String> second) {
        if (first.isEmpty() || second.isEmpty()) {
            return 0;
        }
        int intersection = 0;
        for (String token : first) {
            if (second.contains(token)) {
                intersection++;
            }
        }
        int union = first.size() + second.size() - intersection;
        return union == 0 ? 0 : (double) intersection / union;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private record ArticleSignal(String anchorKey, Set<String> tokens) {
    }

    private static final class ClusterGroup {
        private final String category;
        private final List<Article> articles = new ArrayList<>();
        private String anchorKey;
        private final Set<String> tokens = new LinkedHashSet<>();

        private ClusterGroup(Article article, ArticleSignal signal) {
            this.category = article.getSource().getCategory();
            this.anchorKey = signal.anchorKey();
            add(article, signal);
        }

        private boolean matches(Article article, ArticleSignal signal) {
            if (!category.equals(article.getSource().getCategory())) {
                return false;
            }
            if (!anchorKey.isBlank() && anchorKey.equals(signal.anchorKey())) {
                return true;
            }
            return similarity(tokens, signal.tokens()) >= SIMILARITY_THRESHOLD;
        }

        private void add(Article article, ArticleSignal signal) {
            articles.add(article);
            if (anchorKey.isBlank() && !signal.anchorKey().isBlank()) {
                anchorKey = signal.anchorKey();
            }
            tokens.addAll(signal.tokens());
        }
    }
}
