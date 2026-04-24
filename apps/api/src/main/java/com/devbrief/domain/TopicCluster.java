package com.devbrief.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "topic_clusters")
public class TopicCluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int articleCount;

    private Instant firstSeenAt;
    private Instant lastSeenAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "topic_cluster_articles",
            joinColumns = @JoinColumn(name = "cluster_id"),
            inverseJoinColumns = @JoinColumn(name = "article_id")
    )
    private List<Article> articles = new ArrayList<>();

    protected TopicCluster() {
    }

    private TopicCluster(String title, String category, int score, List<Article> articles) {
        this.title = title;
        this.category = category;
        this.score = score;
        this.articles = new ArrayList<>(articles);
        this.articleCount = articles.size();
        this.firstSeenAt = articles.stream()
                .map(Article::getPublishedAt)
                .min(Comparator.nullsLast(Comparator.naturalOrder()))
                .orElse(Instant.now());
        this.lastSeenAt = articles.stream()
                .map(Article::getPublishedAt)
                .max(Comparator.nullsLast(Comparator.naturalOrder()))
                .orElse(Instant.now());
    }

    public static TopicCluster create(String title, String category, int score, List<Article> articles) {
        return new TopicCluster(title, category, score, articles);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public int getScore() {
        return score;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public Instant getFirstSeenAt() {
        return firstSeenAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public List<Article> getArticles() {
        return List.copyOf(articles);
    }
}

