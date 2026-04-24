import { briefingDetail, sourceStatus, todayBriefings, trendData } from "./demo-data";
import type { AdminActionResult, BriefingDetail, SourceStatus, TodayBriefings, TrendResponse } from "./types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function fetchJson<T>(path: string, fallback: T, init?: RequestInit): Promise<T> {
  try {
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      cache: "no-store"
    });
    if (!response.ok) {
      return fallback;
    }
    return (await response.json()) as T;
  } catch {
    return fallback;
  }
}

export function getTodayBriefings(): Promise<TodayBriefings> {
  return fetchJson("/api/briefings/today", todayBriefings);
}

export function getBriefingDetail(id: string): Promise<BriefingDetail> {
  return fetchJson(`/api/briefings/${id}`, { ...briefingDetail, id: Number(id) || briefingDetail.id });
}

export function getTrends(range: "day" | "week" = "day"): Promise<TrendResponse> {
  return fetchJson(`/api/trends?range=${range}`, trendData);
}

export function getSourceStatus(): Promise<SourceStatus> {
  return fetchJson("/api/sources/status", sourceStatus);
}

export async function runAdminAction(path: "/api/admin/ingest/run" | "/api/admin/briefings/generate") {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" }
  });
  if (!response.ok) {
    throw new Error(`요청 실패: ${response.status}`);
  }
  return response.json() as Promise<AdminActionResult>;
}
