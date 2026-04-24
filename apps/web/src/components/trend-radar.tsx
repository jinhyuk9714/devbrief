import type { TrendItem } from "../lib/types";

const categoryOrder = ["AI 모델", "오픈소스", "개발 도구", "보안", "클라우드"];

export function TrendRadar({ trends }: { trends: TrendItem[] }) {
  const grouped = categoryOrder
    .map((category) => ({
      category,
      items: trends.filter((trend) => trend.category === category)
    }))
    .filter((group) => group.items.length > 0);

  return (
    <section className="trend-radar" aria-label="트렌드 레이더">
      {grouped.map((group) => (
        <div className="trend-lane" key={group.category}>
          <div className="lane-label">{group.category}</div>
          <div className="lane-items">
            {group.items.map((item) => (
              <div className="trend-item" key={`${item.category}-${item.title}`}>
                <div className="trend-title">
                  <span>{item.title}</span>
                  <strong>{item.score}</strong>
                </div>
                <div className="trend-metric">
                  <span>신호 강도</span>
                  <strong>{item.score}</strong>
                </div>
                <div className="trend-bar" aria-hidden>
                  <span style={{ width: `${item.score}%` }} />
                </div>
                <p>{`${item.articleCount}개 연결 기사`}</p>
              </div>
            ))}
          </div>
        </div>
      ))}
    </section>
  );
}
