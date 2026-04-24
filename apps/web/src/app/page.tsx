import Link from "next/link";
import { BriefingList } from "../components/briefing-list";
import { TrendRadar } from "../components/trend-radar";
import { getTodayBriefings, getTrends } from "../lib/api";

const valuePillars = [
  {
    title: "무슨 일",
    body: "여러 출처에서 나온 같은 신호를 묶어 오늘 실제로 벌어진 일을 짧게 정리합니다."
  },
  {
    title: "왜 중요",
    body: "모델, 도구, 보안, 클라우드 선택에 어떤 영향을 주는지 개발자 관점으로 설명합니다."
  },
  {
    title: "해볼 것",
    body: "읽고 끝나지 않도록 문서 확인, 샘플 실행, 의존성 점검 같은 작은 액션으로 연결합니다."
  }
];

const systemFlow = ["RSS/API 수집", "중복 제거", "클러스터링", "요약 생성", "Redis 캐시"];

export default async function HomePage() {
  const [today, trends] = await Promise.all([
    getTodayBriefings(),
    getTrends("day")
  ]);

  return (
    <main>
      <section className="hero">
        <div className="hero-content">
          <p className="eyebrow">AI/개발 뉴스 데일리 브리핑</p>
          <h1>오늘 나온 AI/개발 뉴스를 <span className="keep-together">5분 안에</span> 이해하세요</h1>
          <p className="hero-copy">
            무슨 일인지, 왜 중요한지, 개발자가 뭘 해보면 좋은지, 원문 출처까지 한 화면에 정리합니다.
          </p>
          <div className="hero-actions">
            <Link className="primary-action" href="#today-briefings">오늘 브리핑 보기</Link>
            <Link className="secondary-action" href="/admin">시스템 데모 보기</Link>
          </div>
        </div>
        <div className="hero-visual" aria-hidden>
          <div className="briefing-preview">
            <div className="preview-kicker">오늘의 브리핑</div>
            <div className="preview-story is-featured">
              <span>AI 모델</span>
              <strong>새 모델 공개와 개발자 영향</strong>
              <p>요약 · 왜 중요 · 해볼 것</p>
            </div>
            <div className="preview-story">
              <span>오픈소스</span>
              <strong>인기 프로젝트 릴리스 신호</strong>
            </div>
            <div className="preview-story">
              <span>보안</span>
              <strong>공급망 취약점 대응 체크</strong>
            </div>
          </div>
        </div>
      </section>

      <section className="briefing-section" id="today-briefings">
        <div className="section-heading">
          <h2>오늘의 브리핑</h2>
          <p>{new Date(today.generatedAt).toLocaleString("ko-KR")} 생성됨</p>
        </div>
        <BriefingList briefings={today.briefings} />
      </section>

      <section className="value-section" aria-label="브리핑 구성">
        {valuePillars.map((pillar) => (
          <article className="value-item" key={pillar.title}>
            <h2>{pillar.title}</h2>
            <p>{pillar.body}</p>
          </article>
        ))}
      </section>

      <section className="trend-section">
        <div className="section-heading">
          <h2>트렌드 레이더</h2>
          <p>{trends.trends.length}개 묶인 신호</p>
        </div>
        <TrendRadar trends={trends.trends} />
      </section>

      <section className="system-proof">
        <div className="section-heading">
          <h2>시스템이 브리핑을 만드는 방식</h2>
          <p>뉴스 경험을 방해하지 않도록 기술 증거는 하단에 분리했습니다.</p>
        </div>
        <div className="system-flow" aria-label="브리핑 생성 흐름">
          {systemFlow.map((step, index) => (
            <div className="system-step" key={step}>
              <span>{String(index + 1).padStart(2, "0")}</span>
              <strong>{step}</strong>
            </div>
          ))}
        </div>
      </section>
    </main>
  );
}
