package com.devbrief.bootstrap;

import com.devbrief.briefing.BriefingService;
import com.devbrief.domain.ArticleRepository;
import com.devbrief.domain.BriefingRepository;
import com.devbrief.ingest.IngestionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataInitializer implements ApplicationRunner {
    private final IngestionService ingestionService;
    private final BriefingService briefingService;
    private final ArticleRepository articleRepository;
    private final BriefingRepository briefingRepository;
    private final boolean seedOnStartup;

    public DemoDataInitializer(IngestionService ingestionService,
                               BriefingService briefingService,
                               ArticleRepository articleRepository,
                               BriefingRepository briefingRepository,
                               @Value("${devbrief.demo.seed-on-startup:true}") boolean seedOnStartup) {
        this.ingestionService = ingestionService;
        this.briefingService = briefingService;
        this.articleRepository = articleRepository;
        this.briefingRepository = briefingRepository;
        this.seedOnStartup = seedOnStartup;
    }

    @Override
    public void run(ApplicationArguments args) {
        ingestionService.seedSources();
        if (!seedOnStartup) {
            return;
        }
        if (articleRepository.count() == 0) {
            ingestionService.run();
        }
        if (briefingRepository.count() == 0) {
            briefingService.generate();
        }
    }
}

