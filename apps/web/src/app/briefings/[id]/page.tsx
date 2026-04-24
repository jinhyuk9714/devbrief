import Link from "next/link";
import { ArrowLeft, ExternalLink } from "lucide-react";
import { getBriefingDetail } from "../../../lib/api";

export default async function BriefingDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  const briefing = await getBriefingDetail(id);

  return (
    <main className="detail-page">
      <Link className="back-link" href="/">
        <ArrowLeft size={18} aria-hidden />
        오늘
      </Link>
      <section className="detail-header">
        <p className="eyebrow">{briefing.category} / {briefing.importance}</p>
        <h1>{briefing.title}</h1>
        <p>{briefing.summary}</p>
      </section>

      <section className="detail-grid">
        <div className="detail-main">
          <h2>왜 중요한가</h2>
          <p>{briefing.whyItMatters}</p>
          <h2>핵심 포인트</h2>
          <ul className="check-list">
            {briefing.keyPoints.map((item) => <li key={item}>{item}</li>)}
          </ul>
          <h2>개발자 액션</h2>
          <ul className="check-list">
            {briefing.actionItems.map((item) => <li key={item}>{item}</li>)}
          </ul>
        </div>
        <aside className="detail-side">
          <h2>원문 출처</h2>
          {briefing.sources.map((source) => (
            <a className="source-link" href={source.url} key={source.url} target="_blank" rel="noreferrer">
              <span>{source.sourceName}</span>
              <strong>{source.title}</strong>
              <ExternalLink size={16} aria-hidden />
            </a>
          ))}
          <h2>타임라인</h2>
          <div className="timeline">
            {briefing.timeline.map((item) => (
              <div key={`${item.at}-${item.title}`}>
                <time>{new Date(item.at).toLocaleTimeString("ko-KR", { hour: "2-digit", minute: "2-digit" })}</time>
                <span>{item.source}</span>
                <p>{item.title}</p>
              </div>
            ))}
          </div>
          <h2>주의할 점</h2>
          <ul className="risk-list">
            {briefing.riskNotes.map((note) => <li key={note}>{note}</li>)}
          </ul>
        </aside>
      </section>
    </main>
  );
}
