import { render, screen } from "@testing-library/react";
import { TrendRadar } from "../trend-radar";
import { trendData } from "../../lib/demo-data";

describe("TrendRadar", () => {
  it("groups trends by category with briefing desk labels", () => {
    render(<TrendRadar trends={trendData.trends} />);

    expect(screen.getByText("AI 모델")).toBeInTheDocument();
    expect(screen.getByText("오픈소스")).toBeInTheDocument();
    expect(screen.getByText("보안")).toBeInTheDocument();
    expect(screen.getAllByText("신호 강도").length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText("5개 연결 기사")).toBeInTheDocument();
  });
});
