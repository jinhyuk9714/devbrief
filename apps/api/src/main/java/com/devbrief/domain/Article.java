package com.devbrief.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "articles", indexes = {
        @Index(name = "idx_article_hash", columnList = "contentHash", unique = true),
        @Index(name = "idx_article_published", columnList = "publishedAt")
})
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Source source;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 1000)
    private String url;

    private String author;

    private Instant publishedAt;

    @Column(length = 500)
    private String excerpt;

    @Column(nullable = false, unique = true)
    private String contentHash;

    @Column(nullable = false)
    private Instant createdAt;

    protected Article() {
    }

    private Article(Source source, String title, String url, String author, Instant publishedAt, String excerpt, String contentHash) {
        this.source = source;
        this.title = title;
        this.url = url;
        this.author = author;
        this.publishedAt = publishedAt;
        this.excerpt = excerpt;
        this.contentHash = contentHash;
        this.createdAt = Instant.now();
    }

    public static Article create(Source source, String title, String url, String author, Instant publishedAt, String excerpt, String contentHash) {
        return new Article(source, title, url, author, publishedAt, excerpt, contentHash);
    }

    public Long getId() {
        return id;
    }

    public Source getSource() {
        return source;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getAuthor() {
        return author;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public String getContentHash() {
        return contentHash;
    }
}
