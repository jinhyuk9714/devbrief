package com.devbrief.briefing;

import com.devbrief.domain.*;
import com.devbrief.ops.RedisGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class BriefingService {
    private final ArticleRepository articleRepository;
    private final TopicClusterRepository clusterRepository;
    private final BriefingRepository briefingRepository;
    private final ClusterScoringService clusterScoringService;
    private final SummaryProvider summaryProvider;
    private final RedisGateway redisGateway;

    public BriefingService(ArticleRepository articleRepository,
                           TopicClusterRepository clusterRepository,
                           BriefingRepository briefingRepository,
                           ClusterScoringService clusterScoringService,
                           SummaryProvider summaryProvider,
                           RedisGateway redisGateway) {
        this.articleRepository = articleRepository;
        this.clusterRepository = clusterRepository;
        this.briefingRepository = briefingRepository;
        this.clusterScoringService = clusterScoringService;
        this.summaryProvider = summaryProvider;
        this.redisGateway = redisGateway;
    }

    @Transactional
    public GenerationResult generate() {
        List<Article> articles = articleRepository.findTop80ByPublishedAtAfterOrderByPublishedAtDesc(Instant.now().minus(Duration.ofDays(7)));
        if (articles.isEmpty()) {
            articles = articleRepository.findTop80ByOrderByPublishedAtDesc();
        }

        briefingRepository.deleteAllInBatch();
        clusterRepository.deleteAllInBatch();

        List<TopicCluster> clusters = clusterScoringService.cluster(articles).stream().limit(12).toList();
        int count = 0;
        for (TopicCluster cluster : clusters) {
            TopicCluster saved = clusterRepository.save(cluster);
            GeneratedBriefing generated = summaryProvider.generate(saved, saved.getArticles());
            briefingRepository.save(Briefing.create(
                    saved,
                    generated.summary(),
                    generated.whyItMatters(),
                    generated.keyPoints(),
                    generated.actionItems(),
                    generated.riskNotes(),
                    Math.max(3, Math.min(7, 2 + saved.getArticleCount()))
            ));
            count++;
        }
        redisGateway.put("devbrief:cache:today:last-generated", Instant.now().toString(), Duration.ofHours(6));
        return new GenerationResult(count);
    }

    @Transactional(readOnly = true)
    public List<Briefing> today() {
        return briefingRepository.findTop5ByOrderByClusterScoreDescGeneratedAtDesc();
    }

    @Transactional(readOnly = true)
    public Briefing detail(Long id) {
        return briefingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Briefing not found"));
    }

    @Transactional(readOnly = true)
    public List<TopicCluster> trends() {
        return clusterRepository.findTop20ByOrderByScoreDescLastSeenAtDesc();
    }

    public record GenerationResult(int briefingsGenerated) {
    }
}

