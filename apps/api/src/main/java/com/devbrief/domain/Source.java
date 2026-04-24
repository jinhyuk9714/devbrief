package com.devbrief.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "sources")
public class Source {
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
}

