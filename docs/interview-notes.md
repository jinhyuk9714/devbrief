# DevBrief Interview Notes

## 1분 설명

DevBrief는 개발자가 매일 쏟아지는 AI/개발 뉴스를 빠르게 따라잡을 수 있게 만든 한국어 데일리 브리핑 서비스입니다. Spring Boot 백엔드가 RSS, GitHub Trending, 기술 블로그를 수집하고 URL/content hash로 중복을 줄인 뒤, 제목과 excerpt의 신호를 기준으로 토픽을 묶고 점수화합니다. 이후 OpenAI 또는 deterministic fallback으로 `무슨 일인지`, `왜 중요한지`, `개발자가 뭘 해보면 좋은지`를 생성하고, Next.js UI에서 오늘의 브리핑, 상세, 트렌드, 관리자 상태 화면을 제공합니다.

## 3분 설명

이 프로젝트는 단순 뉴스 목록이나 CRUD 앱이 아니라 외부 데이터 수집부터 운영 상태까지 이어지는 풀스택 파이프라인입니다. 핵심 흐름은 `source 수집 -> 중복 제거 -> anchor + BM25/IDF 휴리스틱 그룹핑/점수화 -> 한국어 요약 -> 캐시/상태 표시`입니다.

백엔드에서는 source별로 성공, 데모, 대체 데이터, 실패 상태를 기록합니다. 네트워크가 꺼져 있거나 RSS 파싱이 실패해도 전체 수집이 멈추지 않고 source별 결과를 관리자 화면에 남깁니다. Redis가 있으면 ingestion job을 distributed gate로 보호하고, Redis가 없으면 local lock으로 데모 동작을 유지합니다. OpenAI 키가 있으면 실제 한국어 요약을 만들고, 없거나 실패하면 source 이름, 원문 제목, excerpt를 반영한 fallback 요약을 생성합니다.

프론트는 포트폴리오 설명 페이지가 아니라 브리핑 데스크처럼 설계했습니다. 홈에서는 오늘 읽을 lead briefing과 브리핑 큐를 먼저 보여주고, 상세 페이지는 요약, 중요한 이유, 개발자 액션을 우선 배치합니다. `/admin`은 운영 도구로 분리해서 수집 실행, 브리핑 생성, source 상태, Redis/cache 상태를 확인할 수 있게 했습니다.

## 예상 질문과 답변

### 왜 이 프로젝트를 만들었나요?

게시판이나 TODO 앱보다 실제 서비스에서 자주 만나는 문제가 외부 데이터 수집, 실패 대응, 요약 품질, 운영 가시성이라고 봤습니다. AI/개발 뉴스는 데이터 source가 다양하고 노이즈가 많아서, 풀스택 포트폴리오로 파이프라인 설계를 보여주기 좋았습니다.

### 클러스터링이 AI인가요?

현재는 임베딩 기반 semantic clustering이 아니라 anchor + BM25/IDF 기반 휴리스틱 그룹핑입니다. 제목과 excerpt에서 MCP, browser trace replay, GitHub Actions provenance 같은 강한 anchor를 먼저 잡고, 나머지는 token overlap과 BM25/IDF 가중 유사도로 묶습니다. `agent`, `model` 같은 일반 단어만으로는 과하게 묶지 않도록 했고, README에도 이 구현 수준을 투명하게 적었습니다. 다음 단계로는 embedding similarity를 붙일 수 있습니다.

### OpenAI가 실패하면 어떻게 되나요?

OpenAI provider는 abstraction 뒤에 있고, 키가 없거나 응답 파싱이 실패하면 deterministic fallback provider로 내려갑니다. fallback도 단순 카테고리 템플릿이 아니라 lead article의 source, title, excerpt를 섞어서 브리핑별 문장이 달라지게 했습니다.

### 외부 RSS나 GitHub Trending이 실패하면요?

source 하나가 실패해도 전체 ingestion loop는 계속 진행됩니다. 각 source는 `OK`, `DEMO`, `FALLBACK`, `FAILED` 중 하나로 기록되고, 가져온 기사 수, fallback 여부, 실패 메시지가 `/admin`에 보입니다. 빈 피드, 파싱 실패, 네트워크 실패 메시지도 구분합니다.

### Redis는 어떤 역할인가요?

Redis가 있으면 manual/scheduled ingestion이 여러 인스턴스에서 동시에 실행되지 않도록 gate로 사용하고, hot briefing cache 상태도 남깁니다. Redis가 없는 무료 데모 환경에서는 local lock fallback으로 한 인스턴스 안의 중복 실행만 막습니다.

### 공개 데모는 왜 H2인가요?

현재 공개 API는 무료 Render Docker web service에서 돌아가고 있어 H2 memory storage를 사용합니다. 공개 데모는 비용 없이 보는 포트폴리오 경험을 우선했고, admin mutation은 `DEVBRIEF_ADMIN_TOKEN`으로 보호하는 방향입니다. 실제 full setup용 PostgreSQL/Redis `render.yaml`은 함께 커밋해두었습니다.

### 관리자 API는 공개되어 있나요?

상태 조회는 공개지만 수집 실행과 브리핑 생성 같은 mutation은 `DEVBRIEF_ADMIN_TOKEN`이 설정된 환경에서 `X-Admin-Token`을 요구합니다. 로컬 개발에서는 토큰을 비워둘 수 있게 했습니다.

### 다음 개선은 무엇인가요?

첫 번째는 실제 운영 RSS source를 늘리고 parser fixture를 더 쌓는 것입니다. 두 번째는 BM25/IDF 그룹핑 위에 embedding 기반 유사도 그룹핑을 선택적으로 올리는 것입니다. 세 번째는 OpenAI 요약 결과의 JSON schema 검증과 출처 근거 표시를 더 강화하는 것입니다.
