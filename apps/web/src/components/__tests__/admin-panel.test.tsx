import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { AdminPanel } from "../admin-panel";

const { runAdminAction } = vi.hoisted(() => ({
  runAdminAction: vi.fn()
}));

vi.mock("../../lib/api", () => ({
  runAdminAction
}));

describe("AdminPanel", () => {
  beforeEach(() => {
    runAdminAction.mockReset();
    window.sessionStorage.clear();
  });

  it("renders Korean operation labels", () => {
    render(<AdminPanel />);

    expect(screen.getByRole("button", { name: "수집 실행" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "브리핑 생성" })).toBeInTheDocument();
    expect(screen.getByLabelText("관리 토큰")).toBeInTheDocument();
    expect(screen.getByText("시스템 상태와 수집 파이프라인을 확인합니다.")).toBeInTheDocument();
    expect(screen.getByText("대기 중")).toBeInTheDocument();
  });

  it("requires a token before running protected admin actions", async () => {
    render(<AdminPanel />);

    await userEvent.click(screen.getByRole("button", { name: "수집 실행" }));

    expect(runAdminAction).not.toHaveBeenCalled();
    expect(screen.getByText("관리 토큰이 필요합니다.")).toBeInTheDocument();
  });

  it("summarizes ingestion results before raw details", async () => {
    runAdminAction.mockResolvedValue({
      sourcesChecked: 2,
      articlesImported: 3,
      failedSources: ["Broken Feed"],
      sourceResults: [
        {
          sourceName: "Working Feed",
          status: "DEMO",
          fetchedCount: 3,
          importedCount: 3,
          fallbackUsed: true,
          message: "네트워크 비활성화로 데모 데이터를 사용했습니다."
        }
      ]
    });

    render(<AdminPanel />);
    await userEvent.type(screen.getByLabelText("관리 토큰"), "secret-token");
    await userEvent.click(screen.getByRole("button", { name: "수집 실행" }));

    await waitFor(() => {
      expect(runAdminAction).toHaveBeenCalledWith("/api/admin/ingest/run", "secret-token");
      expect(screen.getByText("2개 출처 확인")).toBeInTheDocument();
      expect(screen.getByText("3개 신규 저장")).toBeInTheDocument();
      expect(screen.getByText("실패 1개")).toBeInTheDocument();
      expect(screen.getByText("Working Feed")).toBeInTheDocument();
    });
  });
});
