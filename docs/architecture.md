# DevBrief Architecture

## Flow

1. `IngestionService` ensures the default source catalog exists and updates URL/type/category when the catalog changes.
2. Each enabled source is fetched from RSS/API/HTML when network mode is enabled; otherwise deterministic demo articles are used.
3. `ContentHashService` removes common URL tracking noise and deduplicates articles.
4. Each source contributes at most the latest 40 parsed articles so one noisy source does not dominate briefing candidates.
5. `ClusterScoringService` groups articles by anchor signals plus BM25/IDF token similarity, ranks them by freshness, volume, and keyword strength, and places the title representative article first.
6. `OpenAiSummaryProvider` generates Korean briefings with the OpenAI Responses API when `OPENAI_API_KEY` is present.
7. `BriefingQualityValidator` rejects generic, English-only, non-actionable, or source/title-translating responses.
8. `DeterministicSummaryProvider` remains the fallback path when the key is missing or an LLM request fails validation.
9. `RedisGateway` provides a lightweight ingestion lock and cache status surface.
10. Next.js reads the public API and falls back to local demo data when the API is not running.

## Portfolio Emphasis

The app is designed to make backend work visible in the UI:

- `Today` shows score, category, reading time, source count, and article count.
- `Detail` shows whether the briefing is based on a single original article or a multi-article cluster, then source links, timeline, risk notes, and action items.
- `Trends` shows clusters by category.
- `Admin` can trigger ingestion and briefing generation, then inspect source-level `OK`, `DEMO`, `FALLBACK`, and `FAILED` status.
