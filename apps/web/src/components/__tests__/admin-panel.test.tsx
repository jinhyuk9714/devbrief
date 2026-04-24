import { render, screen } from "@testing-library/react";
import { AdminPanel } from "../admin-panel";

describe("AdminPanel", () => {
  it("renders Korean operation labels", () => {
    render(<AdminPanel />);

    expect(screen.getByRole("button", { name: "수집 실행" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "브리핑 생성" })).toBeInTheDocument();
    expect(screen.getByText("시스템 상태와 수집 파이프라인을 확인합니다.")).toBeInTheDocument();
    expect(screen.getByText("대기 중")).toBeInTheDocument();
  });
});
