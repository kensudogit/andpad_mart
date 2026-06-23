/**
 * Railway 生存確認用（Next.js プロセスのみ。Java API は呼ばない）。
 * API 込みの準備完了は /ready または /status で確認する。
 */
export const dynamic = 'force-dynamic'

/** 生存確認 JSON（常に 200 — Railway healthcheck 用） */
export async function GET() {
  const unified =
    process.env.UNIFIED_DEPLOY === '1' || process.env.UNIFIED_DEPLOY === 'true'

  return Response.json({
    ok: true,
    service: 'andpad-web',
    unified,
  })
}
