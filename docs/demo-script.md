# DevBrief Demo Script

## 30초 포지셔닝

DevBrief는 AI/개발 뉴스를 여러 출처에서 수집하고, 중복 제거와 토픽 묶기, 점수화, 한국어 요약을 거쳐 개발자가 바로 해볼 액션까지 정리해주는 브리핑 서비스입니다.

## 시연 순서

1. 홈에서 `오늘의 AI/개발 브리핑`을 보여줍니다.
   - lead briefing과 나머지 briefing queue를 보여주며 “뉴스 목록이 아니라 개발자 액션까지 정리한다”고 설명합니다.

2. 브리핑 상세 페이지를 엽니다.
   - `요약`, `왜 중요한가`, `개발자 액션`이 먼저 나오고, 원문 출처와 타임라인은 보조 정보로 분리되어 있음을 보여줍니다.

3. `/trends?range=day`와 `/trends?range=week`를 비교합니다.
   - 토픽 묶음과 신호 강도를 보여주며 anchor + BM25/IDF 기반 휴리스틱 그룹핑이라고 투명하게 설명합니다.

4. `/admin`에서 source 상태를 확인합니다.
   - `정상`, `데모`, `대체 데이터`, `실패` 배지와 가져온 기사 수, fallback 메시지를 보여줍니다.
   - Anthropic은 RSS가 없어 뉴스룸 HTML parser로 처리한다고 설명합니다.

5. 관리 토큰 없이 mutation 버튼을 시도합니다.
   - 공개 데모에서도 수집/생성 mutation은 `DEVBRIEF_ADMIN_TOKEN`으로 보호한다고 설명합니다.

6. 관리 토큰이 있는 환경에서는 `수집 실행` 후 `브리핑 생성`을 실행합니다.
   - source별 수집 결과와 생성된 브리핑 수를 확인합니다.

## 강조할 설계 선택

- 무료 공개 데모는 H2로 가볍게 유지하고, `render.yaml`에는 PostgreSQL/Redis 확장 구성을 남겼습니다.
- 한 source가 너무 많은 기사를 가져오면 최신 40개만 사용합니다.
- OpenAI 응답은 한국어, 원문 source/title 보존, 일반론 방지, 실행 가능한 action item 기준으로 검증합니다.
- OpenAI 키가 없거나 응답 품질이 낮으면 기사 기반 deterministic fallback으로 데모가 깨지지 않습니다.

## 예상 질문

- “클러스터링이 AI인가요?”
  현재는 임베딩이 아니라 anchor + BM25/IDF 기반 휴리스틱 그룹핑입니다. 비용 없이 안정적인 MVP를 만들고, 다음 단계로 embedding similarity를 붙일 수 있게 구조를 분리했습니다.

- “외부 source가 실패하면요?”
  source별 try/catch로 전체 수집은 계속 진행하고, 실패 source는 `/admin`에 상태, 메시지, fallback 여부로 남깁니다.

- “왜 admin이 공개되어 있나요?”
  상태 조회는 공개 데모의 신뢰도 증거로 열어두고, 수집/생성 mutation만 token-protected로 분리했습니다.
