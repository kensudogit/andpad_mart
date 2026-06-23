/**
 * 統合デプロイ時の Java API 起動ログ（サーバー側のみ）。
 */
import fs from 'node:fs'

const LOG_PATHS = ['/app/data/api-startup.log', '/app/data/api.log', '/tmp/api.log'] as const

/** Java API 起動ログの末尾を読み取る */
export function readApiStartupLogTail(maxLines = 40): string | undefined {
  for (const path of LOG_PATHS) {
    try {
      if (!fs.existsSync(path)) continue
      const text = fs.readFileSync(path, 'utf8')
      const lines = text.split(/\r?\n/).filter((line) => line.trim().length > 0)
      if (lines.length === 0) continue
      return lines.slice(-maxLines).join('\n')
    } catch {
      // ignore unreadable paths
    }
  }
  return undefined
}

/** ログ内容から起動失敗の原因ヒントを推定 */
export function inferApiStartupHint(log?: string): string | undefined {
  if (!log) return undefined
  const lower = log.toLowerCase()
  if (lower.includes('outofmemoryerror') || lower.includes('native memory') || lower.includes('killed process')) {
    return 'Java API がメモリ不足で終了した可能性があります。Railway のプランを上げるか、再デプロイ後に /status を確認してください。'
  }
  if (lower.includes('flyway') && (lower.includes('failed') || lower.includes('error'))) {
    return 'Flyway マイグレーションが失敗しています。下の起動ログを確認してください。'
  }
  if (lower.includes('connection refused') || lower.includes('could not connect to server')) {
    return 'PostgreSQL に接続できません。DATABASE_URL の Reference と Postgres サービスの起動を確認してください。'
  }
  if (lower.includes('password authentication failed')) {
    return 'PostgreSQL の認証に失敗しています。DATABASE_URL の Reference を付け直して Redeploy してください。'
  }
  return undefined
}
