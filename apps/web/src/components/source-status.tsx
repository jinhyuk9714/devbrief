import { Database, RadioTower } from "lucide-react";
import type { SourceStatus as SourceStatusType } from "../lib/types";

function redisLabel(value: unknown) {
  if (value === "available") return "사용 가능";
  if (value === "unavailable") return "연결 실패";
  if (value === "demo") return "데모 모드";
  if (value === "disabled") return "비활성";
  return "알 수 없음";
}

function cacheLabel(value: unknown) {
  return value ? "활성" : "비활성";
}

function sourceStatusLabel(value: unknown) {
  if (value === "OK") return "정상";
  if (value === "DEMO") return "데모";
  if (value === "FALLBACK") return "대체 데이터";
  if (value === "FAILED") return "실패";
  return "미수집";
}

function formatFetchedAt(value: string | null) {
  if (!value) return "미수집";
  return new Date(value).toLocaleString("ko-KR", {
    month: "numeric",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
}

export function SourceStatus({ status }: { status: SourceStatusType }) {
  return (
    <section className="source-status">
      <div className="section-heading">
        <h2>출처 상태</h2>
        <p>{status.sources.length}개 출처 추적 중</p>
      </div>
      <div className="status-grid">
        <div className="status-line">
          <Database size={18} aria-hidden />
          <span>Redis</span>
          <strong>{redisLabel(status.cache.redis)}</strong>
        </div>
        <div className="status-line">
          <RadioTower size={18} aria-hidden />
          <span>핫 캐시</span>
          <strong>{cacheLabel(status.cache.hotBriefingCache)}</strong>
        </div>
      </div>
      <div className="source-table">
        {status.sources.slice(0, 6).map((source) => (
          <div className="source-row" key={source.name}>
            <div>
              <span>{source.name}</span>
              <small>{source.category} · {source.type}</small>
              {source.lastFetchMessage ? <small className="source-message">{source.lastFetchMessage}</small> : null}
            </div>
            <strong className={`source-badge status-${source.lastFetchStatus?.toLowerCase() ?? "unknown"}`}>
              {sourceStatusLabel(source.lastFetchStatus)}
            </strong>
            <span>{source.lastArticleCount ?? 0}개</span>
            <span>{formatFetchedAt(source.lastFetchedAt)}</span>
            <span>{source.lastUsedFallback ? "대체 사용" : "원본"}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
