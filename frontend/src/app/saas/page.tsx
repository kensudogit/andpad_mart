/**
 * SaaS モジュールハブページ（/saas）。
 */
import { SaasHubClient } from '@/components/SaasHubClient'

export const dynamic = 'force-dynamic'

/** SaaS ハブページ */
export default function SaasHubPage() {
  return <SaasHubClient />
}
