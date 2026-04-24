package com.devbrief.briefing;

@FunctionalInterface
public interface OpenAiBriefingClient {
    GeneratedBriefing generate(OpenAiSummaryRequest request);
}
