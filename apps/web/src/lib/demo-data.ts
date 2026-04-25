import type { BriefingDetail, TodayBriefings, TrendResponse, SourceStatus } from "./types";

export const todayBriefings: TodayBriefings = {
  generatedAt: new Date("2026-04-24T08:30:00.000Z").toISOString(),
  briefings: [
    {
      id: 1,
      title: "모델 컨텍스트 기능이 에이전트 워크플로를 바꾸는 중",
      category: "AI 모델",
      importance: 92,
      readingMinutes: 4,
      sourceCount: 3,
      articleCount: 2,
      summary:
        "AI 모델 제공사들이 긴 컨텍스트, 도구 호출, 세션 메모리를 더 제품화하면서 개발자는 에이전트 워크플로를 작은 실험 단위로 검증해야 합니다.",
      whyItMatters:
        "코딩 에이전트가 단발성 답변보다 장기 작업을 맡기 쉬워지고 있어, 컨텍스트 관리와 검증 파이프라인이 제품 품질을 좌우합니다.",
      actionItems: [
        "현재 쓰는 에이전트 프롬프트에 컨텍스트 회수 기준을 명시하기",
        "한 저장소에서 작은 agent workflow spike를 실행해 실패 로그를 모으기",
        "도구 호출이 만든 변경을 테스트와 리뷰 체크리스트에 연결하기"
      ]
    },
    {
      id: 2,
      title: "코딩 에이전트 전반으로 MCP 서버 도입 확산",
      category: "오픈소스",
      importance: 88,
      readingMinutes: 5,
      sourceCount: 4,
      articleCount: 4,
      summary:
        "MCP 서버가 문서, GitHub, 로컬 도구, SaaS API를 연결하는 표준 인터페이스로 빠르게 확산되고 있습니다.",
      whyItMatters:
        "개발팀은 에이전트별 플러그인을 따로 관리하기보다 공통 도구 계층을 갖추는 편이 유지보수에 유리합니다.",
      actionItems: [
        "자주 쓰는 내부 API 하나를 read-only MCP로 감싸보기",
        "권한과 감사 로그가 필요한 도구를 별도 분리하기",
        "도구 응답에 source와 freshness 필드를 포함하기"
      ]
    },
    {
      id: 3,
      title: "AI 코딩 워크플로 초기에 시크릿 스캐닝 배치",
      category: "보안",
      importance: 84,
      readingMinutes: 3,
      sourceCount: 2,
      articleCount: 2,
      summary:
        "AI가 코드를 생성하는 지점에서 secret scan과 정책 검사를 더 일찍 실행하려는 흐름이 강해지고 있습니다.",
      whyItMatters:
        "생성된 코드가 PR까지 간 뒤 잡히면 비용이 커지므로, 로컬 에이전트 단계의 빠른 차단이 중요합니다.",
      actionItems: [
        "템플릿 레포에 pre-commit secret scan을 기본값으로 넣기",
        "에이전트 출력에 .env 예시와 실제 키가 섞이지 않게 규칙화하기",
        "CI 실패 메시지를 개발자 행동으로 바로 이어지게 쓰기"
      ]
    },
    {
      id: 4,
      title: "브라우저 자동화 도구의 트레이스 재생 강화",
      category: "개발 도구",
      importance: 77,
      readingMinutes: 4,
      sourceCount: 2,
      articleCount: 2,
      summary:
        "브라우저 자동화 도구들이 테스트 생성보다 trace replay와 실패 원인 설명에 더 많은 기능을 붙이고 있습니다.",
      whyItMatters:
        "QA와 백엔드가 같은 사용자 흐름을 보고 API 호출, 지연, 실패를 함께 추적하기 쉬워집니다.",
      actionItems: [
        "핵심 사용자 흐름 하나를 Playwright trace로 기록하기",
        "API 호출 목록을 성능 테스트 초안으로 변환하기",
        "실패 화면과 네트워크 로그를 같은 리포트에 묶기"
      ]
    },
    {
      id: 5,
      title: "관리형 Postgres의 벡터 인덱싱 기본값 개선",
      category: "클라우드",
      importance: 71,
      readingMinutes: 3,
      sourceCount: 2,
      articleCount: 2,
      summary:
        "관리형 Postgres 서비스가 vector search와 운영 메트릭 기본값을 강화하면서 RAG 프로토타입의 운영 진입 장벽이 낮아지고 있습니다.",
      whyItMatters:
        "작은 팀도 별도 벡터 DB 없이 검색 품질과 운영성을 동시에 실험할 수 있습니다.",
      actionItems: [
        "문서 검색용 테이블에 freshness와 source 컬럼을 함께 설계하기",
        "embedding 재생성 작업을 큐 기반으로 분리하기",
        "검색 결과 품질을 수동 평가 샘플로 추적하기"
      ]
    }
  ]
};

