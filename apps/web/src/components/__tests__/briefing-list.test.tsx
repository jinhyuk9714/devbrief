import { render, screen } from "@testing-library/react";
import { BriefingList } from "../briefing-list";
import { todayBriefings } from "../../lib/demo-data";

describe("BriefingList", () => {
  it("renders briefing cards as news summaries with explicit labels", () => {
    render(<BriefingList briefings={todayBriefings.briefings} />);

    expect(screen.getByText("AI 모델")).toBeInTheDocument();
    expect(screen.getByText(/중요도 92/)).toBeInTheDocument();
    expect(screen.getAllByText(/4분 읽기/).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText(/3개 출처/)).toBeInTheDocument();
    expect(screen.getAllByText("요약").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("왜 중요").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText("해볼 것").length).toBeGreaterThanOrEqual(1);
  });
});
