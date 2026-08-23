import type { Severity } from "../types";

const LABELS: Record<Severity, string> = {
  LOW: "Low",
  MEDIUM: "Medium",
  HIGH: "High",
  CRITICAL: "Critical",
};

export default function SeverityBadge({ severity, score }: { severity: Severity; score: number }) {
  return (
    <span className={`severity-badge severity-${severity}`} title={`${LABELS[severity]} severity`}>
      {LABELS[severity]}
      <span className="score">{score}</span>
    </span>
  );
}
