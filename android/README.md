# leak-check · Android 客户端

> 个人信息“泄漏”查询的安卓客户端（原生 Kotlin）。支持**在线查询**与**本地离线查询**双模式，结果统一脱敏。

## 功能

- **在线查询**：调用 leak-check 服务端接口（`POST {base}/dig/masking`），需在设置中配置服务器地址；
- **本地查询**：直接读取内置/导入的 SQLite 数据库，复刻服务端 BFS 溯源逻辑（深度 2、上限 64 条），完全离线；
- 输入 手机号 / 身份证号 / 邮箱 / QQ 号自动识别类型；
- 展示脱敏聚合结果：身份证、姓名、收件人、昵称、手机号、邮箱、QQ、微博、地址、车辆、联系人、公司、数据来源；
- 设置页可配置服务器地址、测试连接、查看本地库记录数、**导入自有 `.db` 文件**；
- 界面为 加载中 / 成功 / 空数据 / 失败 四态，深色浅色自适应。

## 环境

- JDK 17+
- Android SDK：`compileSdk 34`、`build-tools 34.0.0`
- `minSdk 26`、`targetSdk 34`

## 构建

```bash
cd android
./gradlew :app:assembleDebug      # Debug
./gradlew :app:assembleRelease    # Release（R8 混淆 + 资源压缩）
```

Release 产物：`app/build/outputs/apk/release/app-release.apk`

## 签名

Release 签名通过环境变量注入（CI 由仓库 Secrets 提供），未注入时产物为未签名包：

| 环境变量 | 说明 |
| --- | --- |
| `RELEASE_STORE_FILE` | keystore 文件路径 |
| `RELEASE_STORE_PASSWORD` | keystore 密码 |
| `RELEASE_KEY_ALIAS` | 密钥别名 |
| `RELEASE_KEY_PASSWORD` | 密钥密码 |

## 数据与接口

- **在线模式**：默认服务器地址 `http://172.16.1.4/leak-check`（可在设置中修改）。
  - `POST {base}/dig/masking`，请求体 `{"q": "<手机号|身份证|邮箱|QQ>"}`，返回脱敏聚合 JSON；
  - `GET {base}/`，返回数据库记录数。
- **本地模式**：`assets/example.db` 为内置示例库，首次启动释放到应用私有目录；可在设置中导入自有 `.db`（表结构需含 `person`（含 `rowid`）与 `source` 表）。

## 作者

Johnny520 · <https://github.com/Johnny520>
