import { Link } from "react-router-dom";
import type { Risk } from "../types";
import SeverityBadge from "./SeverityBadge";
import { titleCase } from "../utils/format";

export default function RiskTable({ risks }: { risks: Risk[] }) {
  if (risks.length === 0) {
    return (
      <div className="card empty-state">
        No risks match the current filters. Try clearing a filter, or create a new risk to get started.
      </div>
    );
  }

  return (
    <div className="card" style={{ overflowX: "auto" }}>
      <table className="risk-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Category</th>
            <th>Status</th>
            <th>Inherent</th>
            <th>Residual</th>
            <th>Mitigations</th>
          </tr>
        </thead>
        <tbody>
          {risks.map((risk) => (
            <tr key={risk.id}>
              <td className="risk-title-cell">
                <Link to={`/risks/${risk.id}`}>{risk.title}</Link>
                {risk.description && <div className="desc">{risk.description}</div>}
              </td>
              <td>
                <span className="pill">{titleCase(risk.category)}</span>
              </td>
              <td>
                <span className="pill">{titleCase(risk.status)}</span>
              </td>
              <td>
                <SeverityBadge severity={risk.inherentSeverity} score={risk.inherentScore} />
              </td>
              <td>
                <SeverityBadge severity={risk.residualSeverity} score={risk.residualScore} />
              </td>
              <td>{risk.mitigationCount}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
