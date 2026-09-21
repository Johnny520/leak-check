package com.johnny.leakcheck.data

import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * leak-check 在线接口客户端。
 * - POST {base}/dig/masking  查询（脱敏聚合）
 * - GET  {base}/             读取数据库记录数
 */
class LeakCheckApi {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    suspend fun query(baseUrlRaw: String, q: String): ApiResult<AggregatedResult> =
        withContext(Dispatchers.IO) {
            val base = normalizeBase(baseUrlRaw)
                ?: return@withContext ApiResult.Failure("服务器地址无效，请在设置中配置")

            val payload = JSONObject().put("q", q).toString().toRequestBody(JSON_MEDIA)
            val request = Request.Builder()
                .url("$base/dig/masking")
                .post(payload)
                .build()

            try {
                client.newCall(request).execute().use { resp ->
                    val text = resp.body?.string().orEmpty()
                    if (!resp.isSuccessful) {
                        ApiResult.Failure(extractError(resp.code, text))
                    } else {
                        parseAggregated(text)
                    }
                }
            } catch (e: IOException) {
                ApiResult.Failure("网络错误：" + (e.message ?: "连接失败"))
            } catch (e: Exception) {
                ApiResult.Failure("请求失败：" + (e.message ?: "未知错误"))
            }
        }

    suspend fun count(baseUrlRaw: String): ApiResult<String> = withContext(Dispatchers.IO) {
        val base = normalizeBase(baseUrlRaw)
            ?: return@withContext ApiResult.Failure("服务器地址无效")

        val request = Request.Builder().url("$base/").get().build()
        try {
            client.newCall(request).execute().use { resp ->
                val text = resp.body?.string().orEmpty()
                if (resp.isSuccessful) {
                    ApiResult.Success(text.trim())
                } else {
                    ApiResult.Failure(extractError(resp.code, text))
                }
            }
        } catch (e: IOException) {
            ApiResult.Failure("网络错误：" + (e.message ?: "连接失败"))
        } catch (e: Exception) {
            ApiResult.Failure("请求失败：" + (e.message ?: "未知错误"))
        }
    }

    private fun normalizeBase(raw: String): String? {
        var s = raw.trim()
        if (s.isEmpty()) return null
        if (!s.startsWith("http://") && !s.startsWith("https://")) {
            s = "http://$s"
        }
        while (s.endsWith("/")) s = s.dropLast(1)
        return s.ifEmpty { null }
    }

    private fun extractError(code: Int, text: String): String {
        val detail = runCatching {
            val obj = JSONObject(text)
            when (val d = obj.opt("detail")) {
                is String -> d
                is JSONArray -> {
                    val first = if (d.length() > 0) d.optJSONObject(0) else null
                    first?.optString("msg")?.takeIf { it.isNotBlank() } ?: d.toString()
                }
                else -> null
            }
        }.getOrNull()
        return detail?.takeIf { it.isNotBlank() } ?: "请求失败（HTTP $code）"
    }

    private fun parseAggregated(text: String): ApiResult<AggregatedResult> = runCatching {
        val json = JSONObject(text)
        AggregatedResult(
            id = json.stringList("id"),
            name = json.stringList("name"),
            receiver = json.stringList("receiver"),
            nickname = json.stringList("nickname"),
            phone = json.stringList("phone"),
            address = json.stringList("address"),
            car = json.stringList("car"),
            email = json.stringList("email"),
            qq = json.stringList("qq"),
            weibo = json.stringList("weibo"),
            contact = json.stringList("contact"),
            company = json.stringList("company"),
            source = json.stringList("source")
        )
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Failure("响应解析失败：" + (it.message ?: "数据格式异常")) }
    )

    private fun JSONObject.stringList(key: String): List<String> {
        val arr = optJSONArray(key) ?: return emptyList()
        val out = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            val v = arr.opt(i)
            if (v != null && v != JSONObject.NULL) {
                val s = v.toString()
                if (s.isNotBlank()) out.add(s)
            }
        }
        return out
    }

    private companion object {
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
