import { useState } from "react";
import type { Mitigation } from "../types";
import { formatDate } from "../utils/format";

function EffectivenessDots({ value }: { value: number }) {
  return (
    <span className="effectiveness-dots" title={`Effectiveness: ${value}/5`}>
      {[1, 2, 3, 4, 5].map((i) => (
        <span key={i} className={i <= value ? "filled" : ""} />
      ))}
    </span>
  );
}

interface Props {
  mitigations: Mitigation[];
  onAdd: (description: string, effectiveness: number) => Promise<void>;
  onDelete: (mitigationId: string) => Promise<void>;
}

export default function MitigationPanel({ mitigations, onAdd, onDelete }: Props) {
  const [description, setDescription] = useState("");
  const [effectiveness, setEffectiveness] = useState(3);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleAdd(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onAdd(description, effectiveness);
      setDescription("");
      setEffectiveness(3);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not add mitigation");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      {mitigations.length === 0 ? (
        <p className="text-muted">
          No mitigations yet. A risk needs at least one mitigation before it can be marked Closed.
        </p>
      ) : (
        <div className="mitigation-list">
          {mitigations.map((m) => (
            <div key={m.id} className="mitigation-row">
              <div>
                <div className="desc">{m.description}</div>
                <div className="meta">
                  Added {formatDate(m.createdAt)} · <EffectivenessDots value={m.effectiveness} />
                </div>
              </div>
              <button
                type="button"
                className="icon-btn"
                title="Remove mitigation"
                onClick={() => onDelete(m.id)}
              >
                Remove
              </button>
            </div>
          ))}
        </div>
      )}

      {error && <div className="banner banner-error" style={{ marginTop: 12 }}>{error}</div>}

      <form className="mitigation-form" onSubmit={handleAdd}>
        <div className="field">
          <label htmlFor="mitigation-desc">New mitigation</label>
          <input
            id="mitigation-desc"
            required
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="e.g. Apply latest security patches"
          />
        </div>
        <div className="field" style={{ maxWidth: 160 }}>
          <label htmlFor="mitigation-eff">Effectiveness (1-5)</label>
          <input
            id="mitigation-eff"
            type="number"
            min={1}
            max={5}
            required
            value={effectiveness}
            onChange={(e) => setEffectiveness(Number(e.target.value))}
          />
        </div>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? "Adding…" : "Add mitigation"}
        </button>
      </form>
    </div>
  );
}