export const trendData: TrendResponse = {
  range: "day",
  trends: [
    { category: "AI 모델", title: "에이전트 메모리와 컨텍스트 윈도", score: 92, articleCount: 5, lastSeenAt: "2026-04-24T08:00:00Z" },
    { category: "오픈소스", title: "MCP 서버 레지스트리 패턴", score: 88, articleCount: 4, lastSeenAt: "2026-04-24T07:40:00Z" },
    { category: "보안", title: "AI 생성 코드 시크릿 스캐닝", score: 84, articleCount: 3, lastSeenAt: "2026-04-24T07:10:00Z" },
    { category: "개발 도구", title: "브라우저 흐름 트레이스 재생", score: 77, articleCount: 2, lastSeenAt: "2026-04-24T06:35:00Z" },
    { category: "클라우드", title: "Postgres 벡터 기본값", score: 71, articleCount: 2, lastSeenAt: "2026-04-24T06:00:00Z" }
  ],
  categories: {
    "AI 모델": 1,
    "오픈소스": 1,
    "보안": 1,
    "개발 도구": 1,
    "클라우드": 1
  }
};

export const briefingDetail: BriefingDetail = {
  ...todayBriefings.briefings[0],
  keyPoints: [
    "모델 제공사들이 세션 단위 컨텍스트와 도구 호출 안정성을 제품 기능으로 밀고 있습니다.",
    "개발자는 에이전트가 읽는 자료의 freshness와 source를 더 엄격히 관리해야 합니다.",
    "작은 workflow 실험을 테스트와 로그로 남기는 팀이 더 빠르게 이득을 봅니다."
  ],
  riskNotes: [
    "벤더 발표는 실제 SDK 안정성과 시차가 있을 수 있습니다.",
    "긴 컨텍스트는 비용과 지연을 늘릴 수 있어 캐시와 요약 전략이 필요합니다."
  ],
  sources: [
    {
      title: "New model context features reshape agent workflows",
      url: "https://example.com/model-context",
      sourceName: "OpenAI Blog",
      author: "OpenAI",
      publishedAt: "2026-04-24T08:00:00Z",
      excerpt: "New context APIs help developers ship agentic workflows."
    },
    {
      title: "Agent workflow tools converge around shared context",
      url: "https://example.com/agent-workflow",
      sourceName: "Hacker News",
      author: "HN",
      publishedAt: "2026-04-24T07:20:00Z",
      excerpt: "Developers compare context handling patterns across coding agents."
    }
  ],
  timeline: [
    { at: "2026-04-24T07:20:00Z", source: "Hacker News", title: "Agent workflow tools converge around shared context" },
    { at: "2026-04-24T08:00:00Z", source: "OpenAI Blog", title: "New model context features reshape agent workflows" }
  ]
};

export const sourceStatus: SourceStatus = {
  sources: [
    {
      name: "GitHub Trending",
      type: "API",
      category: "오픈소스",
      enabled: true,
      lastFetchedAt: "2026-04-24T08:30:00Z",
      lastFetchStatus: "DEMO",
      lastFetchMessage: "네트워크 비활성화 또는 비RSS 출처라 데모 데이터를 사용했습니다.",
      lastArticleCount: 2,
      lastUsedFallback: true
    },
    {
      name: "Hacker News",
      type: "RSS",
      category: "개발 도구",
      enabled: true,
      lastFetchedAt: "2026-04-24T08:30:00Z",
      lastFetchStatus: "OK",
      lastFetchMessage: "RSS 수집 성공",
      lastArticleCount: 4,
      lastUsedFallback: false
    },
    {
      name: "OpenAI Blog",
      type: "RSS",
      category: "AI 모델",
      enabled: true,
      lastFetchedAt: "2026-04-24T08:30:00Z",
      lastFetchStatus: "FALLBACK",
      lastFetchMessage: "RSS 수집 실패 후 대체 데이터를 사용했습니다.",
      lastArticleCount: 2,
      lastUsedFallback: true
    }
  ],
  cache: {
    redis: "demo",
    hotBriefingCache: true
  }
};
