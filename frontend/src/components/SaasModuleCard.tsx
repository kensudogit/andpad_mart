'use client'

/**
 * SaaS モジュールカード（有効/無効トグルは AJAX + キャッシュ更新で当該カードのみ再描画）。
 */
import Link from 'next/link'
import { memo, useEffect, useState } from 'react'
import { useMutation } from '@apollo/client/react'
import {
  SaasModuleCode,
  SaasModulesDocument,
  SetSaasModuleEnabledDocument,
} from '@/lib/generated/graphql'
import { saasModuleArtUrl } from '@/lib/saas-module-art'
import { ui } from '@/lib/ui'

export type SaasModuleCardItem = {
  slug: string
  href: string
  code: SaasModuleCode
  label: string
  icon: string
  tone: string
}

type SaasModuleCardProps = {
  item: SaasModuleCardItem
  description: string
  enabled: boolean
  canToggle: boolean
  index: number
}

/** 単一 SaaS モジュールカード */
export const SaasModuleCard = memo(function SaasModuleCard({
  item,
  description,
  enabled: enabledFromServer,
  canToggle,
  index,
}: SaasModuleCardProps) {
  const [enabled, setEnabled] = useState(enabledFromServer)
  const [toggling, setToggling] = useState(false)
  const [setModuleEnabled] = useMutation(SetSaasModuleEnabledDocument)

  useEffect(() => {
    setEnabled(enabledFromServer)
  }, [enabledFromServer])

  async function toggle(next: boolean) {
    if (!canToggle || toggling) return

    const previous = enabled
    setEnabled(next)
    setToggling(true)

    try {
      await setModuleEnabled({
        variables: { code: item.code, enabled: next },
        optimisticResponse: {
          setSaasModuleEnabled: {
            __typename: 'SaasModule',
            code: item.code,
            name: item.label,
            description,
            enabled: next,
          },
        },
        update(cache, { data }) {
          if (!data) return
          cache.updateQuery({ query: SaasModulesDocument }, (existing) => {
            if (!existing?.saasModules) return existing
            return {
              ...existing,
              saasModules: existing.saasModules.map((m) =>
                m.code === item.code
                  ? { ...m, enabled: data.setSaasModuleEnabled.enabled }
                  : m,
              ),
            }
          })
        },
      })
    } catch {
      setEnabled(previous)
    } finally {
      setToggling(false)
    }
  }

  return (
    <article
      className={`saas-card saas-card--${item.tone}${enabled ? '' : ' disabled'}`}
      style={{ animationDelay: `${index * 0.04}s` }}
    >
      <div
        className="saas-card-art"
        style={{ backgroundImage: `url(${saasModuleArtUrl(item.slug)})` }}
        aria-hidden
      />
      <div className="saas-card-inner">
        <div className="saas-card-head">
          <span className={`saas-card-icon saas-card-icon--${item.tone}`} aria-hidden>
            {item.icon}
          </span>
          <div className="saas-card-body">
            <Link href={enabled ? item.href : '/saas'} className="saas-card-link">
              <h3>{item.label}</h3>
              <p>{description}</p>
            </Link>
          </div>
          {canToggle ? (
            <label className="saas-toggle" title={enabled ? ui.saasDisable : ui.saasEnable}>
              <input
                type="checkbox"
                checked={enabled}
                disabled={toggling}
                onChange={(e) => toggle(e.target.checked)}
              />
              <span className="saas-toggle-ui" aria-hidden />
            </label>
          ) : null}
        </div>
        <div className="saas-card-foot">
          <span className={`saas-badge${enabled ? ' on' : ''}`}>
            {enabled ? ui.saasEnabled : ui.saasDisabled}
          </span>
          {enabled ? (
            <Link href={item.href} className="saas-open-link">
              {ui.saasOpen}
              <span aria-hidden>›</span>
            </Link>
          ) : null}
        </div>
      </div>
    </article>
  )
})
