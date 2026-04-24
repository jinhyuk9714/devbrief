import { render, screen } from "@testing-library/react";
import { AdminPanel } from "../admin-panel";

describe("AdminPanel", () => {
  it("renders Korean operation labels", () => {
    render(<AdminPanel />);

    expect(screen.getByRole("button", { name: "수집 실행" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "브리핑 생성" })).toBeInTheDocument();
    expect(screen.getByText("대기 중")).toBeInTheDocument();
  });
});
