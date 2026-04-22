export interface AuthStatus {
  connected: boolean;
  accountDomain?: string;
  expiresAt?: string;
}

export interface KommoField {
  id: string;
  name: string;
  fieldType: string;
  fieldTypeCode: number;
  entityType: "lead" | "contact" | "company";
  isStandard: boolean;
  fieldCode?: string;
}

export interface KommoFieldsResponse {
  lead: KommoField[];
  contact: KommoField[];
  company: KommoField[];
}

export interface FieldMapping {
  payloadField: string;
  kommoFieldId: string;
  kommoFieldName: string;
  kommoEntityType: "lead" | "contact" | "company";
  isStandard: boolean;
  kommoFieldType: string;
  fieldCode?: string;
}

export const PAYLOAD_FIELDS = [
  {
    key: "name",
    label: "Nome",
    description: "Nome completo do lead",
    icon: "👤",
  },
  {
    key: "email",
    label: "E-mail",
    description: "Endereço de e-mail",
    icon: "✉️",
  },
  {
    key: "phone",
    label: "Telefone",
    description: "Número de telefone com DDI",
    icon: "📱",
  },
  {
    key: "instagram_handle",
    label: "Instagram",
    description: "Handle do Instagram (@usuario)",
    icon: "📸",
  },
  {
    key: "revenue",
    label: "Faturamento",
    description: "Faixa de faturamento da empresa",
    icon: "💰",
  },
  {
    key: "pt_investment",
    label: "Investimento PT",
    description: "Investimento em personal trainer",
    icon: "🏋️",
  },
  {
    key: "clinic_owner",
    label: "Dono de Clínica",
    description: "É dono de clínica? (true/false)",
    icon: "🏥",
  },
] as const;

export type PayloadFieldKey = (typeof PAYLOAD_FIELDS)[number]["key"];

export interface PipelineStatus {
  id: number;
  name: string;
  color: string;
}

export interface Pipeline {
  id: number;
  name: string;
  isMain: boolean;
  statuses: PipelineStatus[];
}

export interface IntegrationSettings {
  pipelineId?: number | null;
  statusId?: number | null;
}

