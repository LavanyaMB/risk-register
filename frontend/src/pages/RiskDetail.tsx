import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { mitigationsApi, risksApi } from "../api/client";
import SeverityBadge from "../components/SeverityBadge";
import RiskForm from "../components/RiskForm";
import MitigationPanel from "../components/MitigationPanel";
import { titleCase } from "../utils/format";
import type { Risk, RiskFormValues } from "../types";

export default function RiskDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [risk, setRisk] = useState<Risk | null>(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function load() {
    if (!id) return;
    setLoading(true);
    risksApi
      .get(id)
      .then(setRisk)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load risk"))
      .finally(() => setLoading(false));
  }

  useEffect(load, [id]);

  async function handleUpdate(values: RiskFormValues) {
    if (!id) return;
    const updated = await risksApi.update(id, values);
    setRisk(updated);
    setEditing(false);
  }

  async function handleDelete() {
    if (!id) return;
    if (!confirm("Delete this risk and all of its mitigations? This can't be undone.")) return;
    await risksApi.delete(id);
    navigate("/");
  }

  async function handleAddMitigation(description: string, effectiveness: number) {
    if (!id) return;
    const updated = await mitigationsApi.create(id, { description, effectiveness });
    setRisk(updated);
  }

  async function handleDeleteMitigation(mitigationId: string) {
    if (!id) return;
    const updated = await mitigationsApi.delete(id, mitigationId);
    setRisk(updated);
  }

  if (loading) {
    return (
      <div className="app-shell">
        <div className="card empty-state">Loading…</div>
      </div>
    );
  }

  if (error || !risk) {
    return (
      <div className="app-shell">
        <div className="breadcrumb">
          <Link to="/">← Back to dashboard</Link>
        </div>
        <div className="banner banner-error">{error ?? "Risk not found"}</div>
      </div>
    );
  }

  return (
    <div className="app-shell">
      <div className="breadcrumb">
        <Link to="/">← Back to dashboard</Link>
      </div>

      {editing ? (
        <div className="card" style={{ padding: 20 }}>
          <h1 style={{ fontSize: 18, marginTop: 0 }}>Edit risk</h1>
          <RiskForm
            submitLabel="Save changes"
            initial={{
              title: risk.title,
              description: risk.description ?? "",
              category: risk.category,
              owner: risk.owner ?? "",
              likelihood: risk.likelihood,
              impact: risk.impact,
              status: risk.status,
            }}
            onSubmit={handleUpdate}
            onCancel={() => setEditing(false)}
          />
        </div>
      ) : (
        <>
          <div className="detail-header">
            <div>
              <h1>{risk.title}</h1>
              {risk.description && <p className="text-muted" style={{ maxWidth: 640 }}>{risk.description}</p>}
              <div className="detail-meta">
                <span className="pill">{titleCase(risk.category)}</span>
                <span className="pill">{titleCase(risk.status)}</span>
                {risk.owner && <span className="pill">Owner: {risk.owner}</span>}
              </div>
            </div>
            <div style={{ display: "flex", gap: 8 }}>
              <button className="btn" onClick={() => setEditing(true)}>
                Edit
              </button>
              <button className="btn btn-danger" onClick={handleDelete}>
                Delete
              </button>
            </div>
          </div>

          <div className="score-grid">
            <div className="score-box">
              <div className="label">Inherent risk</div>
              <div className="value-row">
                <span className="number">{risk.inherentScore}</span>
                <SeverityBadge severity={risk.inherentSeverity} score={risk.inherentScore} />
              </div>
            </div>
            <div className="score-box">
              <div className="label">Residual risk</div>
              <div className="value-row">
                <span className="number">{risk.residualScore}</span>
                <SeverityBadge severity={risk.residualSeverity} score={risk.residualScore} />
              </div>
            </div>
          </div>

          <h2 className="section-title">Mitigations ({risk.mitigationCount})</h2>
          <MitigationPanel
            mitigations={risk.mitigations}
            onAdd={handleAddMitigation}
            onDelete={handleDeleteMitigation}
          />
        </>
      )}
    </div>
  );
}
