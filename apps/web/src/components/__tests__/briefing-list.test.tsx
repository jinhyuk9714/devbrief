import { render, screen } from "@testing-library/react";
import { BriefingList } from "../briefing-list";
import { todayBriefings } from "../../lib/demo-data";

describe("BriefingList", () => {
  it("renders importance, category, reading time, and source count", () => {
    render(<BriefingList briefings={todayBriefings.briefings} />);

    expect(screen.getByText("AI 모델")).toBeInTheDocument();
    expect(screen.getByText(/92/)).toBeInTheDocument();
    expect(screen.getAllByText(/4분/).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/3개 출처/)).toBeInTheDocument();
  });
});
