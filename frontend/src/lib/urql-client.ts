'use client'

/**
 * urql クライアント（GraphQL Subscription / WebSocket 接続状態管理）。
 */
import { cacheExchange, Client, fetchExchange, subscriptionExchange } from '@urql/core'
import { createClient as createWSClient, type Client as WSClient } from 'graphql-ws'
import { defaultGraphqlWsUri, graphqlHttpUri } from '@/lib/graphql-endpoints'

export type WsConnectionState = 'connecting' | 'connected' | 'reconnecting' | 'offline'

let wsConnectionState: WsConnectionState = 'connecting'
const wsListeners = new Set<(state: WsConnectionState) => void>()

function setWsConnectionState(next: WsConnectionState) {
  if (wsConnectionState === next) return
  wsConnectionState = next
  wsListeners.forEach((fn) => fn(next))
}

/** 現在の WebSocket 接続状態 */
export function getWsConnectionState(): WsConnectionState {
  return wsConnectionState
}

/** WebSocket 接続状態の変更を購読（解除関数を返す） */
export function subscribeWsConnectionState(listener: (state: WsConnectionState) => void): () => void {
  wsListeners.add(listener)
  listener(wsConnectionState)
  return () => {
    wsListeners.delete(listener)
  }
}

function buildWsClient(wsUrl: string): WSClient {
  return createWSClient({
    url: wsUrl,
    keepAlive: 12_000,
    retryAttempts: Number.POSITIVE_INFINITY,
    shouldRetry: () => true,
    retryWait: async (retries) => {
      setWsConnectionState('reconnecting')
      await new Promise((r) => setTimeout(r, Math.min(1000 + retries * 500, 10_000)))
    },
    on: {
      connected: () => setWsConnectionState('connected'),
      closed: () => setWsConnectionState('reconnecting'),
      error: () => setWsConnectionState('reconnecting'),
    },
  })
}

/** WebSocket 付き urql Client を生成 */
export function createUrqlClient(wsUrl?: string): Client {
  setWsConnectionState('connecting')
  const resolvedWs = wsUrl ?? defaultGraphqlWsUri()
  const wsClient = typeof window !== 'undefined' ? buildWsClient(resolvedWs) : null

  return new Client({
    url: graphqlHttpUri(),
    fetchOptions: { credentials: 'include' },
    exchanges: [
      cacheExchange,
      fetchExchange,
      subscriptionExchange({
        forwardSubscription(request) {
          if (!wsClient) {
            throw new Error('WebSocket subscriptions are only available in the browser')
          }
          const input = { ...request, query: request.query ?? '' }
          return {
            subscribe(sink) {
              const dispose = wsClient.subscribe(input, sink)
              return { unsubscribe: dispose }
            },
          }
        },
      }),
    ],
  })
}

let urql: Client | null = null
let wsUrlUsed = ''

/** @deprecated /api/runtime-config 取得後は createUrqlClient を使用 */
export function getUrqlClient(): Client {
  const ws = defaultGraphqlWsUri()
  if (!urql || wsUrlUsed !== ws) {
    urql = createUrqlClient(ws)
    wsUrlUsed = ws
  }
  return urql
}
