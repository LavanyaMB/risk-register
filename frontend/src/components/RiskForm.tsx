import { useState } from "react";
import { CATEGORIES, STATUSES, type RiskFormValues } from "../types";
import SeverityBadge from "./SeverityBadge";
import { titleCase } from "../utils/format";
import type { Severity } from "../types";

function severityFor(score: number): Severity {
  if (score <= 5) return "LOW";
  if (score <= 12) return "MEDIUM";
  if (score <= 19) return "HIGH";
  return "CRITICAL";
}

interface Props {
  initial?: RiskFormValues;
  submitLabel: string;
  onSubmit: (values: RiskFormValues) => Promise<void>;
  onCancel?: () => void;
}

const DEFAULTS: RiskFormValues = {
  title: "",
  description: "",
  category: "OPERATIONAL",
  owner: "",
  likelihood: 3,
  impact: 3,
  status: "OPEN",
};

export default function RiskForm({ initial, submitLabel, onSubmit, onCancel }: Props) {
  const [values, setValues] = useState<RiskFormValues>(initial ?? DEFAULTS);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const inherentScore = values.likelihood * values.impact;

  function update<K extends keyof RiskFormValues>(key: K, value: RiskFormValues[K]) {
    setValues((v) => ({ ...v, [key]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit(values);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Something went wrong");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="banner banner-error">{error}</div>}

      <div className="form-grid">
        <div className="field full">
          <label htmlFor="title">Title</label>
          <input
            id="title"
            required
            value={values.title}
            onChange={(e) => update("title", e.target.value)}
            placeholder="e.g. Unpatched production database"
          />
        </div>

        <div className="field full">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            value={values.description}
            onChange={(e) => update("description", e.target.value)}
            placeholder="What's the risk, and why does it matter?"
          />
        </div>

        <div className="field">
          <label htmlFor="category">Category</label>
          <select
            id="category"
            value={values.category}
            onChange={(e) => update("category", e.target.value as RiskFormValues["category"])}
          >
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {titleCase(c)}
              </option>
            ))}
          </select>
        </div>

        <div className="field">
          <label htmlFor="owner">Owner</label>
          <input
            id="owner"
            value={values.owner}
            onChange={(e) => update("owner", e.target.value)}
            placeholder="e.g. Infra Team"
          />
        </div>

        <div className="field">
          <label htmlFor="likelihood">Likelihood (1-5)</label>
          <input
            id="likelihood"
            type="number"
            min={1}
            max={5}
            required
            value={values.likelihood}
            onChange={(e) => update("likelihood", Number(e.target.value))}
          />
        </div>

        <div className="field">
          <label htmlFor="impact">Impact (1-5)</label>
          <input
            id="impact"
            type="number"
            min={1}
            max={5}
            required
            value={values.impact}
            onChange={(e) => update("impact", Number(e.target.value))}
          />
        </div>

        <div className="field">
          <label htmlFor="status">Status</label>
          <select
            id="status"
            value={values.status}
            onChange={(e) => update("status", e.target.value as RiskFormValues["status"])}
          >
            {STATUSES.map((s) => (
              <option key={s} value={s}>
                {titleCase(s)}
              </option>
            ))}
          </select>
        </div>

        <div className="field full">
          <div className="inherent-live">
            <span>Inherent score updates live as you change likelihood / impact:</span>
            <SeverityBadge severity={severityFor(inherentScore)} score={inherentScore} />
          </div>
        </div>
      </div>

      <div className="form-actions">
        {onCancel && (
          <button type="button" className="btn" onClick={onCancel}>
            Cancel
          </button>
        )}
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? "Saving…" : submitLabel}
        </button>
      </div>
    </form>
  );
}
