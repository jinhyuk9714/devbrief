import { render, screen, within } from "@testing-library/react";
import HomePage from "../page";
import { sourceStatus, todayBriefings, trendData } from "../../lib/demo-data";

vi.mock("../../lib/api", () => ({
  getTodayBriefings: () => Promise.resolve(todayBriefings),
  getTrends: () => Promise.resolve(trendData),
  getSourceStatus: () => Promise.resolve(sourceStatus)
}));

describe("HomePage", () => {
  it("explains the service with a clear news briefing hero", async () => {
    render(await HomePage());

    expect(screen.getByText("AI/개발 뉴스 데일리 브리핑")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "오늘 나온 AI/개발 뉴스를 5분 안에 이해하세요" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "오늘 브리핑 보기" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "시스템 데모 보기" })).toBeInTheDocument();
  });

  it("keeps operational cache labels out of the home experience", async () => {
    render(await HomePage());

    expect(screen.queryByText("Redis")).not.toBeInTheDocument();
    expect(screen.queryByText("핫 캐시")).not.toBeInTheDocument();
    expect(screen.queryByText("출처 상태")).not.toBeInTheDocument();
  });

  it("shows the user value before the system implementation flow", async () => {
    render(await HomePage());

    const valueSection = screen.getByRole("region", { name: "브리핑 구성" });
    expect(within(valueSection).getByRole("heading", { name: "무슨 일" })).toBeInTheDocument();
    expect(within(valueSection).getByRole("heading", { name: "왜 중요" })).toBeInTheDocument();
    expect(within(valueSection).getByRole("heading", { name: "해볼 것" })).toBeInTheDocument();
    expect(screen.getByText("시스템이 브리핑을 만드는 방식")).toBeInTheDocument();
  });
});
