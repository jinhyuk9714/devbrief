"use client";

import { useEffect, useState } from "react";
import { RefreshCcw, Wand2 } from "lucide-react";
import { runAdminAction } from "../lib/api";
import type { AdminActionResult } from "../lib/types";

type ActionState = {
  label: string;
  result: AdminActionResult | null;
  error: string | null;
  loading: boolean;
};

export function AdminPanel() {
  const [state, setState] = useState<ActionState>({ label: "대기 중", result: null, error: null, loading: false });
  const [adminToken, setAdminToken] = useState("");

  useEffect(() => {
    setAdminToken(window.sessionStorage.getItem("devbrief-admin-token") ?? "");
  }, []);

  function updateAdminToken(value: string) {
    setAdminToken(value);
    if (value.trim()) {
      window.sessionStorage.setItem("devbrief-admin-token", value);
    } else {
      window.sessionStorage.removeItem("devbrief-admin-token");
    }
  }

  async function run(label: string, path: "/api/admin/ingest/run" | "/api/admin/briefings/generate") {
    const token = adminToken.trim();
    if (!token) {
      setState({ label, result: null, error: "관리 토큰이 필요합니다.", loading: false });
      return;
    }
    setState({ label, result: null, error: "실행 중", loading: true });
    try {
      const result = await runAdminAction(path, token);
      setState({ label, result, error: null, loading: false });
    } catch (error) {
      setState({ label, result: null, error: error instanceof Error ? error.message : "요청 실패", loading: false });
    }
  }

  const failedCount = state.result?.failedSources?.length ?? 0;
  const sourceResults = state.result?.sourceResults ?? [];

  return (
    <section className="admin-panel">
      <div className="section-heading">
        <h1>데모 운영</h1>
        <p>시스템 상태와 수집 파이프라인을 확인합니다.</p>
      </div>
      <label className="admin-token">
        <span>관리 토큰</span>
        <input
          autoComplete="off"
          onChange={(event) => updateAdminToken(event.target.value)}
          placeholder="수집/생성 실행용 토큰"
          type="password"
          value={adminToken}
        />
      </label>
      <div className="admin-actions">
        <button disabled={state.loading} onClick={() => run("수집", "/api/admin/ingest/run")}>
          <RefreshCcw size={18} aria-hidden />
          수집 실행
        </button>
        <button disabled={state.loading} onClick={() => run("생성", "/api/admin/briefings/generate")}>
          <Wand2 size={18} aria-hidden />
          브리핑 생성
        </button>
      </div>
      <div className="operation-output">
        <span>{state.label}</span>
        {state.error ? <p>{state.error}</p> : null}
        {state.result && typeof state.result.sourcesChecked === "number" ? (
          <div className="operation-summary" aria-label="수집 결과 요약">
            <strong>{state.result.sourcesChecked}개 출처 확인</strong>
            <strong>{state.result.articlesImported ?? 0}개 신규 저장</strong>
            <strong>실패 {failedCount}개</strong>
          </div>
        ) : null}
        {sourceResults.length > 0 ? (
          <div className="operation-sources">
            {sourceResults.slice(0, 5).map((source) => (
              <div key={source.sourceName}>
                <strong>{source.sourceName}</strong>
                <span>{source.fetchedCount}개 수집 · {source.importedCount}개 저장</span>
              </div>
            ))}
          </div>
        ) : null}
        {state.result && typeof state.result.briefingsGenerated === "number" ? (
          <div className="operation-summary" aria-label="브리핑 생성 결과">
            <strong>{state.result.briefingsGenerated}개 브리핑 생성</strong>
          </div>
        ) : null}
        <pre>{state.result ? JSON.stringify(state.result, null, 2) : "준비됨"}</pre>
      </div>
    </section>
  );
}
