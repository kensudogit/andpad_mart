/**
 * 統合デプロイの準備完了確認（Next.js + Java API）。
 * Railway の healthcheck には使わない（/health を使用）。
 */
import { dbConfigured } from '@/lib/status-check'

export const dynamic = 'force-dynamic'

async function javaApiHealthy(): Promise<boolean> {
  const port = process.env.API_INTERNAL_PORT?.trim() || '8081'
  try {
    const res = await fetch(`http://127.0.0.1:${port}/health`, {
      cache: 'no-store',
      signal: AbortSignal.timeout(4000),
    })
    if (!res.ok) return false
    const body = (await res.json()) as { ok?: boolean }
    return body.ok === true
  } catch {
    return false
  }
}

function unifiedApiStatusMessage(): string {
  const status = process.env.UNIFIED_API_STATUS?.trim()
  if (status === 'missing_database_url' || !dbConfigured()) {
    return 'DATABASE_URL is not configured — add Postgres Reference on the app service, then Redeploy.'
  }
  if (status === 'unresolved_database_url') {
    return 'DATABASE_URL reference is unresolved (${{...}}). Fix the variable reference and Redeploy.'
  }
  if (status === 'api_exited') {
    return 'Java API exited during startup. Check Railway Deploy logs for [unified] ERROR.'
  }
  if (status === 'api_timeout') {
    return 'Java API did not become ready within 180s. Check Deploy logs and DATABASE_URL.'
  }
  return 'Java API is not responding on 127.0.0.1:8081.'
}

/** フルスタック準備完了 JSON */
export async function GET() {
  const unified =
    process.env.UNIFIED_DEPLOY === '1' || process.env.UNIFIED_DEPLOY === 'true'

  if (!unified) {
    return Response.json({
      ok: true,
      service: 'andpad-web',
      unified: false,
    })
  }

  const javaOk = await javaApiHealthy()
  if (javaOk) {
    return Response.json({
      ok: true,
      service: 'andpad-web',
      unified: true,
      api: 'ready',
    })
  }

  return Response.json(
    {
      ok: false,
      service: 'andpad-web',
      unified: true,
      api: 'unavailable',
      error: unifiedApiStatusMessage(),
    },
    { status: 503 },
  )
}
