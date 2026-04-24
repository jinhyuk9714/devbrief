import { afterEach, describe, expect, it, vi } from "vitest";
import { getSourceStatus, getTodayBriefings, runAdminAction } from "../api";

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

  it("sends the admin token only for admin mutations", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify({ sourcesChecked: 1, articlesImported: 0, failedSources: [] }), {
        status: 200,
        headers: { "Content-Type": "application/json" }
      })
    );
    vi.stubGlobal("fetch", fetchMock);

    await runAdminAction("/api/admin/ingest/run", "secret-token");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/admin/ingest/run",
      expect.objectContaining({
        method: "POST",
        headers: expect.objectContaining({
          "Content-Type": "application/json",
          "X-Admin-Token": "secret-token"
        })
      })
    );
  });

  it("uses a Korean authorization error for protected admin mutations", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(new Response("관리 토큰이 필요합니다.", { status: 401 })));

    await expect(runAdminAction("/api/admin/ingest/run", "wrong-token")).rejects.toThrow("관리 토큰이 필요합니다");
  });
});
