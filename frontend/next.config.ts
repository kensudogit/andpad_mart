/**
 * Next.js 設定（統合デプロイの tracing root・Apollo トランスパイル・画像ドメイン）。
 */
import path from 'node:path'
import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  outputFileTracingRoot: path.join(__dirname, '..'),
  transpilePackages: ['@apollo/client'],
  images: {
    remotePatterns: [
      { protocol: 'https', hostname: 'placehold.co', pathname: '/**' },
    ],
  },
}

export default nextConfig
