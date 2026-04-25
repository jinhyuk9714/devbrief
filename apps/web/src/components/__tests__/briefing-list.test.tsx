import { render, screen, within } from "@testing-library/react";
import { BriefingList } from "../briefing-list";
import { todayBriefings } from "../../lib/demo-data";

describe("BriefingList", () => {
  it("renders a lead story and compact queue for the briefing desk", () => {
    render(<BriefingList briefings={todayBriefings.briefings} />);

    const leadStory = screen.getByRole("article", { name: "대표 브리핑" });
    expect(within(leadStory).getByText("AI 모델")).toBeInTheDocument();
    expect(within(leadStory).getByText(/중요도 92/)).toBeInTheDocument();
    expect(within(leadStory).getByText(/4분 읽기/)).toBeInTheDocument();
    expect(within(leadStory).getByText(/3개 출처/)).toBeInTheDocument();
    expect(within(leadStory).getByText("요약")).toBeInTheDocument();
    expect(within(leadStory).getByText("왜 중요")).toBeInTheDocument();
    expect(within(leadStory).getByText("해볼 것")).toBeInTheDocument();

    const queue = screen.getByRole("region", { name: "브리핑 큐" });
    expect(within(queue).getByText(todayBriefings.briefings[1].title)).toBeInTheDocument();
    expect(within(queue).getAllByText(/개 출처/).length).toBeGreaterThanOrEqual(1);
  });

  it("labels single-source briefings without implying a multi-source cluster", () => {
    render(<BriefingList briefings={[{ ...todayBriefings.briefings[0], sourceCount: 1, articleCount: 1 }]} />);

    const leadStory = screen.getByRole("article", { name: "대표 브리핑" });
    expect(within(leadStory).getByText(/단일 출처/)).toBeInTheDocument();
    expect(within(leadStory).queryByText(/1개 출처/)).not.toBeInTheDocument();
  });
});
