package com.devbrief.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "sources")
public class Source {
    private static final int FETCH_MESSAGE_MAX_LENGTH = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean enabled;

    private Instant lastFetchedAt;

    @Enumerated(EnumType.STRING)
    private SourceFetchStatus lastFetchStatus;

    @Column(length = 1000)
    private String lastFetchMessage;

    private Integer lastArticleCount;

    private Boolean lastUsedFallback;

    protected Source() {
    }

    private Source(String name, String type, String url, String category) {
        this.name = name;
        this.type = type;
        this.url = url;
        this.category = category;
        this.enabled = true;
    }

    public static Source create(String name, String type, String url, String category) {
        return new Source(name, type, url, category);
    }

    public void markFetched(Instant fetchedAt) {
        this.lastFetchedAt = fetchedAt;
    }

    public void markFetchResult(SourceFetchStatus status, String message, int articleCount, boolean usedFallback, Instant fetchedAt) {
        this.lastFetchStatus = status;
        this.lastFetchMessage = truncate(message, FETCH_MESSAGE_MAX_LENGTH);
        this.lastArticleCount = articleCount;
        this.lastUsedFallback = usedFallback;
        this.lastFetchedAt = fetchedAt;
    }

    public void syncCatalogMetadata(String type, String url, String category) {
        this.type = type;
        this.url = url;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getCategory() {
        return category;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Instant getLastFetchedAt() {
        return lastFetchedAt;
    }

    public SourceFetchStatus getLastFetchStatus() {
        return lastFetchStatus;
    }

    public String getLastFetchMessage() {
        return lastFetchMessage;
    }

    public int getLastArticleCount() {
        return lastArticleCount == null ? 0 : lastArticleCount;
    }

    public boolean isLastUsedFallback() {
        return Boolean.TRUE.equals(lastUsedFallback);
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
