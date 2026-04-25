export type BriefingSummary = {
  id: number;
  title: string;
  category: string;
  importance: number;
  readingMinutes: number;
  sourceCount: number;
  articleCount: number;
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
    lastFetchStatus: "OK" | "DEMO" | "FALLBACK" | "FAILED" | string | null;
    lastFetchMessage: string | null;
    lastArticleCount: number;
    lastUsedFallback: boolean;
  }>;
  cache: Record<string, unknown>;
};

export type SourceResult = {
  sourceName: string;
  status: "OK" | "DEMO" | "FALLBACK" | "FAILED" | string;
  fetchedCount: number;
  importedCount: number;
  fallbackUsed: boolean;
  message: string;
};

export type AdminActionResult = {
  sourcesChecked?: number;
  articlesImported?: number;
  failedSources?: string[];
  sourceResults?: SourceResult[];
  briefingsGenerated?: number;
  [key: string]: unknown;
};
