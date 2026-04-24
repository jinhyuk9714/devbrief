package com.devbrief.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByContentHash(String contentHash);

    List<Article> findTop80ByPublishedAtAfterOrderByPublishedAtDesc(Instant since);

    List<Article> findTop80ByOrderByPublishedAtDesc();
}

