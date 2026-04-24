import { render, screen } from "@testing-library/react";
import { SourceStatus } from "../source-status";
import type { SourceStatus as SourceStatusType } from "../../lib/types";

const status: SourceStatusType = {
  sources: [
    {
      name: "OpenAI Blog",
      type: "RSS",
      category: "AI 모델",
      enabled: true,
      lastFetchedAt: "2026-04-24T08:30:00Z",
      lastFetchStatus: "OK",
      lastFetchMessage: "RSS 수집 성공",
      lastArticleCount: 4,
      lastUsedFallback: false
    },
    {
      name: "GitHub Trending",
      type: "API",
      category: "오픈소스",
      enabled: true,
      lastFetchedAt: "2026-04-24T08:30:00Z",
      lastFetchStatus: "DEMO",
      lastFetchMessage: "네트워크 비활성화로 데모 데이터를 사용했습니다.",
      lastArticleCount: 2,
      lastUsedFallback: true
    },
    {
      name: "Broken Feed",
      type: "RSS",
      category: "개발 도구",
      enabled: true,
      lastFetchedAt: null,
      lastFetchStatus: "FALLBACK",
      lastFetchMessage: "RSS 실패 후 대체 데이터를 사용했습니다.",
      lastArticleCount: 2,
      lastUsedFallback: true
    },
    {
      name: "Empty Feed",
      type: "RSS",
      category: "보안",
      enabled: true,
      lastFetchedAt: null,
      lastFetchStatus: "FAILED",
      lastFetchMessage: "가져올 수 있는 기사가 없습니다.",
      lastArticleCount: 0,
      lastUsedFallback: false
    }
  ],
  cache: {
    redis: "demo",
    hotBriefingCache: true
  }
};

describe("SourceStatus", () => {
  it("shows Korean fetch status badges and source reliability details", () => {
    render(<SourceStatus status={status} />);

    expect(screen.getByText("정상")).toBeInTheDocument();
    expect(screen.getByText("데모")).toBeInTheDocument();
    expect(screen.getByText("대체 데이터")).toBeInTheDocument();
    expect(screen.getByText("실패")).toBeInTheDocument();
    expect(screen.getByText("4개")).toBeInTheDocument();
    expect(screen.getAllByText("대체 사용").length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText("RSS 수집 성공")).toBeInTheDocument();
    expect(screen.getByText("네트워크 비활성화로 데모 데이터를 사용했습니다.")).toBeInTheDocument();
    expect(screen.getByText("가져올 수 있는 기사가 없습니다.")).toBeInTheDocument();
  });
});
