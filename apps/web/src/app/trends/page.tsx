import { TrendRadar } from "../../components/trend-radar";
import { getTrends } from "../../lib/api";

export default async function TrendsPage() {
  const trends = await getTrends("week");

  return (
    <main className="simple-page">
      <section className="section-heading page-heading">
        <h1>트렌드 레이더</h1>
        <p>주간 기준으로 기사 묶음과 점수를 비교합니다.</p>
      </section>
      <TrendRadar trends={trends.trends} />
    </main>
  );
}
