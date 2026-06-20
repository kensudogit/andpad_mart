/**
 * 建設モジュール以外の共通 SaaS 定義（チャットボット等）。
 */
import { SaasModuleCode } from '@/lib/generated/graphql'
import { ui } from '@/lib/ui'

/** 共通 SaaS モジュール定義（チャットボット等） */
export const businessModules = [
  {
    slug: 'chatbot' as const,
    code: SaasModuleCode.Chatbot,
    label: ui.saasChat,
    description: ui.saasChatDesc,
    icon: 'AI',
    tone: 'violet' as const,
    href: '/saas/chatbot',
  },
] as const

export type BusinessModuleSlug = (typeof businessModules)[number]['slug']
