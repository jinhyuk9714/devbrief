import { render, screen } from "@testing-library/react";
import BriefingDetailPage from "../briefings/[id]/page";
import { briefingDetail } from "../../lib/demo-data";

vi.mock("../../lib/api", () => ({
  getBriefingDetail: () => Promise.resolve(briefingDetail)
}));

describe("BriefingDetailPage", () => {
  it("prioritizes summary, importance, and developer actions before sources", async () => {
    render(await BriefingDetailPage({ params: Promise.resolve({ id: "1" }) }));

    expect(screen.getByText("요약")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "왜 중요한가" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "개발자 액션" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "원문 출처" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "타임라인" })).toBeInTheDocument();
    expect(screen.getByText("이 브리핑은 수집된 여러 원문을 묶어 생성됨")).toBeInTheDocument();
  });
});
