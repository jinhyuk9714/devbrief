package com.devbrief.api;

import com.devbrief.briefing.BriefingService;
import com.devbrief.ingest.IngestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final IngestionService ingestionService;
    private final BriefingService briefingService;

    public AdminController(IngestionService ingestionService, BriefingService briefingService) {
        this.ingestionService = ingestionService;
        this.briefingService = briefingService;
    }

    @PostMapping("/ingest/run")
    public IngestionService.IngestionResult runIngestion() {
        return ingestionService.run();
    }

    @PostMapping("/briefings/generate")
    public BriefingService.GenerationResult generateBriefings() {
        return briefingService.generate();
    }
}

