import { Link, useNavigate } from "react-router-dom";
import { risksApi } from "../api/client";
import RiskForm from "../components/RiskForm";
import type { RiskFormValues } from "../types";

export default function NewRisk() {
  const navigate = useNavigate();

  async function handleSubmit(values: RiskFormValues) {
    const created = await risksApi.create(values);
    navigate(`/risks/${created.id}`);
  }

  return (
    <div className="app-shell">
      <div className="breadcrumb">
        <Link to="/">← Back to dashboard</Link>
      </div>
      <header className="app-header">
        <h1>New risk</h1>
      </header>
      <div className="card" style={{ padding: 20 }}>
        <RiskForm submitLabel="Create risk" onSubmit={handleSubmit} onCancel={() => navigate("/")} />
      </div>
    </div>
  );
}
