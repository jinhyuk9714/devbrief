import Link from "next/link";
import { BriefingList } from "../components/briefing-list";
import { TrendRadar } from "../components/trend-radar";
import { getTodayBriefings, getTrends } from "../lib/api";

const valuePillars = [
  {
    label: "무슨 일",
    title: "뉴스를 사건으로 묶습니다",
    body: "여러 출처에서 나온 같은 신호를 묶어 오늘 실제로 벌어진 일을 짧게 정리합니다."
  },
  {
    label: "왜 중요",
    title: "개발자 영향만 남깁니다",
    body: "모델, 도구, 보안, 클라우드 선택에 어떤 영향을 주는지 개발자 관점으로 설명합니다."
  },
  {
    label: "해볼 것",
    title: "다음 행동으로 끝냅니다",
    body: "읽고 끝나지 않도록 문서 확인, 샘플 실행, 의존성 점검 같은 작은 액션으로 연결합니다."
  }
];

export default async function HomePage() {
  const [today, trends] = await Promise.all([
    getTodayBriefings(),
    getTrends("day")
  ]);
  const generatedAt = new Date(today.generatedAt).toLocaleString("ko-KR", {
    month: "numeric",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
  const totalSources = today.briefings.reduce((total, briefing) => total + briefing.sourceCount, 0);

  return (
    <main className="briefing-desk-page">
      <section className="desk-hero" id="today-briefings">
        <div className="desk-heading">
          <div>
            <p className="eyebrow">오늘의 AI/개발 브리핑</p>
            <h1 aria-label="5분 안에 읽고 바로 해볼 것까지 정리합니다">
              <span>5분 안에 읽고</span>
              <span>바로 해볼 것까지</span>
              <span>정리합니다</span>
            </h1>
          </div>
          <p>
            <span>여러 출처에서 같은 신호를 묶고,</span>
            <span>개발자가 지금 확인할 일만</span>
            <span>남깁니다.</span>
          </p>
        </div>
        <div className="desk-toolbar" aria-label="브리핑 상태">
          <span>{generatedAt} 생성</span>
          <span>{today.briefings.length}개 브리핑</span>
          <span>{totalSources}개 출처 신호</span>
        </div>
        <BriefingList briefings={today.briefings} />
      </section>

      <section className="value-section" aria-label="브리핑 구성">
        {valuePillars.map((pillar) => (
          <article className="value-item" key={pillar.label}>
            <span>{pillar.label}</span>
            <h2>{pillar.title}</h2>
            <p>{pillar.body}</p>
          </article>
        ))}
      </section>

      <section className="trend-section">
        <div className="section-heading">
          <div>
            <h2>지금 같이 움직이는 신호</h2>
            <p>{trends.trends.length}개 묶인 신호를 일간 기준으로 봅니다.</p>
          </div>
          <Link className="text-action" href="/trends?range=day">트렌드 전체 보기</Link>
        </div>
        <TrendRadar trends={trends.trends} />
      </section>
    </main>
  );
}
