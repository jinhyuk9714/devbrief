import { afterEach, describe, expect, it, vi } from "vitest";
import { getSourceStatus, getTodayBriefings } from "../api";

describe("api client", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("does not reuse stale Next fetch cache for briefing data", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ generatedAt: "2026-04-24T08:30:00Z", briefings: [] }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      })
    );
    vi.stubGlobal("fetch", fetchMock);

    await getTodayBriefings();

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/briefings/today",
      expect.objectContaining({ cache: "no-store" })
    );
  });

  it("falls back to demo source status when the API is unavailable", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("network down")));

    const status = await getSourceStatus();

    expect(status.sources.length).toBeGreaterThan(0);
    expect(status.sources[0].lastFetchStatus).toBeTruthy();
  });
});
