/** ブラウザセッション用 JWT（HttpOnly Cookie の補完・GraphQL Authorization 用） */
const STORAGE_KEY = 'dv_auth_token'

/** ログイン後 JWT を sessionStorage に保存 */
export function setAuthToken(token: string): void {
  if (typeof window === 'undefined') return
  sessionStorage.setItem(STORAGE_KEY, token)
}

/** GraphQL Authorization ヘッダ用トークンを取得 */
export function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null
  return sessionStorage.getItem(STORAGE_KEY)
}

/** ログアウト時にトークンを削除 */
export function clearAuthToken(): void {
  if (typeof window === 'undefined') return
  sessionStorage.removeItem(STORAGE_KEY)
}
