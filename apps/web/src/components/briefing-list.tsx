import Link from "next/link";
import { ArrowUpRight, Clock, Gauge, Link2 } from "lucide-react";
import type { BriefingSummary } from "../lib/types";

function BriefingMeta({ briefing }: { briefing: BriefingSummary }) {
  const sourceLabel = briefing.sourceCount === 1 ? "단일 출처" : `${briefing.sourceCount}개 출처`;
  const articleLabel = briefing.articleCount === 1 ? "단일 원문" : `${briefing.articleCount}개 원문`;
  return (
    <div className="briefing-meta">
      <span>{briefing.category}</span>
      <span><Gauge size={14} aria-hidden /> 중요도 {briefing.importance}</span>
      <span><Clock size={14} aria-hidden /> {briefing.readingMinutes}분 읽기</span>
      <span><Link2 size={14} aria-hidden /> {sourceLabel}</span>
      <span>{articleLabel}</span>
    </div>
  );
}

function BriefingFields({ briefing, compact = false }: { briefing: BriefingSummary; compact?: boolean }) {
  return (
    <div className={compact ? "briefing-fields is-compact" : "briefing-fields"}>
      <div>
        <span className="field-label">요약</span>
        <p>{briefing.summary}</p>
      </div>
      <div>
        <span className="field-label">왜 중요</span>
        <p>{briefing.whyItMatters}</p>
      </div>
      <div>
        <span className="field-label">해볼 것</span>
        <ul>
          {briefing.actionItems.slice(0, compact ? 1 : 2).map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </div>
    </div>
  );
}

export function BriefingList({ briefings }: { briefings: BriefingSummary[] }) {
  if (briefings.length === 0) {
    return (
      <section className="empty-state">
        <h2>오늘 브리핑이 아직 없습니다</h2>
        <p>관리자 화면에서 수집과 생성을 실행하면 브리핑이 표시됩니다.</p>
      </section>
    );
  }

  const [lead, ...queue] = briefings;

  return (
    <section className="briefing-desk" aria-label="오늘의 브리핑 데스크">
      <article className="lead-briefing" aria-label="대표 브리핑">
        <div className="rank">01</div>
        <div className="briefing-main">
          <BriefingMeta briefing={lead} />
          <h2>{lead.title}</h2>
          <BriefingFields briefing={lead} />
        </div>
        <Link className="lead-link" href={`/briefings/${lead.id}`} aria-label={`${lead.title} 상세 보기`}>
          상세 읽기
          <ArrowUpRight size={18} aria-hidden />
        </Link>
      </article>

      {queue.length > 0 ? (
        <section className="briefing-queue" aria-label="브리핑 큐">
          <div className="queue-heading">
            <span>브리핑 큐</span>
            <strong>{queue.length}개 남음</strong>
          </div>
          {queue.map((briefing, index) => (
            <article className="queue-briefing" key={briefing.id}>
              <div className="rank">{String(index + 2).padStart(2, "0")}</div>
              <div className="briefing-main">
                <BriefingMeta briefing={briefing} />
                <h2>{briefing.title}</h2>
                <BriefingFields briefing={briefing} compact />
              </div>
              <Link className="row-link" href={`/briefings/${briefing.id}`} aria-label={`${briefing.title} 상세 보기`}>
                <ArrowUpRight size={18} aria-hidden />
              </Link>
            </article>
          ))}
        </section>
      ) : null}
    </section>
  );
}
