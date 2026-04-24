import { Database, RadioTower } from "lucide-react";
import type { SourceStatus as SourceStatusType } from "../lib/types";

function redisLabel(value: unknown) {
  if (value === "available") return "사용 가능";
  if (value === "unavailable") return "연결 실패";
  if (value === "demo") return "데모";
  return "알 수 없음";
}

function cacheLabel(value: unknown) {
  return value ? "활성" : "비활성";
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
            <span>{source.name}</span>
            <span>{source.category}</span>
            <span>{source.type}</span>
          </div>
        ))}
      </div>
    </section>
  );
}
