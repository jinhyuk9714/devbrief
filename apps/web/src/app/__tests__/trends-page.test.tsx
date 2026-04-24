import { render, screen } from "@testing-library/react";
import TrendsPage from "../trends/page";
import { trendData } from "../../lib/demo-data";

const { getTrends } = vi.hoisted(() => ({
  getTrends: vi.fn()
}));

vi.mock("../../lib/api", () => ({
  getTrends: (range: "day" | "week") => getTrends(range)
}));

describe("TrendsPage", () => {
  it("connects the range switch to the trends query", async () => {
    getTrends.mockResolvedValue(trendData);

    render(await TrendsPage({ searchParams: Promise.resolve({ range: "day" }) }));

    expect(getTrends).toHaveBeenCalledWith("day");
    expect(screen.getByRole("link", { name: "일간" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "주간" })).toBeInTheDocument();
    expect(screen.getAllByText("신호 강도").length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText(/연결 기사/).length).toBeGreaterThanOrEqual(1);
  });
});
