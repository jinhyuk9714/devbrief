import Link from "next/link";
import { TrendRadar } from "../../components/trend-radar";
import { getTrends } from "../../lib/api";

type TrendsPageProps = {
  searchParams: Promise<{ range?: string | string[] }>;
};

function normalizeRange(range: string | string[] | undefined): "day" | "week" {
  if (Array.isArray(range)) {
    return range[0] === "day" ? "day" : "week";
  }
  return range === "day" ? "day" : "week";
}

export default async function TrendsPage({ searchParams }: TrendsPageProps) {
  const params = await searchParams;
  const range = normalizeRange(params.range);
  const trends = await getTrends(range);
  const rangeLabel = range === "day" ? "일간" : "주간";

  return (
    <main className="simple-page">
      <section className="section-heading page-heading">
        <div>
          <h1>트렌드 레이더</h1>
          <p>{rangeLabel} 기준으로 기사 묶음과 신호 강도를 비교합니다.</p>
        </div>
        <nav className="range-switch" aria-label="트렌드 범위">
          <Link href="/trends?range=day" aria-current={range === "day" ? "page" : undefined}>일간</Link>
          <Link href="/trends?range=week" aria-current={range === "week" ? "page" : undefined}>주간</Link>
        </nav>
      </section>
      <TrendRadar trends={trends.trends} />
    </main>
  );
}
