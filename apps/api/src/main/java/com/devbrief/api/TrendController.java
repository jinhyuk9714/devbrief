package com.devbrief.api;

import com.devbrief.briefing.BriefingService;
import com.devbrief.domain.TopicCluster;
import com.devbrief.i18n.KoreanDisplayText;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trends")
public class TrendController {
    private final BriefingService briefingService;
    private final KoreanDisplayText displayText;

    public TrendController(BriefingService briefingService, KoreanDisplayText displayText) {
        this.briefingService = briefingService;
        this.displayText = displayText;
    }

    @GetMapping
    public TrendResponse trends(@RequestParam(defaultValue = "day") String range) {
        List<TrendItem> trends = briefingService.trends().stream()
                .map(cluster -> new TrendItem(
                        displayText.category(cluster.getCategory()),
                        displayText.briefingTitle(cluster.getTitle()),
                        cluster.getScore(),
                        cluster.getArticleCount(),
                        cluster.getLastSeenAt()
                ))
                .toList();
        Map<String, Long> categories = trends.stream()
                .collect(Collectors.groupingBy(TrendItem::category, Collectors.counting()));
        return new TrendResponse(range, trends, categories);
    }

    public record TrendResponse(String range, List<TrendItem> trends, Map<String, Long> categories) {
    }

    public record TrendItem(String category, String title, int score, int articleCount, java.time.Instant lastSeenAt) {
    }
}
