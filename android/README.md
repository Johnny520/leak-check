# leak-check · Android 客户端

> 个人信息“泄漏”在线查询的安卓客户端（原生 Kotlin，在线调用 leak-check 服务端接口）。

## 功能

- 输入 手机号 / 身份证号 / 邮箱 / QQ 号，自动识别类型后在线查询；
- 展示服务端返回的**脱敏聚合**结果：身份证、姓名、收件人、昵称、手机号、邮箱、QQ、微博、地址、车辆、联系人、公司、数据来源；
- 可配置服务器地址，支持“测试连接”读取数据库记录数；
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

## 服务端

本客户端为**在线查询**，请先在应用「设置」中填写手机上可访问的 leak-check 服务地址，
例如 `http://172.16.1.4/leak-check`（服务端即本仓库根目录的 Python / FastAPI 项目）。

接口契约：
- `POST {base}/dig/masking`，请求体 `{"q": "<手机号|身份证|邮箱|QQ>"}`，返回脱敏聚合 JSON；
- `GET {base}/`，返回数据库记录数。

## 作者

Johnny520 · <https://github.com/Johnny520>
