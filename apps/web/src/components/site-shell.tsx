import Link from "next/link";
import { Activity, Newspaper } from "lucide-react";

export function SiteShell({ children }: { children: React.ReactNode }) {
  return (
    <div className="shell">
      <header className="topbar">
        <Link className="brand" href="/">
          <Newspaper size={22} aria-hidden />
          <span>DevBrief</span>
        </Link>
        <nav className="nav">
          <Link href="/">오늘</Link>
          <Link href="/trends">트렌드</Link>
          <Link href="/admin">데모 관리</Link>
        </nav>
        <div className="live-chip">
          <Activity size={16} aria-hidden />
          데모 준비됨
        </div>
      </header>
      {children}
    </div>
  );
}
