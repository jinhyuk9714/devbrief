# DevBrief Architecture

## Flow

1. `IngestionService` ensures the default source catalog exists.
2. Each enabled source is fetched from RSS/API when network mode is enabled; otherwise deterministic demo articles are used.
3. `ContentHashService` removes common URL tracking noise and deduplicates articles.
4. `ClusterScoringService` groups articles by developer signal and ranks them by freshness, volume, and keyword strength.
5. `SummaryProvider` generates the public briefing. The current default is deterministic so the demo works without an API key.
6. `RedisGateway` provides a lightweight ingestion lock and cache status surface.
7. Next.js reads the public API and falls back to local demo data when the API is not running.

## Portfolio Emphasis

The app is designed to make backend work visible in the UI:

- `Today` shows score, category, reading time, and source count.
- `Detail` shows source links, timeline, risk notes, and action items.
- `Trends` shows clusters by category.
- `Admin` can trigger ingestion and briefing generation.

