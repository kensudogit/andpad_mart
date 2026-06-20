'use client'

/**
 * SaaS モジュールハブ（建設・共通モジュールの一覧と有効/無効切替）。
 */
import Link from 'next/link'
import { useQuery } from '@apollo/client/react'
import { BrandLogo } from '@/components/BrandLogo'
import { SaasModuleCard } from '@/components/SaasModuleCard'
import { businessModules } from '@/lib/business-modules'
import { constructionModules } from '@/lib/construction-modules'
import { CurrentSessionDocument, SaasModulesDocument, MemberRole } from '@/lib/generated/graphql'
import { isAuthRequiredGraphQLError } from '@/lib/graphql-errors'
import { ui } from '@/lib/ui'

/** OWNER / ADMIN のみモジュール有効化を変更可能 */
function canManageModules(role: MemberRole | undefined): boolean {
  return role === MemberRole.Owner || role === MemberRole.Admin
}

/** SaaS モジュール一覧ハブ */
export function SaasHubClient() {
  const { data: sessionData } = useQuery(CurrentSessionDocument, { fetchPolicy: 'cache-first' })
  const { data, loading, error } = useQuery(SaasModulesDocument, {
    fetchPolicy: 'cache-first',
  })

  const modules = data?.saasModules ?? []
  const role = sessionData?.currentSession?.role
  const canToggle = canManageModules(role)

  if (loading && !data) {
    return (
      <div className="saas-hub-loading">
        <BrandLogo size={56} animated />
        <p className="muted">{ui.boardLoading}</p>
      </div>
    )
  }

  if (error) {
    const authRequired = isAuthRequiredGraphQLError(error)
    return (
      <div className="alert">
        <p>{authRequired ? ui.saasLoginHint : error.message}</p>
        {authRequired && (
          <Link href="/login" className="btn">
            {ui.loginSubmit}
          </Link>
        )}
      </div>
    )
  }

  const enabledByCode = new Map(modules.map((m) => [m.code, m.enabled]))
  const descriptionByCode = new Map(modules.map((m) => [m.code, m.description]))

  return (
    <>
      <header className="saas-hub-hero">
        <BrandLogo size={72} animated className="saas-hub-hero-logo" />
        <div className="saas-hub-hero-text">
          <h1>{ui.saasHubTitle}</h1>
          <p>{ui.saasHubDesc}</p>
        </div>
      </header>

      <p className="muted saas-hub-hint">
        {canToggle ? ui.saasToggleHint : ui.saasToggleViewOnly}
      </p>

      <h2 className="saas-section-title">{ui.saasBusinessModules}</h2>
      <div className="saas-grid">
        {businessModules.map((item, index) => (
          <SaasModuleCard
            key={item.code}
            item={item}
            index={index}
            description={descriptionByCode.get(item.code) ?? item.description}
            enabled={enabledByCode.get(item.code) ?? false}
            canToggle={canToggle}
          />
        ))}
      </div>

      <h2 className="saas-section-title">{ui.saasConstructionModules}</h2>
      <div className="saas-grid">
        {constructionModules.map((item, index) => (
          <SaasModuleCard
            key={item.code}
            item={item}
            index={index}
            description={descriptionByCode.get(item.code) ?? ''}
            enabled={enabledByCode.get(item.code) ?? false}
            canToggle={canToggle}
          />
        ))}
      </div>
    </>
  )
}
