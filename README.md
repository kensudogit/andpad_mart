# ANDPAD intra-mart 版 (`andpad_mart`)

[`andpad_j`](../andpad_j) の **Java バックエンド（`jp.andpad.api`）** を intra-mart 開発環境向けに移植したリポジトリです。  
Spring Boot 3 + GraphQL + Next.js 15 に加え、**intra-mart プラグイン統合レイヤー（`jp.andpad.imart`）** を提供します。

## 構成

| パス | 内容 |
|------|------|
| `backend/` | Spring Boot 3.5 · Java 21 · `jp.andpad.api`（andpad_j から移植） |
| `imart/` | intra-mart 統合モジュール · プラグイン登録 · 開発用スタブ SPI |
| `frontend/` | Next.js 15（andpad_j と同一 UI） |
| `graphql/schema.graphql` | GraphQL スキーマ |
| `docker-compose.yml` | PostgreSQL + API + Web |

## クイックスタート

### 1. PostgreSQL

```powershell
cd C:\devlop\andpad_mart
docker compose up -d db
```

Postgres: `localhost:5435` / user `andpad` / password `andpad` / db `andpad`

### 2. API（Spring Boot + intra-mart 統合）

```powershell
cd C:\devlop\andpad_mart
$env:DATABASE_URL="jdbc:postgresql://localhost:5435/andpad"
$env:DB_USER="andpad"
$env:DB_PASSWORD="andpad"
$env:JWT_SECRET="dev-local-secret-minimum-32-characters"
.\gradlew.bat :backend:bootRun
```

- GraphQL: http://localhost:8080/graphql
- Health: http://localhost:8080/health

### 3. Web（Next.js）

```powershell
cd frontend
npm install
npm run dev
```

http://localhost:3000 — デモ: `demo@sakura-dental.jp` / `demo1234`

### まとめて起動

```powershell
npm install
npm run install:all
npm run dev
```

## intra-mart WAR ビルド

intra-mart Accel Platform / Tomcat へデプロイする WAR:

```powershell
.\gradlew.bat :backend:bootWar
```

成果物: `backend/build/libs/andpad-imart.war`  
コンテキストパス: `/andpad-imart`（`application.yml` の `app.imart.context-path`）

## テスト

```powershell
.\gradlew.bat test
# モジュール単位
.\gradlew.bat :backend:test
.\gradlew.bat :imart:test
```

## intra-mart 統合の仕組み

```
┌─────────────────────────────────────────┐
│         intra-mart Accel Platform       │
├─────────────────────────────────────────┤
│  andpad-imart.war (Spring Boot)         │
│  ├── jp.andpad.imart  (プラグイン登録)   │
│  └── jp.andpad.api    (GraphQL / REST)  │
└─────────────────────────────────────────┘
```

- **`IntraMartContextSpi`**: IM セッション・テナント情報への抽象 IF
- **`DevIntraMartContext`**: ライセンス環境が無いローカル開発用スタブ
- **`IntraMartPluginRegistrar`**: 起動時にプラグイン ID を登録
- **`META-INF/intra-mart/andpad-plugin.xml`**: IM プラグイン定義
- **`IntraMartAuthFilter`**: IM セッション認証（LoginSessionManager 相当）
- **`GenericWorkflowEngine`**: 汎用承認フロー（REST / GraphQL）

本番 IM 環境では `jp.co.intra_mart:intra-mart-core` を `compileOnly` で追加し、
`IntraMartContextSpi` の本番実装クラスを差し替えてください。

### intra-mart 認証・認可

| 設定 | 説明 |
|------|------|
| `app.imart.auth.enabled` | IM セッション認証 ON/OFF（デフォルト **`true`**） |
| `app.imart.auth.mode` | `stub`（ローカル）/ `http`（IM ブリッジ） |
| `IMART_AUTH_ENABLED` | 環境変数（デフォルト `true`） |
| `IMART_AUTH_MODE` | 環境変数（デフォルト `stub`） |
| `IMART_BASE_URL` | `http` モード時の IM ベース URL |

#### 有効化手順

1. **設定ファイル** — `imart/src/main/resources/intramart.properties` で `app.imart.auth.enabled=true`（既定値）
2. **application.yml** — `app.imart.auth.enabled: ${IMART_AUTH_ENABLED:true}` により環境変数で上書き可能
3. **ローカル起動** — 追加設定なしで有効。`.\gradlew.bat :backend:bootRun` 前に `DATABASE_URL` / `JWT_SECRET` を設定
4. **Docker** — `docker compose up` の api サービスに `IMART_AUTH_ENABLED=true` を設定済み
5. **Railway** — `.\scripts\setup-railway.ps1` 実行、または Variables に `IMART_AUTH_ENABLED=true` を設定して Redeploy
6. **動作確認** — 下記 curl で `authenticated: true` を確認

```powershell
# ローカル（デフォルトで有効 — 明示設定は任意）
$env:DATABASE_URL="jdbc:postgresql://localhost:5435/andpad"
$env:JWT_SECRET="dev-local-secret-minimum-32-characters"
# $env:IMART_AUTH_ENABLED="true"   # 省略可（デフォルト true）
# $env:IMART_AUTH_MODE="stub"
.\gradlew.bat :backend:bootRun

# セッション確認
curl -H "X-IM-Session-Id: dev-imart-session" http://localhost:8080/auth/imart/session

# ロール認可
curl -X POST -H "X-IM-Session-Id: dev-imart-session" `
  -H "Content-Type: application/json" `
  -d '{"roleId":"andpad-admin"}' `
  http://localhost:8080/auth/imart/roles/certify
```

#### 無効化

```powershell
$env:IMART_AUTH_ENABLED="false"
# Railway: Variables → IMART_AUTH_ENABLED=false → Redeploy
```

#### 本番 IM 連携（http モード）

```powershell
$env:IMART_AUTH_MODE="http"
$env:IMART_BASE_URL="https://im.example.com"
```

### 汎用ワークフロー

任意の業務エンティティ（予算・休暇・書類など）に紐づけられる承認フローです。

| flowId | 用途 |
|--------|------|
| `generic-single-approval` | 汎用 1 段承認 |
| `generic-two-step-approval` | 汎用 2 段承認 |
| `budget-approval` | 予算承認 |
| `leave-approval` | 休暇申請 |
| `document-approval` | 書類承認 |

```powershell
# JWT 取得後（/auth/login）
$token = "..."  # Bearer トークン

# ワークフロー開始
curl -X POST -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{"flowId":"generic-single-approval","entityId":"doc-001","title":"稟議申請","payload":{"amount":100000}}' `
  http://localhost:8080/api/workflow/instances/start

# 承認待ちタスク
curl -H "Authorization: Bearer $token" http://localhost:8080/api/workflow/tasks/my

# 承認
curl -X POST -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{"action":"APPROVE","comment":"承認"}' `
  http://localhost:8080/api/workflow/tasks/{taskId}/complete
```

GraphQL: `workflowDefinitions` · `startWorkflow` · `myWorkflowTasks` · `completeWorkflowTask`

## `andpad_j` との違い

| 項目 | andpad_j | andpad_mart |
|------|----------|-------------|
| 実行形態 | Spring Boot 単体 | **intra-mart WAR + スタンドアロン** |
| 統合モジュール | なし | **`jp.andpad.imart`** |
| DB ポート（Docker） | 5434 | **5435** |
| パッケージ | `jp.andpad.api` | 同一（移植済み） |

## セットアップスクリプト

```powershell
.\scripts\setup-imart.ps1
```

Docker / Gradle / npm の初期セットアップを自動実行します。
