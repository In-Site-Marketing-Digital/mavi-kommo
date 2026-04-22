import type { AuthStatus, FieldMapping, KommoFieldsResponse } from "@/types";

const BACKEND_URL =
  process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const error = await res.json().catch(() => ({ error: res.statusText }));
    throw new Error(error.error || `HTTP ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  auth: {
    getStatus: (): Promise<AuthStatus> =>
      fetch(`${BACKEND_URL}/auth/status`, { cache: "no-store" }).then(
        handleResponse<AuthStatus>
      ),

    getAuthUrl: () => `${BACKEND_URL}/auth/kommo`,

    disconnect: (): Promise<void> =>
      fetch(`${BACKEND_URL}/auth/disconnect`, { method: "POST" }).then(
        (res) => {
          if (!res.ok) throw new Error("Failed to disconnect");
        }
      ),
  },

  fields: {
    getAll: (): Promise<KommoFieldsResponse> =>
      fetch(`${BACKEND_URL}/kommo/fields`, { cache: "no-store" }).then(
        handleResponse<KommoFieldsResponse>
      ),
  },

  mappings: {
    getAll: (): Promise<FieldMapping[]> =>
      fetch(`${BACKEND_URL}/mappings`, { cache: "no-store" }).then(
        handleResponse<FieldMapping[]>
      ),

    save: (mappings: FieldMapping[]): Promise<FieldMapping[]> =>
      fetch(`${BACKEND_URL}/mappings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ mappings }),
      }).then(handleResponse<FieldMapping[]>),
  },

  pipelines: {
    getAll: (): Promise<import("../types").Pipeline[]> =>
      fetch(`${BACKEND_URL}/kommo/pipelines`, { cache: "no-store" }).then(
        handleResponse<import("../types").Pipeline[]>
      ),
  },

  settings: {
    get: (): Promise<import("../types").IntegrationSettings> =>
      fetch(`${BACKEND_URL}/settings`, { cache: "no-store" }).then(
        handleResponse<import("../types").IntegrationSettings>
      ),

    save: (
      settings: import("../types").IntegrationSettings
    ): Promise<import("../types").IntegrationSettings> =>
      fetch(`${BACKEND_URL}/settings`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(settings),
      }).then(handleResponse<import("../types").IntegrationSettings>),
  },
};
