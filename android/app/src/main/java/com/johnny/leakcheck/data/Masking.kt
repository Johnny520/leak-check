package com.johnny.leakcheck.data

/**
 * 脱敏逻辑，与服务端 lib/masking.py 保持一致（1:1 复刻）。
 */
object Masking {

    private fun maskPhone(v: String): String =
        if (v.isEmpty() || v.length < 7) "***"
        else v.substring(0, 2) + "*******" + v.substring(v.length - 2)

    private fun maskEmail(v: String): String {
        if (v.isEmpty() || !v.contains("@")) return "***"
        val idx = v.indexOf('@')
        val name = v.substring(0, idx)
        val domain = v.substring(idx + 1)
        val nameMask = if (name.length <= 2) name.take(1) + "***" else name.substring(0, 2) + "***"
        return "$nameMask@$domain"
    }

    private fun maskId(v: String): String =
        if (v.isEmpty() || v.length < 10) "***"
        else v.substring(0, 2) + "********" + v.substring(v.length - 2)

    private fun maskNumber(v: String): String {
        if (v.length <= 3) return "*".repeat(v.length)
        return "*".repeat(v.length - 3) + v.substring(v.length - 3)
    }

    private fun maskName(v: String): String {
        if (v.isEmpty()) return "***"
        if (v.length == 1) return "*"
        return "*" + v.takeLast(1)
    }

    private fun maskCar(v: String): String {
        if (v.isEmpty()) return "***"
        val s = v.trim()
        if (s.isEmpty()) return "***"
        if (s.length <= 2) return s.take(1) + "*"
        return s.substring(0, 2) + "*".repeat(s.length - 2)
    }

    private fun maskAddress(v: String): String {
        if (v.isEmpty()) return "***"
        return if (v.length > 2) v.substring(0, 2) + "****" else v + "****"
    }

    /** 分发器：字段名 -> 脱敏值；空值返回空串（与服务端 mask_value 一致） */
    fun maskValue(field: String, raw: String?): String {
        if (raw.isNullOrEmpty()) return ""
        val v = raw.trim()
        if (v.isEmpty()) return ""
        return when (field) {
            "phone" -> maskPhone(v)
            "email" -> maskEmail(v)
            "id" -> maskId(v)
            "qq", "weibo" -> maskNumber(v)
            "name", "nickname", "receiver", "contact" -> maskName(v)
            "car" -> maskCar(v)
            "address", "company" -> maskAddress(v)
            else -> v
        }
    }
}
