import Link from "next/link";
import { ArrowUpRight, Clock, Gauge, Link2 } from "lucide-react";
import type { BriefingSummary } from "../lib/types";

export function BriefingList({ briefings }: { briefings: BriefingSummary[] }) {
  if (briefings.length === 0) {
    return (
      <section className="empty-state">
        <h2>오늘 브리핑이 아직 없습니다</h2>
        <p>관리자 화면에서 수집과 생성을 실행하면 브리핑이 표시됩니다.</p>
      </section>
    );
  }

  return (
    <section className="briefing-list" aria-label="오늘의 브리핑">
      {briefings.map((briefing, index) => (
        <article className="briefing-row" key={briefing.id}>
          <div className="rank">{String(index + 1).padStart(2, "0")}</div>
          <div className="briefing-main">
            <div className="briefing-meta">
              <span>{briefing.category}</span>
              <span><Gauge size={14} aria-hidden /> {briefing.importance}</span>
              <span><Clock size={14} aria-hidden /> {briefing.readingMinutes}분</span>
              <span><Link2 size={14} aria-hidden /> {briefing.sourceCount}개 출처</span>
            </div>
            <h2>{briefing.title}</h2>
            <p>{briefing.summary}</p>
            <ul>
              {briefing.actionItems.slice(0, 2).map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </div>
          <Link className="row-link" href={`/briefings/${briefing.id}`} aria-label={`${briefing.title} 상세 보기`}>
            <ArrowUpRight size={20} aria-hidden />
          </Link>
        </article>
      ))}
    </section>
  );
}
