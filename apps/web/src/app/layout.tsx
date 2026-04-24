import type { Metadata } from "next";
import { SiteShell } from "../components/site-shell";
import "./globals.css";

export const metadata: Metadata = {
  title: "DevBrief",
  description: "AI와 개발 뉴스를 개발자 액션까지 정리하는 데일리 브리핑",
  icons: {
    icon: "/icon.svg"
  }
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>
        <SiteShell>{children}</SiteShell>
      </body>
    </html>
  );
}
