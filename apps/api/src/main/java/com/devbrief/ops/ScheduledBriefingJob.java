package com.devbrief.ops;

import com.devbrief.briefing.BriefingService;
import com.devbrief.ingest.IngestionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledBriefingJob {
    private final IngestionService ingestionService;
    private final BriefingService briefingService;
    private final boolean enabled;

    public ScheduledBriefingJob(IngestionService ingestionService,
                                BriefingService briefingService,
                                @Value("${devbrief.scheduler.enabled:true}") boolean enabled) {
        this.ingestionService = ingestionService;
        this.briefingService = briefingService;
        this.enabled = enabled;
    }

    @Scheduled(cron = "${devbrief.scheduler.cron:0 0 8,18 * * *}", zone = "${devbrief.scheduler.zone:Asia/Seoul}")
    public void runScheduledRefresh() {
        if (!enabled) {
            return;
        }
        IngestionService.IngestionResult result = ingestionService.run();
        if (result.sourcesChecked() == 0 && result.failedSources().contains("수집 작업")) {
            return;
        }
        briefingService.generate();
    }
}
