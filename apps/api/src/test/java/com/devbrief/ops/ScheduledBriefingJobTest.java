package com.devbrief.ops;

import com.devbrief.briefing.BriefingService;
import com.devbrief.domain.SourceFetchStatus;
import com.devbrief.ingest.IngestionService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class ScheduledBriefingJobTest {

    @Test
    void runsIngestionThenGeneratesBriefingsWhenEnabled() {
        IngestionService ingestionService = mock(IngestionService.class);
        BriefingService briefingService = mock(BriefingService.class);
        when(ingestionService.run()).thenReturn(new IngestionService.IngestionResult(
                2,
                1,
                List.of(),
                List.of(new IngestionService.SourceResult("Working Feed", SourceFetchStatus.OK, 3, 1, false, "RSS 수집 성공"))
        ));

        ScheduledBriefingJob job = new ScheduledBriefingJob(ingestionService, briefingService, true);

        job.runScheduledRefresh();

        verify(ingestionService).run();
        verify(briefingService).generate();
    }

    @Test
    void skipsGenerationWhenIngestionIsBusyOrDisabled() {
        IngestionService busyIngestion = mock(IngestionService.class);
        BriefingService busyBriefing = mock(BriefingService.class);
        when(busyIngestion.run()).thenReturn(new IngestionService.IngestionResult(
                0,
                0,
                List.of("수집 작업"),
                List.of(new IngestionService.SourceResult("수집 작업", SourceFetchStatus.FAILED, 0, 0, false, "이미 수집 작업이 실행 중입니다."))
        ));

        new ScheduledBriefingJob(busyIngestion, busyBriefing, true).runScheduledRefresh();

        verify(busyIngestion).run();
        verifyNoInteractions(busyBriefing);

        IngestionService disabledIngestion = mock(IngestionService.class);
        BriefingService disabledBriefing = mock(BriefingService.class);

        new ScheduledBriefingJob(disabledIngestion, disabledBriefing, false).runScheduledRefresh();

        verifyNoInteractions(disabledIngestion, disabledBriefing);
    }
}
