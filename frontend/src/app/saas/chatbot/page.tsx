/**
 * AI チャットボットページ（/saas/chatbot）。
 */
import { ChatModuleClient } from '@/components/ChatModuleClient'

export const dynamic = 'force-dynamic'

/** チャットボットページ */
export default function ChatbotPage() {
  return <ChatModuleClient />
}
