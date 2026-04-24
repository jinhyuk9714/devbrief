package com.devbrief.api;

import com.devbrief.briefing.BriefingService;
import com.devbrief.domain.Article;
import com.devbrief.domain.Source;
import com.devbrief.domain.TopicCluster;
import com.devbrief.i18n.KoreanDisplayText;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrendControllerTest {

    @Test
    void filtersTrendsByDayAndWeekRange() {
        BriefingService briefingService = mock(BriefingService.class);
        Instant now = Instant.now();
        TopicCluster recent = cluster("Recent model signal", "AI Models", now.minusSeconds(60 * 60));
        TopicCluster olderThanDay = cluster("Older tool signal", "Developer Tools", now.minusSeconds(2 * 24 * 60 * 60));
        TopicCluster olderThanWeek = cluster("Old security signal", "Security", now.minusSeconds(9 * 24 * 60 * 60));
        when(briefingService.trends()).thenReturn(List.of(recent, olderThanDay, olderThanWeek));

        TrendController controller = new TrendController(briefingService, new KoreanDisplayText());

        TrendController.TrendResponse day = controller.trends("day");
        TrendController.TrendResponse week = controller.trends("week");
        TrendController.TrendResponse invalid = controller.trends("month");

        assertThat(day.range()).isEqualTo("day");
        assertThat(day.trends()).extracting(TrendController.TrendItem::title)
                .containsExactly("Recent model signal");
        assertThat(week.range()).isEqualTo("week");
        assertThat(week.trends()).extracting(TrendController.TrendItem::title)
                .containsExactly("Recent model signal", "Older tool signal");
        assertThat(invalid.range()).isEqualTo("day");
        assertThat(invalid.trends()).extracting(TrendController.TrendItem::title)
                .containsExactly("Recent model signal");
    }

    private TopicCluster cluster(String title, String category, Instant publishedAt) {
        Source source = Source.create(title + " Source", "RSS", "https://example.com/" + title.replace(" ", "-"), category);
        Article article = Article.create(source, title, "https://example.com/article/" + title.replace(" ", "-"), "Author",
                publishedAt, "Excerpt for " + title, title + "-hash");
        return TopicCluster.create(title, category, 90, List.of(article));
    }
}
