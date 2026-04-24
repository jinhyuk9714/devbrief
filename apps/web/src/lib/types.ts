export type BriefingSummary = {
  id: number;
  title: string;
  category: string;
  importance: number;
  readingMinutes: number;
  sourceCount: number;
  summary: string;
  whyItMatters: string;
  actionItems: string[];
};

export type TodayBriefings = {
  generatedAt: string;
  briefings: BriefingSummary[];
};

export type ArticleLink = {
  title: string;
  url: string;
  sourceName: string;
  author: string;
  publishedAt: string;
  excerpt: string;
};

export type TimelineItem = {
  at: string;
  source: string;
  title: string;
};

export type BriefingDetail = BriefingSummary & {
  keyPoints: string[];
  riskNotes: string[];
  sources: ArticleLink[];
  timeline: TimelineItem[];
};

export type TrendItem = {
  category: string;
  title: string;
  score: number;
  articleCount: number;
  lastSeenAt: string;
};

export type TrendResponse = {
  range: "day" | "week" | string;
  trends: TrendItem[];
  categories: Record<string, number>;
};

export type SourceStatus = {
  sources: Array<{
    name: string;
    type: string;
    category: string;
    enabled: boolean;
    lastFetchedAt: string | null;
  }>;
  cache: Record<string, unknown>;
};

