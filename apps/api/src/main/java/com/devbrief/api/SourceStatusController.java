package com.devbrief.api;

import com.devbrief.domain.SourceRepository;
import com.devbrief.i18n.KoreanDisplayText;
import com.devbrief.ops.RedisGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sources")
public class SourceStatusController {
    private final SourceRepository sourceRepository;
    private final RedisGateway redisGateway;
    private final KoreanDisplayText displayText;

    public SourceStatusController(SourceRepository sourceRepository, RedisGateway redisGateway, KoreanDisplayText displayText) {
        this.sourceRepository = sourceRepository;
        this.redisGateway = redisGateway;
        this.displayText = displayText;
    }

    @GetMapping("/status")
    public SourceStatusResponse status() {
        List<SourceStatusItem> sources = sourceRepository.findByEnabledTrueOrderByNameAsc().stream()
                .map(source -> new SourceStatusItem(
                        source.getName(),
                        source.getType(),
                        displayText.category(source.getCategory()),
                        source.isEnabled(),
                        source.getLastFetchedAt(),
                        source.getLastFetchStatus(),
                        source.getLastFetchMessage(),
                        source.getLastArticleCount(),
                        source.isLastUsedFallback()
                ))
                .toList();
        return new SourceStatusResponse(sources, redisGateway.status());
    }

    public record SourceStatusResponse(List<SourceStatusItem> sources, Map<String, Object> cache) {
    }

    public record SourceStatusItem(
            String name,
            String type,
            String category,
            boolean enabled,
            Instant lastFetchedAt,
            com.devbrief.domain.SourceFetchStatus lastFetchStatus,
            String lastFetchMessage,
            int lastArticleCount,
            boolean lastUsedFallback
    ) {
    }
}
