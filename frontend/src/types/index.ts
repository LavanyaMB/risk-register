export type Category = "OPERATIONAL" | "FINANCIAL" | "COMPLIANCE" | "SECURITY" | "STRATEGIC";
export type Status = "OPEN" | "MITIGATING" | "CLOSED";
export type Severity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";

export const CATEGORIES: Category[] = [
  "OPERATIONAL",
  "FINANCIAL",
  "COMPLIANCE",
  "SECURITY",
  "STRATEGIC",
];

export const STATUSES: Status[] = ["OPEN", "MITIGATING", "CLOSED"];

export interface Mitigation {
  id: string;
  riskId: string;
  description: string;
  effectiveness: number;
  createdAt: string;
}

export interface Risk {
  id: string;
  title: string;
  description: string | null;
  category: Category;
  owner: string | null;
  likelihood: number;
  impact: number;
  status: Status;
  createdAt: string;
  updatedAt: string;
  inherentScore: number;
  inherentSeverity: Severity;
  residualScore: number;
  residualSeverity: Severity;
  mitigationCount: number;
  mitigations: Mitigation[];
}

export interface RiskFormValues {
  title: string;
  description: string;
  category: Category;
  owner: string;
  likelihood: number;
  impact: number;
  status: Status;
}

export interface MitigationFormValues {
  description: string;
  effectiveness: number;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  details: string[];
}
