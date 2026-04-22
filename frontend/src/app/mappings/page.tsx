"use client";

import { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { api } from "@/lib/api";
import {
  PAYLOAD_FIELDS,
  type FieldMapping,
  type KommoField,
  type KommoFieldsResponse,
  type Pipeline,
  type IntegrationSettings,
} from "@/types";

type MappingDraft = {
  payloadField: string;
  selectedFieldId: string; // composite "entityType::fieldId"
};

function buildCompositeId(field: KommoField): string {
  return `${field.entityType}::${field.id}`;
}

function parseCompositeId(
  compositeId: string,
  fields: KommoFieldsResponse
): KommoField | null {
  if (!compositeId) return null;
  const [entityType, ...rest] = compositeId.split("::");
  const fieldId = rest.join("::");
  const list =
    fields[entityType as keyof KommoFieldsResponse] as KommoField[] | undefined;
  return list?.find((f) => f.id === fieldId) ?? null;
}

export default function MappingsPage() {
  const [kommoFields, setKommoFields] = useState<KommoFieldsResponse | null>(
    null
  );
  const [drafts, setDrafts] = useState<MappingDraft[]>(
    PAYLOAD_FIELDS.map((f) => ({ payloadField: f.key, selectedFieldId: "" }))
  );
  const [pipelines, setPipelines] = useState<Pipeline[]>([]);
  const [activePipelineId, setActivePipelineId] = useState<string>("");
  const [activeStatusId, setActiveStatusId] = useState<string>("");

  const [loadingFields, setLoadingFields] = useState(true);
  const [saving, setSaving] = useState(false);
  const [connected, setConnected] = useState<boolean | null>(null);

  const fetchAll = useCallback(async () => {
    try {
      const status = await api.auth.getStatus();
      setConnected(status.connected);

      if (!status.connected) {
        setLoadingFields(false);
        return;
      }

      const [fields, saved, fetchedPipelines, settings] = await Promise.all([
        api.fields.getAll(),
        api.mappings.getAll(),
        api.pipelines.getAll().catch(() => []),
        api.settings.get().catch(() => ({}) as IntegrationSettings),
      ]);
      setKommoFields(fields);
      setPipelines(fetchedPipelines as Pipeline[]);

      if (settings.pipelineId) setActivePipelineId(String(settings.pipelineId));
      if (settings.statusId) setActiveStatusId(String(settings.statusId));

      if (saved.length > 0) {
        setDrafts(
          PAYLOAD_FIELDS.map((pf) => {
            const existing = saved.find((m) => m.payloadField === pf.key);
            return {
              payloadField: pf.key,
              selectedFieldId: existing
                ? `${existing.kommoEntityType}::${existing.kommoFieldId}`
                : "",
            };
          })
        );
      }
    } catch {
      toast.error("Erro ao carregar campos do Kommo");
    } finally {
      setLoadingFields(false);
    }
  }, []);

  useEffect(() => {
    fetchAll();
  }, [fetchAll]);

  const handleChange = (payloadField: string, compositeId: string) => {
    setDrafts((prev) =>
      prev.map((d) =>
        d.payloadField === payloadField
          ? { ...d, selectedFieldId: compositeId }
          : d
      )
    );
  };

  const handleSave = async () => {
    if (!kommoFields) return;
    setSaving(true);
    try {
      const mappings: FieldMapping[] = drafts
        .filter((d) => d.selectedFieldId)
        .map((d) => {
          const field = parseCompositeId(d.selectedFieldId, kommoFields)!;
          return {
            payloadField: d.payloadField,
            kommoFieldId: field.id,
            kommoFieldName: field.name,
            kommoEntityType: field.entityType,
            isStandard: field.isStandard,
            kommoFieldType: field.fieldType,
            fieldCode: field.fieldCode,
          };
        });

      const settingsPayload: IntegrationSettings = {
        pipelineId: activePipelineId ? parseInt(activePipelineId) : null,
        statusId: activeStatusId ? parseInt(activeStatusId) : null,
      };

      await Promise.all([
        api.mappings.save(mappings),
        api.settings.save(settingsPayload),
      ]);

      toast.success("Configurações salvas com sucesso!");
    } catch (err: unknown) {
      toast.error(err instanceof Error ? err.message : "Erro ao salvar");
    } finally {
      setSaving(false);
    }
  };

  const mappedCount = drafts.filter((d) => d.selectedFieldId).length;

  return (
    <div className="min-h-screen bg-background">
      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <header className="border-b border-border/50 backdrop-blur-sm sticky top-0 z-10">
        <div className="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg gradient-brand glow-primary flex items-center justify-center">
              <span className="text-white text-sm font-bold">M</span>
            </div>
            <span className="font-semibold text-foreground tracking-tight">
              Mavi Kommo Integrator
            </span>
          </div>
          <nav className="flex items-center gap-2">
            <Button variant="ghost" render={<Link href="/" />}>
              Dashboard
            </Button>
            <Button variant="ghost" render={<Link href="/mappings" />}>
              Mapeamentos
            </Button>
          </nav>
        </div>
      </header>

      <main className="max-w-5xl mx-auto px-6 py-12 space-y-8">
        <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4">
          <div className="space-y-1">
            <h1 className="text-3xl font-bold tracking-tight gradient-text">
              Mapeamento de Campos
            </h1>
            <p className="text-muted-foreground text-sm">
              Relacione cada campo do formulário ao campo correspondente no
              Kommo.
            </p>
          </div>
          {connected && (
            <div className="flex items-center gap-3">
              <span className="text-sm text-muted-foreground">
                {mappedCount}/{PAYLOAD_FIELDS.length} campos mapeados
              </span>
              <Button
                onClick={handleSave}
                disabled={saving || loadingFields}
                className="gradient-brand text-white border-0 glow-primary min-w-[120px]"
              >
                {saving ? "Salvando…" : "Salvar"}
              </Button>
            </div>
          )}
        </div>

        {/* ── Not connected notice ────────────────────────────────────────── */}
        {connected === false && (
          <Card className="border-destructive/30 bg-destructive/5">
            <CardContent className="pt-6 flex flex-col sm:flex-row items-start sm:items-center gap-4">
              <span className="text-2xl">🔌</span>
              <div className="flex-1">
                <p className="font-medium">Conta Kommo não conectada</p>
                <p className="text-sm text-muted-foreground">
                  Conecte sua conta Kommo no Dashboard antes de configurar os
                  mapeamentos.
                </p>
              </div>
              <Button
                render={<Link href="/" />}
                className="gradient-brand text-white border-0"
              >
                Ir para o Dashboard
              </Button>
            </CardContent>
          </Card>
        )}

        {/* ── Loading state ───────────────────────────────────────────────── */}
        {loadingFields && connected !== false && (
          <div className="grid gap-3">
            {[1, 2, 3].map((k) => (
              <div
                key={k}
                className="h-20 rounded-xl bg-muted/30 animate-pulse border border-border/30"
              />
            ))}
          </div>
        )}

        {/* ── Pipeline Selection ──────────────────────────────────────────── */}
        {!loadingFields && connected && kommoFields && (
          <Card className="border-border/60 card-hover overflow-hidden mb-6 bg-card/60">
            <CardHeader className="pb-3 border-b border-border/30 bg-muted/20">
              <div className="flex items-center gap-2">
                <span className="text-xl">🎯</span>
                <CardTitle className="text-base font-medium">
                  Destino do Lead
                </CardTitle>
              </div>
              <p className="text-xs text-muted-foreground mt-1">
                Escolha em qual Funil e Etapa os novos Leads deverão ser
                criados automaticamente.
              </p>
            </CardHeader>
            <CardContent className="pt-5">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                  <Label>Funil (Pipeline)</Label>
                  <Select
                    value={activePipelineId}
                    onValueChange={(val) => {
                      setActivePipelineId(val ?? "");
                      setActiveStatusId(""); // reset status when pipeline changes
                    }}
                  >
                    <SelectTrigger className="bg-muted/30 border-border/50">
                      <SelectValue placeholder="Selecione um funil…" />
                    </SelectTrigger>
                    <SelectContent>
                      {pipelines.map((p) => (
                        <SelectItem key={p.id} value={String(p.id)}>
                          {p.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Etapa (Status)</Label>
                  <Select
                    value={activeStatusId}
                    onValueChange={(val) => setActiveStatusId(val ?? "")}
                    disabled={!activePipelineId}
                  >
                    <SelectTrigger className="bg-muted/30 border-border/50">
                      <SelectValue placeholder="Selecione uma etapa…" />
                    </SelectTrigger>
                    <SelectContent>
                      {pipelines
                        .find((p) => String(p.id) === activePipelineId)
                        ?.statuses.map((s) => (
                          <SelectItem key={s.id} value={String(s.id)}>
                            <div className="flex items-center gap-2">
                              <span
                                className="w-3 h-3 rounded-full border border-border/50"
                                style={{ backgroundColor: s.color }}
                              />
                              {s.name}
                            </div>
                          </SelectItem>
                        ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

        {/* ── Mapping rows ────────────────────────────────────────────────── */}
        {!loadingFields && connected && kommoFields && (
          <div className="space-y-3">
            <div className="hidden sm:grid grid-cols-[1fr_auto_1fr] gap-4 px-1">
              <p className="text-xs uppercase tracking-widest text-muted-foreground font-medium">
                Campo do Formulário
              </p>
              <div className="w-6" />
              <p className="text-xs uppercase tracking-widest text-muted-foreground font-medium">
                Campo do Kommo
              </p>
            </div>

            {PAYLOAD_FIELDS.map((field) => {
              const draft = drafts.find((d) => d.payloadField === field.key)!;
              const selectedField = draft.selectedFieldId
                ? parseCompositeId(draft.selectedFieldId, kommoFields)
                : null;

              return (
                <Card
                  key={field.key}
                  className="border-border/60 card-hover overflow-hidden"
                >
                  <CardContent className="pt-4 pb-4">
                    <div className="grid grid-cols-1 sm:grid-cols-[1fr_auto_1fr] gap-3 items-center">
                      {/* Left — payload field info */}
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-lg bg-accent/60 flex items-center justify-center text-lg shrink-0">
                          {field.icon}
                        </div>
                        <div>
                          <p className="font-medium text-sm">{field.label}</p>
                          <p className="text-xs text-muted-foreground">
                            {field.description}
                          </p>
                          <code className="text-xs text-primary/70 font-mono">
                            {field.key}
                          </code>
                        </div>
                      </div>

                      {/* Arrow */}
                      <div className="hidden sm:flex items-center justify-center">
                        <span className="text-muted-foreground/40 text-lg">
                          →
                        </span>
                      </div>

                      {/* Right — kommo field select */}
                      <div className="space-y-1.5">
                        <Label className="sr-only">
                          Campo Kommo para {field.label}
                        </Label>
                        <div className="flex items-center gap-2">
                          <Select
                            value={draft.selectedFieldId}
                            onValueChange={(val) =>
                              handleChange(field.key, val ?? "")
                            }
                          >
                            <SelectTrigger className="flex-1 bg-muted/30 border-border/50">
                              <SelectValue placeholder="Selecione um campo…" />
                            </SelectTrigger>
                            <SelectContent className="max-h-72">
                              <SelectGroup>
                                <SelectLabel className="text-xs text-violet-400">
                                  📊 Lead
                                </SelectLabel>
                                {kommoFields.lead.map((f) => (
                                  <SelectItem
                                    key={buildCompositeId(f)}
                                    value={buildCompositeId(f)}
                                  >
                                    {f.name}
                                    {f.isStandard && (
                                      <span className="text-muted-foreground ml-1 text-xs">
                                        · padrão
                                      </span>
                                    )}
                                  </SelectItem>
                                ))}
                              </SelectGroup>
                              <SelectGroup>
                                <SelectLabel className="text-xs text-sky-400">
                                  👤 Contato
                                </SelectLabel>
                                {kommoFields.contact.map((f) => (
                                  <SelectItem
                                    key={buildCompositeId(f)}
                                    value={buildCompositeId(f)}
                                  >
                                    {f.name}
                                    {f.isStandard && (
                                      <span className="text-muted-foreground ml-1 text-xs">
                                        · padrão
                                      </span>
                                    )}
                                  </SelectItem>
                                ))}
                              </SelectGroup>
                              <SelectGroup>
                                <SelectLabel className="text-xs text-amber-400">
                                  🏢 Empresa
                                </SelectLabel>
                                {kommoFields.company.map((f) => (
                                  <SelectItem
                                    key={buildCompositeId(f)}
                                    value={buildCompositeId(f)}
                                  >
                                    {f.name}
                                    {f.isStandard && (
                                      <span className="text-muted-foreground ml-1 text-xs">
                                        · padrão
                                      </span>
                                    )}
                                  </SelectItem>
                                ))}
                              </SelectGroup>
                            </SelectContent>
                          </Select>

                          {draft.selectedFieldId && (
                            <button
                              type="button"
                              className="text-muted-foreground hover:text-destructive px-2 py-1 rounded text-sm transition-colors"
                              onClick={() => handleChange(field.key, "")}
                            >
                              ✕
                            </button>
                          )}
                        </div>

                        {selectedField && (
                          <div className="flex items-center gap-1.5 pl-1">
                            <Badge
                              variant="secondary"
                              className={`text-xs border ${
                                selectedField.entityType === "lead"
                                  ? "bg-violet-500/10 text-violet-400 border-violet-500/20"
                                  : selectedField.entityType === "contact"
                                  ? "bg-sky-500/10 text-sky-400 border-sky-500/20"
                                  : "bg-amber-500/10 text-amber-400 border-amber-500/20"
                              }`}
                            >
                              {selectedField.entityType === "lead"
                                ? "Lead"
                                : selectedField.entityType === "contact"
                                ? "Contato"
                                : "Empresa"}
                            </Badge>
                            <Badge
                              variant="secondary"
                              className="text-xs bg-muted text-muted-foreground border border-border/40"
                            >
                              {selectedField.fieldType}
                            </Badge>
                            {selectedField.isStandard && (
                              <Badge
                                variant="secondary"
                                className="text-xs bg-muted text-muted-foreground border border-border/40"
                              >
                                padrão
                              </Badge>
                            )}
                          </div>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>
        )}

        {/* ── Footer action bar ───────────────────────────────────────────── */}
        {connected && !loadingFields && kommoFields && (
          <>
            <Separator className="opacity-30" />
            <div className="flex items-center justify-between">
              <p className="text-sm text-muted-foreground">
                {mappedCount === 0
                  ? "Nenhum campo mapeado ainda."
                  : `${mappedCount} de ${PAYLOAD_FIELDS.length} campos configurados.`}
              </p>
              <Button
                onClick={handleSave}
                disabled={saving}
                className="gradient-brand text-white border-0 glow-primary min-w-[120px]"
              >
                {saving ? "Salvando…" : "Salvar mapeamentos"}
              </Button>
            </div>
          </>
        )}
      </main>
    </div>
  );
}
