# leak-check · Android 客户端

> 个人信息“泄漏”查询的安卓客户端（原生 Kotlin）。支持**在线查询**与**本地离线查询**双模式，结果统一脱敏。

## 功能

- **在线查询**：调用 leak-check 服务端接口（`POST {base}/dig/masking`）；
- **本地查询**：直接读取内置/导入的 SQLite 数据库，复刻服务端 BFS 溯源逻辑（深度 2、上限 64 条），完全离线；
- **服务器地址**：内置默认地址 `https://api.garinasset.com/leak-check`，并支持**自定义多个服务器地址**——
  设置页可添加 / 选用 / 删除（自定义地址带 ×），可一键「恢复默认地址」；主界面菜单「切换服务器」可直接切换；
- 输入 手机号 / 身份证号 / 邮箱 / QQ 号自动识别类型；
- 展示脱敏聚合结果：身份证、姓名、收件人、昵称、手机号、邮箱、QQ、微博、地址、车辆、联系人、公司、数据来源；
- 设置页可测试连接、查看本地库记录数、**导入自有 `.db` 文件**；
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

- **在线模式**：
  - 默认地址 `https://api.garinasset.com/leak-check`（官方服务；可在设置中修改，或添加多个自定义地址）；
  - `POST {base}/dig/masking`，请求体 `{"q": "<手机号|身份证|邮箱|QQ>"}`，返回脱敏聚合 JSON；
  - `GET {base}/`，返回数据库记录数。
  - 说明：`https://leak-check.garinasset.com/` 是**官网前端**（网页），接口在 `api.garinasset.com` 下。
- **本地模式**：`assets/example.db` 为内置示例库，首次启动释放到应用私有目录；可在设置中导入自有 `.db`（表结构需含 `person`（含 `rowid`）与 `source` 表）。

## 本地（离线）模式说明

1. 主界面顶部切换到「本地查询」；
2. 首次使用会自动释放内置示例库，可在设置页看到当前记录数；
3. 如需查询自有数据：设置 → 本地数据库 →「导入 .db 文件」，选择设备上的 SQLite 文件即可
   （会校验 SQLite 文件头，非法文件会被拒绝）；
4. 导入后记录数会刷新；回到主界面输入 手机号 / 身份证 / 邮箱 / QQ 即可离线查询，结果与在线模式同样脱敏。

## 服务器地址管理

- **默认地址**：内置 `https://api.garinasset.com/leak-check`，不可删除，始终在列表中；
- **自定义地址**：在设置页输入框中填入地址后点「保存并使用」，即加入列表并切换为当前地址；
- 在列表中**点击**某个地址即切换为当前地址；点击自定义地址上的 **×** 即删除；
- 点「恢复默认地址」把当前地址切回默认地址（自定义列表保留）；
- 主界面菜单「切换服务器」可快速切换（含「更多设置」入口）。

## 作者

Johnny520 · <https://github.com/Johnny520>
