import { BriefingList } from "../components/briefing-list";
import { SourceStatus } from "../components/source-status";
import { TrendRadar } from "../components/trend-radar";
import { getSourceStatus, getTodayBriefings, getTrends } from "../lib/api";

export default async function HomePage() {
  const [today, trends, status] = await Promise.all([
    getTodayBriefings(),
    getTrends("day"),
    getSourceStatus()
  ]);

  return (
    <main>
      <section className="hero">
        <div>
          <p className="eyebrow">AI / Developer Daily</p>
          <h1>오늘 개발자가 알아야 할 변화만 남깁니다.</h1>
          <p className="hero-copy">
            수집, 중복 제거, 클러스터링, 요약, 원문 추적까지 한 번에 보여주는 공개 데모 브리핑입니다.
          </p>
        </div>
        <div className="hero-visual" aria-hidden>
          <div className="pulse-ring" />
          <div className="signal-column">
            <span style={{ height: "46%" }} />
            <span style={{ height: "72%" }} />
            <span style={{ height: "88%" }} />
            <span style={{ height: "58%" }} />
            <span style={{ height: "66%" }} />
          </div>
        </div>
      </section>

      <section className="workspace-grid">
        <div className="primary-column">
          <div className="section-heading">
            <h2>오늘의 브리핑</h2>
            <p>{new Date(today.generatedAt).toLocaleString("ko-KR")} 생성됨</p>
          </div>
          <BriefingList briefings={today.briefings} />
        </div>
        <aside className="side-column">
          <div className="section-heading">
            <h2>트렌드 레이더</h2>
            <p>{trends.trends.length}개 묶인 신호</p>
          </div>
          <TrendRadar trends={trends.trends} />
          <SourceStatus status={status} />
        </aside>
      </section>
    </main>
  );
}
