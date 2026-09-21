# leak-check 💠
> 个人信息泄露查询工具 | 原生 Kotlin Android 客户端 | 在线 / 本地离线双查询，结果自动脱敏


---

## 项目简介
本项目用于检索个人信息是否出现在公开泄露数据集内，**所有查询结果均做脱敏展示**。

1. **不记录**任何用户查询记录
2. **不提供**超出本次检索范围的额外信息
3. **接受**公众舆论监督

> 你有权了解自身数据是否在互联网被传播泄露。

---

## ✨ 功能特性
- 🌐 **在线查询**：调用后端接口 `POST {base}/dig/masking`，返回脱敏聚合查询结果
- 📴 **本地离线查询**：直接读取 SQLite 数据库，内置 BFS 溯源逻辑（深度2，最多返回64条），无网络可用
- 🔍 输入自动识别：手机号 / 身份证号 / 邮箱 / QQ号
- 🖥️ 多服务地址管理：内置默认接口地址，支持新增、切换、删除自定义服务地址，一键恢复默认配置
- 🗄️ 本地数据库管理：查看数据库记录总数，支持导入自定义 `.db` 文件，自动校验SQLite文件合法性
- 📋 结果展示字段：姓名、收件人、昵称、手机号、邮箱、QQ、微博、地址、车辆、联系人、公司、数据来源
- 🎨 多状态UI：加载中 / 查询成功 / 无数据 / 请求失败；深色/浅色主题自适应

---

## 🏗️ 项目结构
 
 
leak-check/
├── android/            # 📱 Android 客户端，原生 Kotlin
│   ├── src/main/       # APP主源码
│   └── build.gradle    # 客户端构建配置
├── db/                 # 🗄️ 数据库目录
│   └── leak-check.db   # SQLite数据库文件
├── install.sh          # 🚀 一键开发环境初始化脚本
└── README.md           # 项目说明文档
 
plaintext  

---

## 📊 数据库结构
项目采用 SQLite，**本仓库不提供完整原始数据拷贝**，你可自行准备数据库文件。
```bash
sqlite3 ./db/leak-check.db
 
 
sql  
-- 数据源表
CREATE TABLE source (
    id INTEGER PRIMARY KEY,
    source TEXT DEFAULT NULL
);

-- 人员信息表
CREATE TABLE person (
    id TEXT DEFAULT NULL,
    name TEXT DEFAULT NULL,
    receiver TEXT DEFAULT NULL,
    nickname TEXT DEFAULT NULL,
    phone TEXT DEFAULT NULL,
    address TEXT DEFAULT NULL,
    car TEXT DEFAULT NULL,
    email TEXT DEFAULT NULL,
    qq INTEGER DEFAULT NULL,
    weibo INTEGER DEFAULT NULL,
    contact TEXT DEFAULT NULL,
    company TEXT DEFAULT NULL,
    source_id INTEGER DEFAULT 0,
    FOREIGN KEY (source_id) REFERENCES source(id)
);
 
 
 
 
📱 Android客户端详情
 
原生 Kotlin 开发安卓App，在线联网查询 + SQLite本地离线查询双模式，查询结果自动脱敏。
 
🔧 环境要求
 
- JDK 17+
- Android SDK:  compileSdk 34 ， build-tools 34.0.0 
-  minSdk 26 ， targetSdk 34 
 
📦 编译打包
 
bash  
cd android

# Debug 调试包
./gradlew :app:assembleDebug

# Release 正式包（R8混淆 + 资源压缩）
./gradlew :app:assembleRelease
 
 
Release 产物路径： app/build/outputs/apk/release/app-release.apk 
 
🔐 Release 签名配置
 
Release包签名信息通过CI环境变量注入，在GitHub Actions Secrets中配置。未配置签名变量时，打包输出未签名APK。
 
环境变量 说明 
 RELEASE_STORE_FILE  keystore 密钥文件路径 
 RELEASE_STORE_PASSWORD  密钥库密码 
 RELEASE_KEY_ALIAS  密钥别名 
 RELEASE_KEY_PASSWORD  密钥密码 
 
 
 
📡 接口说明
 
在线查询模式
 
- 默认接口地址： https://api.garinasset.com/leak-check 
- 网页前端官网： https://leak-check.garinasset.com/ （前端站点，与API接口分离）
- 查询接口： POST {base}/dig/masking 
- 请求体： {"q": "<手机号|身份证|邮箱|QQ>"} 
- 返回：脱敏JSON聚合结果
- 统计接口： GET {base}/ 
- 返回：数据库总记录数量
 
本地离线查询模式
 
1. 在APP主界面顶部切换至「本地查询」
2. 首次启动自动释放内置示例数据库  assets/example.db ，设置页面可查看数据库记录数量
3. 导入自有数据库：设置 → 本地数据库 → 导入  .db  文件
 
数据库必须包含  person （需要包含  rowid  字段）与  source  两张表；非法SQLite文件会直接拦截拒绝导入
 
 
 
⚙️ 服务器地址管理
 
- 默认地址：内置固定API地址，不可删除，常驻列表
- 自定义地址：设置页面填写并保存，可一键切换；点击地址后方 × 可删除自定义项
- 恢复默认：一键切回官方默认地址，已添加的自定义地址列表保留
- 快捷切换：主界面菜单提供「切换服务器」快速入口，附带跳转设置页面入口
 
 
 
🚀 快速开发部署
 
一键脚本拉取并初始化开发环境
 
bash  
curl -LsSf https://raw.githubusercontent.com/garinasset/leak-check/refs/heads/main/install.sh | bash
 
 
🗑️ 完全删除项目
 
bash  
rm -rf leak-check/
 
 
 
 
📬 更新与反馈
 
入口 链接 
🐙 GitHub仓库 https://github.com/garinasset/leak-check 
🐛 Bug反馈 & 建议 Issues 
 
📜 License
 
MIT License
 
 
Built with ❤️ by Johnny520
``` 