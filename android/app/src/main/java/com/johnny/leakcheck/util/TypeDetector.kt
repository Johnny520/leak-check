package com.johnny.leakcheck.util

/**
 * 输入类型识别，规则与服务端 models/request.py 保持一致：
 * 手机号 / 邮箱 / 身份证（大陆 + 台湾） / QQ。
 */
object TypeDetector {

    const val TYPE_PHONE = "phone"
    const val TYPE_EMAIL = "email"
    const val TYPE_ID = "id"
    const val TYPE_QQ = "qq"

    private val CN_PHONE = Regex("^1\\d{10}$")
    private val INTL_PHONE = Regex("^\\+\\d{6,15}$")
    private val EMAIL = Regex("^[\\w.-]+@[\\w.-]+\\.\\w+$")
    private val MAINLAND_ID = Regex("^\\d{17}[\\dXx]$")
    private val TAIWAN_ID = Regex("^[A-Z][12]\\d{8}$")
    private val QQ = Regex("^\\d{5,11}$")
    private val SEPARATORS = Regex("[ \\-()]")

    /** 清洗：去空格 / 连字符 / 括号（与服务端 request.py 一致） */
    fun clean(raw: String): String = SEPARATORS.replace(raw.trim(), "")

    /** 返回 phone / email / id / qq；无法识别返回 null */
    fun detect(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val cleaned = clean(trimmed)
        return when {
            CN_PHONE.matches(cleaned) || INTL_PHONE.matches(cleaned) -> TYPE_PHONE
            EMAIL.matches(trimmed) -> TYPE_EMAIL
            MAINLAND_ID.matches(cleaned) || TAIWAN_ID.matches(cleaned) -> TYPE_ID
            QQ.matches(cleaned) -> TYPE_QQ
            else -> null
        }
    }

    fun label(type: String?): String = when (type) {
        TYPE_PHONE -> "手机号"
        TYPE_EMAIL -> "邮箱"
        TYPE_ID -> "身份证"
        TYPE_QQ -> "QQ"
        else -> "未知"
    }
}
