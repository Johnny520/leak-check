package com.johnny.leakcheck.data

import android.content.Context

/** 轻量配置存储：服务器地址 + 查询模式 */
object Prefs {
    private const val FILE = "leak_check_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_MODE = "query_mode"

    /** 预置默认服务器地址（可在设置中修改） */
    const val DEFAULT_BASE_URL = "http://172.16.1.4/leak-check"

    const val MODE_ONLINE = "online"
    const val MODE_LOCAL = "local"

    private fun sp(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getBaseUrl(context: Context): String =
        sp(context).getString(KEY_BASE_URL, DEFAULT_BASE_URL).orEmpty()

    fun setBaseUrl(context: Context, url: String) {
        val v = url.trim().ifEmpty { DEFAULT_BASE_URL }
        sp(context).edit().putString(KEY_BASE_URL, v).apply()
    }

    fun getMode(context: Context): String =
        if (sp(context).getString(KEY_MODE, MODE_ONLINE) == MODE_LOCAL) MODE_LOCAL else MODE_ONLINE

    fun setMode(context: Context, mode: String) {
        sp(context).edit().putString(KEY_MODE, mode).apply()
    }
}
