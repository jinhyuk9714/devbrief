package com.devbrief.api;

import com.devbrief.briefing.BriefingService;
import com.devbrief.domain.Briefing;
import com.devbrief.i18n.KoreanDisplayText;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/briefings")
public class BriefingController {
    private final BriefingService briefingService;
    private final KoreanDisplayText displayText;

    public BriefingController(BriefingService briefingService, KoreanDisplayText displayText) {
        this.briefingService = briefingService;
        this.displayText = displayText;
    }

    @GetMapping("/today")
    public BriefingDtos.TodayResponse today() {
        List<Briefing> briefings = briefingService.today();
        Instant generatedAt = briefings.stream().findFirst().map(Briefing::getGeneratedAt).orElse(Instant.now());
        return new BriefingDtos.TodayResponse(generatedAt, briefings.stream()
                .map(briefing -> BriefingDtos.BriefingSummary.from(briefing, displayText))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BriefingDtos.BriefingDetail> detail(@PathVariable Long id) {
        return ResponseEntity.ok(BriefingDtos.BriefingDetail.from(briefingService.detail(id), displayText));
    }
}
