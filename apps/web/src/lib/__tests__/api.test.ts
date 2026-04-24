import { afterEach, describe, expect, it, vi } from "vitest";
import { getTodayBriefings } from "../api";

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
});
