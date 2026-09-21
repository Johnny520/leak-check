 ––leak-check
 
个人信息泄露查询工具 · 原生 Kotlin 安卓客户端 · 在线 / 本地双模式 · 结果自动脱敏
 
GitHub
License
 
 
 
项目简介
 
查询个人信息是否出现在公开泄露数据中，所有结果统一脱敏展示。
 
- 不记录任何查询记录
- 不提供超出本次检索的额外信息
- 接受公众舆论监督
 
你有权了解自己的数据处于何种状态。
 
 
 
功能特性
 
模式 说明 
🌐 在线查询 调用服务端接口  POST {base}/dig/masking ，实时返回脱敏聚合结果 
📴 本地查询 直接读取 SQLite 数据库，内置 BFS 溯源逻辑（深度 2，上限 64 条），完全离线可用 
 
其他能力：
 
- 自动识别输入类型：手机号 / 身份证号 / 邮箱 / QQ 号
- 多服务器地址管理：内置默认地址，支持添加、切换、删除自定义地址，一键恢复默认
- 本地数据库管理：查看记录数、导入自有  .db  文件（自动校验 SQLite 文件头）
- 结果字段：姓名、收件人、昵称、手机号、邮箱、QQ、微博、地址、车辆、联系人、公司、数据来源
- 四态 UI：加载中 / 成功 / 空数据 / 失败
- 深色 / 浅色主题自适应
 
 
 
快速开始
 
环境要求
 
- JDK 17+
- Android SDK： compileSdk 34 、 build-tools 34.0.0 
-  minSdk 26 、 targetSdk 34 
 
一键初始化开发环境
 
bash  
curl -LsSf https://raw.githubusercontent.com/garinasset/leak-check/refs/heads/main/install.sh | bash
 
 
编译构建
 
bash  
cd android

# Debug 调试包
./gradlew :app:assembleDebug

# Release 正式包（R8 混淆 + 资源压缩）
./gradlew :app:assembleRelease
 
 
Release 产物路径： app/build/outputs/apk/release/app-release.apk 
 
 
 
签名配置
 
Release 签名通过环境变量注入（CI 由 GitHub Secrets 提供），未注入时产物为未签名包：
 
环境变量 说明 
 RELEASE_STORE_FILE  keystore 文件路径 
 RELEASE_STORE_PASSWORD  keystore 密码 
 RELEASE_KEY_ALIAS  密钥别名 
 RELEASE_KEY_PASSWORD  密钥密码 
 
 
 
接口说明
 
在线模式
 
- 默认地址： https://api.garinasset.com/leak-check 
- 官网前端： https://leak-check.garinasset.com/ （网页端，与接口分离）
- 查询接口： POST {base}/dig/masking 
- 请求体： {"q": "<手机号|身份证|邮箱|QQ>"} 
- 返回：脱敏聚合 JSON
- 统计接口： GET {base}/ 
- 返回：数据库总记录数
 
本地模式
 
1. 主界面顶部切换到「本地查询」
2. 首次启动自动释放内置示例库（ assets/example.db ），设置页可查看记录数
3. 导入自有数据库：设置 → 本地数据库 →「导入 .db 文件」
- 需包含  person （含  rowid ）与  source  两张表
- 非法 SQLite 文件会被自动拦截
 
 
 
数据库结构
 
项目采用 SQLite，不提供数据拷贝，可自行准备数据库文件：
 
bash  
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
 
 
 
 
服务器地址管理
 
- 默认地址：内置  https://api.garinasset.com/leak-check ，不可删除，常驻列表
- 自定义地址：设置页输入后「保存并使用」即加入列表并切换；点击 × 删除；点击即切换
- 恢复默认：一键切回默认地址（自定义列表保留）
- 快捷切换：主界面菜单「切换服务器」可直接切换
 
 
 
完全删除
 
bash  
rm -rf leak-check/
 
 
 
 
更新与反馈
 
- GitHub 仓库：https://github.com/garinasset/leak-check
- Issues & Bug 报告：https://github.com/garinasset/leak-check/issues
 
 
 
许可证
 
MIT License
 
 
 
作者
 
Johnny520 · https://github.com/Johnny520