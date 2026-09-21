# ===== R8 混淆/压缩规则 =====
# 保留注解与泛型签名
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# OkHttp / Okio 自带 consumer 规则，这里仅抑制其可选依赖告警
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
-dontwarn kotlinx.coroutines.**
