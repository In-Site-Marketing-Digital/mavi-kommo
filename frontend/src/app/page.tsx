"use client";

import { useEffect, useState, useCallback, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { api } from "@/lib/api";
import type { AuthStatus, FieldMapping } from "@/types";

const BACKEND_URL =
  process.env.NEXT_PUBLIC_BACKEND_URL || "http://localhost:8080";

// ── Inner component that uses useSearchParams ────────────────────────────────
function DashboardInner() {
  const searchParams = useSearchParams();
  const [authStatus, setAuthStatus] = useState<AuthStatus | null>(null);
  const [mappings, setMappings] = useState<FieldMapping[]>([]);
  const [loading, setLoading] = useState(true);
  const [disconnecting, setDisconnecting] = useState(false);

  const fetchData = useCallback(async () => {
    try {
      const [status, maps] = await Promise.all([
        api.auth.getStatus(),
        api.mappings.getAll().catch(() => [] as FieldMapping[]),
      ]);
      setAuthStatus(status);
      setMappings(maps);
    } catch {
      toast.error("Erro ao carregar status da conexão");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
    if (searchParams.get("connected") === "true") {
      toast.success("Conta Kommo conectada com sucesso!");
    }
  }, [fetchData, searchParams]);

  const handleDisconnect = async () => {
    setDisconnecting(true);
    try {
      await api.auth.disconnect();
      setAuthStatus({ connected: false });
      toast.info("Conta Kommo desconectada!");
    } catch {
      toast.error("Erro ao desconectar");
    } finally {
      setDisconnecting(false);
    }
  };

  const webhookUrl = `${BACKEND_URL}/webhook/form`;
  const callbackUrl = `${BACKEND_URL}/auth/kommo/callback`;
  const activeMappings = mappings.length;

  return (
    <main className="max-w-5xl mx-auto px-6 py-12 space-y-8">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold tracking-tight gradient-text">
          Dashboard
        </h1>
        <p className="text-muted-foreground">
          Gerencie sua integração entre a Mavi CRM e o Kommo.
        </p>
      </div>

      {/* ── Status Card ─────────────────────────────────────────────────── */}
      <Card className="border-border/60 card-hover">
        <CardHeader className="pb-3">
          <CardTitle className="text-base font-medium">
            Conexão com o Kommo
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {loading ? (
            <div className="flex items-center gap-3">
              <div className="w-3 h-3 rounded-full bg-muted animate-pulse" />
              <span className="text-muted-foreground text-sm">
                Verificando conexão…
              </span>
            </div>
          ) : authStatus?.connected ? (
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span className="relative flex h-3 w-3">
                  <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                  <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-500" />
                </span>
                <div>
                  <p className="text-sm font-medium">Conectado</p>
                  <p className="text-xs text-muted-foreground">
                    {authStatus.accountDomain}
                  </p>
                </div>
                <Badge
                  variant="secondary"
                  className="bg-emerald-500/10 text-emerald-400 border-emerald-500/20"
                >
                  Ativo
                </Badge>
              </div>
              <Button
                variant="outline"
                onClick={handleDisconnect}
                disabled={disconnecting}
                className="border-destructive/40 text-destructive hover:bg-destructive/10"
              >
                {disconnecting ? "Desconectando…" : "Desconectar"}
              </Button>
            </div>
          ) : (
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span className="h-3 w-3 rounded-full bg-muted-foreground/40" />
                <div>
                  <p className="text-sm font-medium">Desconectado</p>
                  <p className="text-xs text-muted-foreground">
                    Conecte sua conta Kommo para começar
                  </p>
                </div>
                <Badge
                  variant="secondary"
                  className="bg-muted text-muted-foreground"
                >
                  Inativo
                </Badge>
              </div>
              <Button
                render={<a href={api.auth.getAuthUrl()} />}
                className="gradient-brand text-white border-0 glow-primary"
              >
                Conectar ao Kommo
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* ── Stats Row ────────────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard
          label="Campos mapeados"
          value={activeMappings}
          icon="🗂️"
          sub={activeMappings === 0 ? "Nenhum configurado" : "ativos"}
        />
        <StatCard
          label="Campos do formulário"
          value={7}
          icon="📋"
          sub="no payload fixo"
        />
        <StatCard
          label="Status do webhook"
          value={authStatus?.connected ? "Pronto" : "Aguardando"}
          icon={authStatus?.connected ? "✅" : "⏳"}
          sub={authStatus?.connected ? "para receber" : "conexão OAuth"}
        />
      </div>

      {/* ── URLs de configuração ─────────────────────────────────────────── */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <UrlCard
          title="Webhook do Formulário"
          description="Configure esta URL no seu formulário externo como destino POST."
          url={webhookUrl}
          method="POST"
          badgeColor="bg-violet-500/10 text-violet-400 border-violet-500/20"
        />
        <UrlCard
          title="Redirect URI — OAuth"
          description="Configure esta URL na integração do Kommo como Redirect URI."
          url={callbackUrl}
          method="GET"
          badgeColor="bg-sky-500/10 text-sky-400 border-sky-500/20"
        />
      </div>

      <Separator className="opacity-30" />

      <div className="flex items-center justify-between">
        <p className="text-sm text-muted-foreground">
          {activeMappings === 0
            ? "Configure os mapeamentos para ativar a integração."
            : `${activeMappings} campo(s) mapeado(s). Você pode editar a qualquer momento.`}
        </p>
        <Button
          render={<Link href="/mappings" />}
          variant="secondary"
        >
          {activeMappings === 0
            ? "Configurar mapeamentos →"
            : "Editar mapeamentos →"}
        </Button>
      </div>
    </main>
  );
}

// ── Page shell with Suspense (required for useSearchParams in Next.js 14) ───
export default function DashboardPage() {
  return (
    <div className="min-h-screen bg-background">
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
            <Button variant="ghost" render={<Link href="/" />}>Dashboard</Button>
            <Button variant="ghost" render={<Link href="/mappings" />}>Mapeamentos</Button>
          </nav>
        </div>
      </header>

      <Suspense
        fallback={
          <main className="max-w-5xl mx-auto px-6 py-12">
            <div className="h-8 w-48 bg-muted/30 rounded animate-pulse mb-4" />
            <div className="h-32 bg-muted/20 rounded-xl animate-pulse" />
          </main>
        }
      >
        <DashboardInner />
      </Suspense>
    </div>
  );
}

// ── Sub-components ──────────────────────────────────────────────────────────

function StatCard({
  label,
  value,
  icon,
  sub,
}: {
  label: string;
  value: string | number;
  icon: string;
  sub: string;
}) {
  return (
    <Card className="border-border/60 card-hover">
      <CardContent className="pt-6">
        <div className="flex items-start justify-between">
          <div>
            <p className="text-xs text-muted-foreground uppercase tracking-widest mb-1">
              {label}
            </p>
            <p className="text-2xl font-bold">{value}</p>
            <p className="text-xs text-muted-foreground mt-1">{sub}</p>
          </div>
          <span className="text-2xl">{icon}</span>
        </div>
      </CardContent>
    </Card>
  );
}

function UrlCard({
  title,
  description,
  url,
  method,
  badgeColor,
}: {
  title: string;
  description: string;
  url: string;
  method: "GET" | "POST";
  badgeColor: string;
}) {
  const handleCopy = () => {
    navigator.clipboard.writeText(url);
    toast.success("URL copiada!");
  };

  return (
    <Card className="border-border/60 card-hover">
      <CardHeader className="pb-2">
        <div className="flex items-center gap-2">
          <CardTitle className="text-sm font-medium">{title}</CardTitle>
          <Badge className={`text-xs ${badgeColor} border`}>{method}</Badge>
        </div>
        <p className="text-xs text-muted-foreground">{description}</p>
      </CardHeader>
      <CardContent>
        <div className="flex items-center gap-2">
          <code className="flex-1 text-xs bg-muted/50 rounded-md px-3 py-2 font-mono text-muted-foreground truncate border border-border/40">
            {url}
          </code>
          <Button variant="outline" onClick={handleCopy}>
            Copiar
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
