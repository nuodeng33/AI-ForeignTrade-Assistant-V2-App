# AI Foreign Trade Assistant V2 App

AI Foreign Trade Assistant V2 App 是面向外贸业务场景的 Android + Strapi 项目，应用名为“贸联智盒”。当前代码已经进入 V3 foundation 阶段：Android 端完成了基础环境分层、登录会话、订单 Repository/ViewModel、订单列表、创建和删除闭环；后端提供 Strapi 订单 content-type，并已补齐订单用户归属、删除权限校验和更新禁用策略。

## 当前状态

已完成或已有基础：

- Android Kotlin + XML 页面结构。
- Gradle product flavors：`dev`、`qa`、`prod`。
- `BuildConfig.API_BASE_URL` 管理 API 地址。
- Retrofit / OkHttp / Gson 网络层。
- JWT 通过共享 OkHttp client 自动注入 `Authorization` header。
- SessionManager 使用 EncryptedSharedPreferences，并保留 legacy SharedPreferences 兼容写入。
- 订单列表使用 RecyclerView 动态展示。
- 订单创建与 Strapi REST API 对接。
- 订单删除使用 Strapi 5 `documentId`。
- Strapi order 增加 `user` 关系，controller 按当前登录用户过滤订单。
- 订单详情和删除会校验当前用户归属，其他用户访问返回 `404`。
- 订单更新当前被禁用，`PUT/PATCH orders/{documentId}` 返回 `405` 和 `ORDER_UPDATE_DISABLED`。
- dev 环境保留模拟订单 fallback，qa/prod 环境接口失败时不展示模拟订单，会显示真实错误或空态。
- Android CI 已覆盖 unit test、lint、dev debug 构建。
- Backend CI 已覆盖 `npm ci` 和 `npm run build`。

进行中：

- PR #6 正在为订单 ownership controller 补自动化测试，并在 Backend CI 中增加 `npm test`。该 PR 尚未合并到 `main`。

仍需继续推进：

- `prod` flavor 仍使用 `https://api.example.com/api/` 占位地址，发布前必须替换为真实 HTTPS 域名。
- Strapi Users & Permissions 后台仍需为 Authenticated 角色开启 order `find`、`findOne`、`create`、`delete` 权限，并保持 `update` 关闭。
- 个人中心、物流详情仍有模拟数据。
- 订单缺分页、搜索、服务端筛选和更完整的业务字段。
- 头像选择仍需迁移到 Activity Result API / Android Photo Picker。
- 本地 Android + Strapi + MySQL 的完整联动仍需要在开发机上验收。

## 技术栈

Android：

- Kotlin
- Gradle Kotlin DSL
- Android Gradle Plugin 8.13.0
- Kotlin 2.0.21
- XML Layout、AppCompat、Material Components、ConstraintLayout
- Retrofit、OkHttp、Gson Converter
- Kotlin Coroutines
- AndroidX Lifecycle
- RecyclerView
- MPAndroidChart
- WebView
- EncryptedSharedPreferences

Backend：

- Strapi 5.31.x
- Node.js >=20 <=24
- Users & Permissions plugin
- MySQL driver dependency is present (`mysql2`)

## 项目结构

```text
.
├── app/                         Android app
├── backend/                     Strapi backend
├── docs/api-contract.md         Android 与后端接口契约
├── .github/workflows/           CI workflows
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

## Android 运行方式

环境要求：

- Android Studio 最新稳定版或较新版本
- JDK 17 或 Android Studio 内置 JDK
- Android SDK 36
- 可访问 Strapi 后端服务的网络环境

步骤：

```bash
git clone https://github.com/nuodeng33/AI-ForeignTrade-Assistant-V2-App.git
cd AI-ForeignTrade-Assistant-V2-App
./gradlew assembleDevDebug
```

在 Android Studio 中打开项目根目录，选择 `devDebug`、`qaDebug` 或 `prodRelease` 等变体运行。

API 地址位于 `app/build.gradle.kts` 的 product flavors：

- `dev`: `http://192.168.1.3:1337/api/`
- `qa`: `http://192.168.1.3:1337/api/`
- `prod`: `https://api.example.com/api/`

本地联调时需要把 dev 的 `API_BASE_URL` 改成 Android 设备可访问的 Strapi 地址：

- 真机同一 Wi-Fi：使用电脑局域网 IP，例如 `http://192.168.1.3:1337/api/`。
- Android Emulator：通常使用 `http://10.0.2.2:1337/api/`。

发布前必须替换 `prod` 地址，并确保生产环境关闭明文流量。

## Backend 运行方式

环境要求：

- Node.js >=20.0.0 <=24.x.x
- npm
- 可用数据库配置。默认 Strapi 项目可按本地配置启动，生产环境需配置数据库、密钥和部署变量。

步骤：

```bash
cd backend
npm ci
npm run develop
```

构建检查：

```bash
cd backend
npm run build
```

MySQL 联调时，在 `backend/.env` 中显式配置：

```env
DATABASE_CLIENT=mysql
DATABASE_HOST=127.0.0.1
DATABASE_PORT=3306
DATABASE_NAME=strapi
DATABASE_USERNAME=strapi
DATABASE_PASSWORD=strapi
DATABASE_SSL=false
```

订单权限配置：

1. 启动 Strapi admin。
2. 打开 Settings -> Users & Permissions plugin -> Roles -> Authenticated。
3. 为 order 开启 `find`、`findOne`、`create`、`delete`。
4. 保持 order `update` 关闭。即使误开，后端当前也会返回 `405`，但权限仍不应开放。
5. 保存后，用两个不同用户分别创建订单，验证列表和删除只能操作自己的订单。

## 接口契约

接口细节见 `docs/api-contract.md`。关键约定：

- Android 请求订单接口时必须携带 JWT。
- Android 创建订单时不传 `user` 字段。
- 后端从 `ctx.state.user` 写入订单归属。
- 后端查询详情、列表和删除都按当前用户过滤。
- 删除订单使用 Strapi 5 `documentId`，不是数字 `id`。
- 更新订单当前不支持，`PUT/PATCH orders/{documentId}` 返回 `405 Method Not Allowed`。

## 本地联动验收

建议用 `devDebug` 验证 Android + Strapi + MySQL：

1. 启动 MySQL，确认数据库和账号可用。
2. 配置 `backend/.env`，确保 `DATABASE_CLIENT=mysql`。
3. 启动 Strapi：`cd backend && npm run develop`。
4. 在 Strapi admin 中配置 Authenticated 角色的 order 权限。
5. 在 Android Studio 运行 `devDebug`。
6. 注册用户 A，登录后创建订单。
7. 验证 A 能看到并删除自己的订单。
8. 注册用户 B，验证 B 看不到 A 的订单，也不能删除 A 的订单。
9. 验证 `PUT/PATCH orders/{documentId}` 返回 `405`。

## CI

当前 GitHub Actions：

- Android：`testDevDebugUnitTest`、`lintDevDebug`、`assembleDevDebug`。
- Backend：`npm ci`、`npm run build`。
- PR #6 合并后，Backend CI 将额外运行 `npm test`。

## 后续优先级

1. 合并 PR #6，让订单 ownership 自动化测试进入 `main`。
2. 替换生产 API 域名并完成 HTTPS 发布配置。
3. 在本地完成 Android devDebug + Strapi + MySQL 的真实联动验收。
4. 改造个人中心真实接口和 ViewModel 状态层。
5. 接入真实物流模型与物流查询接口。
6. 引入 DataStore/Room 缓存订单、聊天和用户资料。
7. 迁移头像选择到 Android Photo Picker。
