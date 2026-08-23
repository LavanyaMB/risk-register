import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { risksApi } from "../api/client";
import RiskTable from "../components/RiskTable";
import { titleCase } from "../utils/format";
import { CATEGORIES, STATUSES, type Category, type Risk, type Status } from "../types";

export default function Dashboard() {
  const [risks, setRisks] = useState<Risk[]>([]);
  const [category, setCategory] = useState<Category | "">("");
  const [status, setStatus] = useState<Status | "">("");
  const [sortByResidual, setSortByResidual] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    risksApi
      .list({ category, status, sortByResidualDesc: sortByResidual })
      .then((data) => {
        if (!cancelled) setRisks(data);
      })
      .catch((err) => {
        if (!cancelled) setError(err instanceof Error ? err.message : "Failed to load risks");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [category, status, sortByResidual]);

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <p className="eyebrow">Risk Register</p>
          <h1>Risk Dashboard</h1>
        </div>
        <Link to="/risks/new" className="btn btn-primary">
          + New risk
        </Link>
      </header>

      <div className="toolbar">
        <select value={category} onChange={(e) => setCategory(e.target.value as Category | "")}>
          <option value="">All categories</option>
          {CATEGORIES.map((c) => (
            <option key={c} value={c}>
              {titleCase(c)}
            </option>
          ))}
        </select>

        <select value={status} onChange={(e) => setStatus(e.target.value as Status | "")}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>
              {titleCase(s)}
            </option>
          ))}
        </select>

        <div className="spacer" />

        <label style={{ fontSize: 13, color: "var(--color-text-muted)", display: "flex", gap: 6, alignItems: "center" }}>
          <input
            type="checkbox"
            checked={sortByResidual}
            onChange={(e) => setSortByResidual(e.target.checked)}
          />
          Sort by residual score (highest first)
        </label>
      </div>

      {error && <div className="banner banner-error">{error}</div>}

      {loading ? (
        <div className="card empty-state">Loading risks…</div>
      ) : (
        <RiskTable risks={risks} />
      )}
    </div>
  );
}
