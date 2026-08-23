import type {
  ApiError,
  Category,
  MitigationFormValues,
  Risk,
  RiskFormValues,
  Status,
} from "../types";

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export class ApiRequestError extends Error {
  details: string[];
  status: number;

  constructor(apiError: ApiError) {
    super(apiError.message);
    this.details = apiError.details ?? [];
    this.status = apiError.status;
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  if (!res.ok) {
    let apiError: ApiError;
    try {
      apiError = await res.json();
    } catch {
      throw new Error(`Request failed with status ${res.status}`);
    }
    throw new ApiRequestError(apiError);
  }

  if (res.status === 204) {
    return undefined as T;
  }
  return res.json() as Promise<T>;
}

export interface ListRisksParams {
  category?: Category | "";
  status?: Status | "";
  sortByResidualDesc?: boolean;
}

export const risksApi = {
  list(params: ListRisksParams = {}): Promise<Risk[]> {
    const query = new URLSearchParams();
    if (params.category) query.set("category", params.category);
    if (params.status) query.set("status", params.status);
    if (params.sortByResidualDesc) query.set("sort", "residualScore,desc");
    const qs = query.toString();
    return request<Risk[]>(`/risks${qs ? `?${qs}` : ""}`);
  },

  get(id: string): Promise<Risk> {
    return request<Risk>(`/risks/${id}`);
  },

  create(values: RiskFormValues): Promise<Risk> {
    return request<Risk>(`/risks`, {
      method: "POST",
      body: JSON.stringify(values),
    });
  },

  update(id: string, values: RiskFormValues): Promise<Risk> {
    return request<Risk>(`/risks/${id}`, {
      method: "PUT",
      body: JSON.stringify(values),
    });
  },

  delete(id: string): Promise<void> {
    return request<void>(`/risks/${id}`, { method: "DELETE" });
  },
};

export const mitigationsApi = {
  create(riskId: string, values: MitigationFormValues): Promise<Risk> {
    return request<Risk>(`/risks/${riskId}/mitigations`, {
      method: "POST",
      body: JSON.stringify(values),
    });
  },

  update(riskId: string, mitigationId: string, values: MitigationFormValues): Promise<Risk> {
    return request<Risk>(`/risks/${riskId}/mitigations/${mitigationId}`, {
      method: "PUT",
      body: JSON.stringify(values),
    });
  },

  delete(riskId: string, mitigationId: string): Promise<Risk> {
    return request<Risk>(`/risks/${riskId}/mitigations/${mitigationId}`, {
      method: "DELETE",
    });
  },
};
