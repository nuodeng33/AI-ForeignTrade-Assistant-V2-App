# AI Foreign Trade Assistant V2 App

## 1. 项目简介

AI Foreign Trade Assistant V2 App 是一个面向外贸业务场景的 Android Kotlin 应用，应用名称为“贸联智盒”。项目围绕外贸服务、智能客服、订单管理、物流查询、个人中心等业务能力展开，目标是为外贸从业者提供咨询、服务浏览、订单创建和物流辅助查询的一体化移动端工具。

当前版本更接近 V2 功能原型：主要页面和业务流程已经具备，后端对接方向以 Strapi 接口为主，同时包含 DeepSeek 智能问答能力。项目后续 V3 的重点应放在正式环境配置、安全、架构分层、真实数据接入和工程质量提升上。

## 2. 技术栈

- 开发语言：Kotlin
- 平台：Android
- 构建工具：Gradle Kotlin DSL
- Android Gradle Plugin：8.13.0
- Kotlin：2.0.21
- UI：XML Layout、AppCompat、Material Components、ConstraintLayout
- 网络请求：Retrofit、OkHttp、Gson Converter
- 异步处理：Kotlin Coroutines
- 生命周期：AndroidX Lifecycle
- 列表组件：RecyclerView
- 图表组件：MPAndroidChart
- 网页展示：WebView
- 本地存储：SharedPreferences
- 后端接口风格：Strapi REST API

## 3. 功能模块

### 启动与认证

- 启动页 `SplashActivity`
- 登录页 `LoginActivity`
- 注册页 `RegisterActivity`
- 登录态保存与检查
- 退出登录与会话清理

### 服务中心

- 服务首页 `ServiceActivity`
- 市场调研服务入口
- 报关清关服务入口
- 物流解决方案入口
- 外贸法律咨询入口
- 服务详情 WebView 展示 `ServiceDetailActivity`

### 智能客服

- 智能客服页面 `CustomerServiceActivity`
- 快捷问题入口
- 用户消息与 AI 消息列表展示
- DeepSeek 外贸问答接口
- 流式输出效果

### 联系我们

- 联系页面 `ContactActivity`
- 底部导航联动

### 个人中心

- 个人中心页面 `PersonalCenterActivity`
- 用户名展示
- 咨询数量、订单数量等统计展示
- 咨询类型图表展示
- 头像选择
- 我的订单入口

### 订单管理

- 我的订单页面 `MyOrderActivity`
- 从后端加载订单列表
- 创建订单页面 `CreateOrderActivity`
- 提交订单到 Strapi 后端
- 订单状态展示
- 快递单号复制、分享、查询

### 物流详情

- 物流详情页面 `LogisticsDetailActivity`
- 展示订单号、配送时间、配送路线、实时位置
- 当前主要为模拟数据

### 设置

- 设置页面 `SettingActivity`
- 退出登录
- 清除本地用户会话

## 4. 项目结构

```text
.
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/maolianzhihe/
        │   ├── BaseActivity.kt
        │   ├── SplashActivity.kt
        │   ├── LoginActivity.kt
        │   ├── RegisterActivity.kt
        │   ├── ServiceActivity.kt
        │   ├── CustomerServiceActivity.kt
        │   ├── ContactActivity.kt
        │   ├── PersonalCenterActivity.kt
        │   ├── SettingActivity.kt
        │   ├── MyOrderActivity.kt
        │   ├── CreateOrderActivity.kt
        │   ├── ServiceDetailActivity.kt
        │   ├── LogisticsDetailActivity.kt
        │   ├── ChatAdapter.kt
        │   ├── model/
        │   │   ├── AuthResponse.kt
        │   │   └── Order.kt
        │   └── network/
        │       ├── ApiService.kt
        │       └── StreamService.kt
        └── res/
            ├── layout/
            ├── drawable/
            ├── mipmap-*/
            └── values/
```

### 关键文件说明

- `AndroidManifest.xml`：声明权限、应用主题、Activity 列表和启动入口。
- `BaseActivity.kt`：封装标题栏和底部导航逻辑。
- `ApiService.kt`：Retrofit 接口定义，包含登录、注册、订单、AI 问答等接口。
- `StreamService.kt`：OkHttp 流式 AI 问答请求实现。
- `Order.kt`：Strapi 订单接口相关数据模型。
- `AuthResponse.kt`：登录/注册认证响应模型。

## 5. 运行方式

### 环境要求

- Android Studio 最新稳定版或较新版本
- JDK 17 或 Android Studio 内置 JDK
- Android SDK 36
- Gradle Wrapper 或 Android Studio 自动同步 Gradle
- 可访问后端服务的网络环境

### 运行步骤

1. 克隆项目：

```bash
git clone https://github.com/nuodeng33/AI-ForeignTrade-Assistant-V2-App.git
```

2. 使用 Android Studio 打开项目根目录。

3. 等待 Gradle 同步完成。

4. 确认后端服务地址可访问。

当前代码中后端地址位于：

- `app/src/main/java/com/example/maolianzhihe/network/ApiService.kt`
- `app/src/main/java/com/example/maolianzhihe/network/StreamService.kt`

当前配置为局域网 HTTP 地址，运行前需要确保 Android 设备或模拟器能访问这些 IP。

5. 选择模拟器或真机运行 `app` 模块。

### 注意事项

