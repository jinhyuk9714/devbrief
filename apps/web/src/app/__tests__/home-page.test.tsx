import { render, screen, within } from "@testing-library/react";
import HomePage from "../page";
import { sourceStatus, todayBriefings, trendData } from "../../lib/demo-data";

vi.mock("../../lib/api", () => ({
  getTodayBriefings: () => Promise.resolve(todayBriefings),
  getTrends: () => Promise.resolve(trendData),
  getSourceStatus: () => Promise.resolve(sourceStatus)
}));

describe("HomePage", () => {
  it("opens as a briefing desk with a lead story and queue", async () => {
    render(await HomePage());

    expect(screen.getByText("오늘의 AI/개발 브리핑")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "5분 안에 읽고 바로 해볼 것까지 정리합니다" })).toBeInTheDocument();

    const leadStory = screen.getByRole("article", { name: "대표 브리핑" });
    expect(within(leadStory).getByRole("heading", { name: todayBriefings.briefings[0].title })).toBeInTheDocument();
    expect(within(leadStory).getByText("요약")).toBeInTheDocument();
    expect(within(leadStory).getByText("왜 중요")).toBeInTheDocument();
    expect(within(leadStory).getByText("해볼 것")).toBeInTheDocument();

    const queue = screen.getByRole("region", { name: "브리핑 큐" });
    expect(within(queue).getByText(todayBriefings.briefings[1].title)).toBeInTheDocument();
  });

  it("keeps operational cache labels out of the home experience", async () => {
    render(await HomePage());

    expect(screen.queryByText("Redis")).not.toBeInTheDocument();
    expect(screen.queryByText("핫 캐시")).not.toBeInTheDocument();
    expect(screen.queryByText("출처 상태")).not.toBeInTheDocument();
    expect(screen.queryByText("시스템 데모 보기")).not.toBeInTheDocument();
    expect(screen.queryByText("시스템이 브리핑을 만드는 방식")).not.toBeInTheDocument();
  });

  it("keeps concise user value language on the home page", async () => {
    render(await HomePage());

    expect(screen.getByText(/중요 기사와 관련 신호를 읽고/)).toBeInTheDocument();
    expect(screen.queryByText(/여러 출처에서 같은 신호를 묶고/)).not.toBeInTheDocument();
    const valueSection = screen.getByRole("region", { name: "브리핑 구성" });
    expect(within(valueSection).getByText("무슨 일")).toBeInTheDocument();
    expect(within(valueSection).getByText("왜 중요")).toBeInTheDocument();
    expect(within(valueSection).getByText("해볼 것")).toBeInTheDocument();
    expect(within(valueSection).getByRole("heading", { name: "중요 기사를 먼저 읽습니다" })).toBeInTheDocument();
  });
});
