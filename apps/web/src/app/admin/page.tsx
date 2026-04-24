import { AdminPanel } from "../../components/admin-panel";
import { SourceStatus } from "../../components/source-status";
import { getSourceStatus } from "../../lib/api";

export default async function AdminPage() {
  const status = await getSourceStatus();

  return (
    <main className="simple-page">
      <AdminPanel />
      <SourceStatus status={status} />
    </main>
  );
}

