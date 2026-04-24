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
        List<SourceResult> sourceResults = new ArrayList<>();
        try {
            for (Source source : sources) {
                int sourceImported = 0;
                FetchOutcome outcome = null;
                try {
                    outcome = fetch(source);
                    for (ParsedArticle candidate : outcome.articles()) {
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
                            sourceImported++;
                        }
                    }
                    source.markFetchResult(outcome.status(), outcome.message(), outcome.articles().size(), outcome.fallbackUsed(), Instant.now());
                    if (outcome.status() == SourceFetchStatus.FAILED) {
                        failed.add(source.getName());
                    }
                    sourceResults.add(new SourceResult(
                            source.getName(),
                            outcome.status(),
                            outcome.articles().size(),
                            sourceImported,
                            outcome.fallbackUsed(),
                            outcome.message()
                    ));
                } catch (Exception ex) {
                    String message = "수집 실패: " + ex.getMessage();
                    int fetchedCount = outcome == null ? 0 : outcome.articles().size();
                    source.markFetchResult(SourceFetchStatus.FAILED, message, fetchedCount, false, Instant.now());
                    failed.add(source.getName());
                    sourceResults.add(new SourceResult(
                            source.getName(),
                            SourceFetchStatus.FAILED,
                            fetchedCount,
                            sourceImported,
                            false,
                            message
                    ));
                }
            }
        } finally {
            if (lock) {
                redisGateway.release("devbrief:ingest:lock");
            }
        }
        return new IngestionResult(sources.size(), imported, failed, sourceResults);
    }

    @Transactional
    public void seedSources() {
        for (NewsSourceCatalog.SourceSpec spec : catalog.defaults()) {
            sourceRepository.findByName(spec.name())
                    .orElseGet(() -> sourceRepository.save(Source.create(spec.name(), spec.type(), spec.url(), spec.category())));
        }
    }

    private FetchOutcome fetch(Source source) {
        if (!networkEnabled || !"RSS".equalsIgnoreCase(source.getType())) {
            return new FetchOutcome(
                    catalog.demoArticlesFor(source),
                    SourceFetchStatus.DEMO,
                    true,
                    "네트워크 비활성화 또는 비RSS 출처라 데모 데이터를 사용했습니다."
            );
        }
        try {
            String body = restClient.get().uri(source.getUrl()).retrieve().body(String.class);
            if (body == null || body.isBlank()) {
                return fallback(source, "응답 본문이 비어 있어 대체 데이터를 사용했습니다.");
            }
            return new FetchOutcome(
                    rssFeedParser.parse(body, source.getId(), source.getCategory()),
                    SourceFetchStatus.OK,
                    false,
                    "RSS 수집 성공"
            );
        } catch (Exception ex) {
            return fallback(source, "RSS 수집 실패 후 대체 데이터를 사용했습니다: " + ex.getMessage());
        }
    }

    private FetchOutcome fallback(Source source, String message) {
        return new FetchOutcome(catalog.demoArticlesFor(source), SourceFetchStatus.FALLBACK, true, message);
    }

    private record FetchOutcome(List<ParsedArticle> articles, SourceFetchStatus status, boolean fallbackUsed, String message) {
    }

    public record SourceResult(
            String sourceName,
            SourceFetchStatus status,
            int fetchedCount,
            int importedCount,
            boolean fallbackUsed,
            String message
    ) {
    }

    public record IngestionResult(int sourcesChecked, int articlesImported, List<String> failedSources, List<SourceResult> sourceResults) {
    }
}
