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
    private static final double BM25_SIMILARITY_THRESHOLD = 0.14;
    private static final double BM25_K1 = 1.2;
    private static final double BM25_B = 0.75;

    public List<TopicCluster> cluster(List<Article> articles) {
        CorpusStats corpus = corpusStats(articles);
        List<ClusterGroup> groups = new ArrayList<>();
        for (Article article : articles) {
            ArticleSignal signal = signalFor(article, corpus);
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

    private ArticleSignal signalFor(Article article, CorpusStats corpus) {
        Map<String, Integer> frequencies = tokenFrequencies(article);
        Set<String> tokens = frequencies.keySet();
        return new ArticleSignal(anchorKey(tokens), tokens, bm25Vector(frequencies, corpus));
    }

    private Set<String> tokens(Article article) {
        return tokenFrequencies(article).keySet();
    }

    private Map<String, Integer> tokenFrequencies(Article article) {
        String text = (safe(article.getTitle()) + " " + safe(article.getExcerpt())).toLowerCase(Locale.ROOT);
        String[] words = text.replaceAll("[^a-z0-9가-힣\\s-]", " ").split("\\s+");
        Map<String, Integer> frequencies = new LinkedHashMap<>();
        for (String word : words) {
            String token = normalize(word);
            if (token.length() < 3 || STOP_WORDS.contains(token)) {
                continue;
            }
            frequencies.merge(token, 1, Integer::sum);
        }
        return frequencies;
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

    private CorpusStats corpusStats(List<Article> articles) {
        Map<String, Integer> documentFrequency = new HashMap<>();
        int totalLength = 0;
        for (Article article : articles) {
            Set<String> tokens = tokens(article);
            totalLength += tokens.size();
            for (String token : tokens) {
                documentFrequency.merge(token, 1, Integer::sum);
            }
        }
        double averageLength = articles.isEmpty() ? 1.0 : Math.max(1.0, (double) totalLength / articles.size());
        return new CorpusStats(Math.max(1, articles.size()), averageLength, documentFrequency);
    }

    private Map<String, Double> bm25Vector(Map<String, Integer> frequencies, CorpusStats corpus) {
        Map<String, Double> vector = new LinkedHashMap<>();
        int documentLength = Math.max(1, frequencies.values().stream().mapToInt(Integer::intValue).sum());
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            int documentFrequency = corpus.documentFrequency().getOrDefault(entry.getKey(), 0);
            double idf = Math.log(1.0 + (corpus.documentCount() - documentFrequency + 0.5) / (documentFrequency + 0.5));
            double denominator = entry.getValue() + BM25_K1 * (1.0 - BM25_B + BM25_B * documentLength / corpus.averageDocumentLength());
            double weight = idf * (entry.getValue() * (BM25_K1 + 1.0)) / denominator;
            vector.put(entry.getKey(), weight);
        }
        return vector;
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

    private static double weightedSimilarity(Map<String, Double> first, Map<String, Double> second) {
        if (first.isEmpty() || second.isEmpty()) {
            return 0;
        }
        double dot = 0;
        for (Map.Entry<String, Double> entry : first.entrySet()) {
            dot += entry.getValue() * second.getOrDefault(entry.getKey(), 0.0);
        }
        double firstNorm = norm(first);
        double secondNorm = norm(second);
        if (firstNorm == 0 || secondNorm == 0) {
            return 0;
        }
        return dot / (firstNorm * secondNorm);
    }

    private static double norm(Map<String, Double> vector) {
        double sum = 0;
        for (double value : vector.values()) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private record CorpusStats(int documentCount, double averageDocumentLength, Map<String, Integer> documentFrequency) {
    }

    private record ArticleSignal(String anchorKey, Set<String> tokens, Map<String, Double> weights) {
    }

    private static final class ClusterGroup {
        private final String category;
        private final List<Article> articles = new ArrayList<>();
        private String anchorKey;
        private final Set<String> tokens = new LinkedHashSet<>();
        private final Map<String, Double> weights = new LinkedHashMap<>();

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
            return similarity(tokens, signal.tokens()) >= SIMILARITY_THRESHOLD
                    || weightedSimilarity(weights, signal.weights()) >= BM25_SIMILARITY_THRESHOLD;
        }

        private void add(Article article, ArticleSignal signal) {
            articles.add(article);
            if (anchorKey.isBlank() && !signal.anchorKey().isBlank()) {
                anchorKey = signal.anchorKey();
            }
            tokens.addAll(signal.tokens());
            for (Map.Entry<String, Double> entry : signal.weights().entrySet()) {
                weights.merge(entry.getKey(), entry.getValue(), Math::max);
            }
        }
    }
}
