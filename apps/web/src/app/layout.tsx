import type { Metadata } from "next";
import { SiteShell } from "../components/site-shell";
import "./globals.css";

export const metadata: Metadata = {
  title: "DevBrief",
  description: "AI and developer news daily briefing dashboard"
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