- 当前项目使用 HTTP 明文请求，并在 Manifest 中开启了 `usesCleartextTraffic`。
- 如果使用模拟器访问本机服务，通常不能直接使用局域网 IP，需要根据实际网络环境调整。
- 如果后端未启动，登录、注册、订单、AI 问答等功能会请求失败。

## 6. 当前问题

1. 后端地址硬编码

`ApiService.kt` 和 `StreamService.kt` 中的 Base URL 写死为局域网 IP，不适合多人协作、测试环境切换和正式发布。

2. 两个网络服务地址不一致

普通接口和流式 AI 接口使用了不同的局域网 IP，容易导致部分功能可用、部分功能不可用。

3. HTTP 明文请求存在安全风险

当前使用 `http://`，并开启 `android:usesCleartextTraffic="true"`，生产环境应切换到 HTTPS。

4. Token 未统一注入请求头

登录后保存了 JWT，但订单等接口请求未看到统一添加 `Authorization` Header 的逻辑。

5. Activity 中业务逻辑较重

网络请求、数据处理、UI 更新、跳转逻辑大量写在 Activity 中，后续维护和测试成本较高。

6. 缺少 Repository / ViewModel 分层

当前没有形成清晰的数据层、业务层和 UI 层结构，功能扩展时容易产生重复代码。

7. 流式接口 JSON 处理较脆弱

`StreamService.kt` 中使用字符串拼接 JSON，并手写解析 `content` 字段，遇到复杂转义或响应结构变化时容易出错。

8. 本地存储安全性不足

JWT 和用户信息保存在普通 SharedPreferences 中，安全性不足。

9. 部分数据仍为模拟数据

个人中心统计、物流详情等模块仍使用本地模拟数据，尚未完全对接真实后端。

10. 订单列表展示能力有限

订单页面最多展示固定 3 条，筛选标签没有真正按状态过滤，列表扩展性不足。

11. WebView 安全策略不足

服务详情页加载外部网页并开启 JavaScript，但缺少域名白名单、URL 校验和更严格的安全策略。

12. Release 配置不完整

Release 构建未开启混淆和资源压缩，日志拦截器也未区分调试与生产环境。

13. Android 新版本兼容问题

头像选择仍使用 `READ_EXTERNAL_STORAGE` 和 `startActivityForResult`，需要适配新版本 Android 的权限和 Activity Result API。

14. 版本号不一致

Gradle 中 `versionName = "1.0"`，但字符串资源中显示版本号 `3.1.0`。

## 7. V3 规划

### 架构升级

- 引入 MVVM 架构。
- Activity 只负责页面展示和用户交互。
- 使用 ViewModel 管理页面状态。
- 使用 Repository 统一管理网络、本地缓存和业务数据。
- 按业务模块拆分认证、订单、客服、个人中心等代码。

### 网络与环境配置升级

- 将 Base URL 移入 `BuildConfig`、Gradle 配置或环境配置文件。
- 区分开发、测试、生产环境。
- 统一 Retrofit 和 OkHttp 初始化。
- 使用 OkHttp Interceptor 自动添加 Token。
- 增加统一错误处理、请求日志控制、超时配置和重试策略。

### 安全升级

- 后端接口切换 HTTPS。
- 关闭生产环境明文流量。
- JWT 使用 EncryptedSharedPreferences 或 Jetpack Security 保存。
- Release 环境关闭 BODY 级别请求日志。
- WebView 增加可信域名白名单。
- 对外部 URL 跳转做校验。

### 数据层升级

- 引入 Room 或 DataStore。
- 缓存订单列表、用户资料、聊天记录等关键数据。
- 支持离线展示和失败重试。
- 建立统一的数据模型和 DTO 转换逻辑。

### 智能客服升级

- 使用稳定的 SSE / WebSocket / 流式请求封装。
- 使用 Gson 或 kotlinx.serialization 解析流式响应。
- 支持停止生成、重新生成、复制回答、清空上下文。
- 聊天记录支持本地保存和后端同步。
- 增加异常恢复和网络断开提示。

### 订单模块升级

- 订单列表改为 RecyclerView 动态渲染。
- 支持分页、下拉刷新、状态筛选、搜索。
- 创建订单表单增加更多外贸字段。
- 支持订单详情页、物流轨迹页、状态流转。
- 接入真实物流查询接口。

### 个人中心升级

- 用户资料从后端接口获取。
- 头像上传到后端或对象存储。
- 统计数据改为真实接口。
- 增加账户安全、语言、货币、通知等真实设置能力。

### UI 与交互升级

- 统一设计规范和组件样式。
- 优化底部导航实现，可考虑 Navigation Component。
- 增加加载中、空状态、错误状态。
- 优化表单校验和错误提示。
- 适配深色模式和多屏幕尺寸。

### 工程质量升级

- 增加单元测试和 UI 测试。
- 增加接口 Mock 测试。
- 配置 CI 构建检查。
- 开启 Release 混淆、资源压缩和签名配置。
- 建立代码规范和提交规范。
- 梳理版本号规则，统一应用内显示版本与 Gradle 版本。

### 后端协同规划

- 明确 Strapi 数据模型。
- 完善用户、订单、物流、统计、聊天记录等接口。
- 增加接口鉴权和权限控制。
- 输出接口文档，方便前后端协作。
- 为 V3 提供稳定测试环境和生产环境。
