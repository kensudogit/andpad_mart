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

本番 IM 環境では `jp.co.intra_mart:intra-mart-core` を `compileOnly` で追加し、
`IntraMartContextSpi` の本番実装クラスを差し替えてください。

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
# andpad_mart
