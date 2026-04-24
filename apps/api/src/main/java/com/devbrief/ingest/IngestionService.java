package com.devbrief.ingest;

import com.devbrief.domain.*;
import com.devbrief.ops.RedisGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {
    private final SourceRepository sourceRepository;
    private final ArticleRepository articleRepository;
    private final ContentHashService contentHashService;
    private final NewsSourceCatalog catalog;
    private final RssFeedParser rssFeedParser;
    private final RedisGateway redisGateway;
    private final RestClient restClient = RestClient.builder().build();
    private final boolean networkEnabled;

    public IngestionService(SourceRepository sourceRepository,
                            ArticleRepository articleRepository,
                            ContentHashService contentHashService,
                            NewsSourceCatalog catalog,
                            RssFeedParser rssFeedParser,
                            RedisGateway redisGateway,
                            @Value("${devbrief.ingestion.network-enabled:false}") boolean networkEnabled) {
        this.sourceRepository = sourceRepository;
        this.articleRepository = articleRepository;
        this.contentHashService = contentHashService;
        this.catalog = catalog;
        this.rssFeedParser = rssFeedParser;
        this.redisGateway = redisGateway;
        this.networkEnabled = networkEnabled;
    }

    @Transactional
    public IngestionResult run() {
        seedSources();
        boolean lock = redisGateway.tryLock("devbrief:ingest:lock", Duration.ofSeconds(45));
        List<Source> sources = sourceRepository.findByEnabledTrueOrderByNameAsc();
        int imported = 0;
        List<String> failed = new ArrayList<>();
        try {
            for (Source source : sources) {
                List<ParsedArticle> parsed = fetch(source);
                for (ParsedArticle candidate : parsed) {
                    String hash = contentHashService.hash(candidate.title(), candidate.url());
                    if (!articleRepository.existsByContentHash(hash)) {
                        articleRepository.save(Article.create(
                                source,
                                candidate.title(),
                                candidate.url(),
                                candidate.author(),
                                candidate.publishedAt(),
                                candidate.excerpt(),
                                hash
                        ));
                        imported++;
                    }
                }
                source.markFetched(Instant.now());
            }
        } catch (Exception ex) {
            failed.add(ex.getMessage());
        } finally {
            if (lock) {
                redisGateway.release("devbrief:ingest:lock");
            }
        }
        return new IngestionResult(sources.size(), imported, failed);
    }

    @Transactional
    public void seedSources() {
        for (NewsSourceCatalog.SourceSpec spec : catalog.defaults()) {
            sourceRepository.findByName(spec.name())
                    .orElseGet(() -> sourceRepository.save(Source.create(spec.name(), spec.type(), spec.url(), spec.category())));
        }
    }

    private List<ParsedArticle> fetch(Source source) {
        if (!networkEnabled || !"RSS".equalsIgnoreCase(source.getType())) {
            return catalog.demoArticlesFor(source);
        }
        try {
            String body = restClient.get().uri(source.getUrl()).retrieve().body(String.class);
            if (body == null || body.isBlank()) {
                return catalog.demoArticlesFor(source);
            }
            return rssFeedParser.parse(body, source.getId(), source.getCategory());
        } catch (Exception ignored) {
            return catalog.demoArticlesFor(source);
        }
    }

    public record IngestionResult(int sourcesChecked, int articlesImported, List<String> failedSources) {
    }
}

