"use client";

import { useState } from "react";
import { RefreshCcw, Wand2 } from "lucide-react";
import { runAdminAction } from "../lib/api";

type ActionState = {
  label: string;
  result: string;
  loading: boolean;
};

export function AdminPanel() {
  const [state, setState] = useState<ActionState>({ label: "대기 중", result: "준비됨", loading: false });

  async function run(label: string, path: "/api/admin/ingest/run" | "/api/admin/briefings/generate") {
    setState({ label, result: "실행 중", loading: true });
    try {
      const result = await runAdminAction(path);
      setState({ label, result: JSON.stringify(result, null, 2), loading: false });
    } catch (error) {
      setState({ label, result: error instanceof Error ? error.message : "요청 실패", loading: false });
    }
  }

  return (
    <section className="admin-panel">
      <div className="section-heading">
        <h1>데모 운영</h1>
        <p>시스템 상태와 수집 파이프라인을 확인합니다.</p>
      </div>
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
        <pre>{state.result}</pre>
      </div>
    </section>
  );
}
