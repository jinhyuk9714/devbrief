package com.devbrief.briefing;

import java.util.List;

public record GeneratedBriefing(
        String summary,
        String whyItMatters,
        List<String> keyPoints,
        List<String> actionItems,
        List<String> riskNotes
) {
}

