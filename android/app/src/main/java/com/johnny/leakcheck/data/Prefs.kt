package com.johnny.leakcheck.data

import android.content.Context

/** 轻量配置存储：仅保存服务器地址 */
object Prefs {
    private const val FILE = "leak_check_prefs"
    private const val KEY_BASE_URL = "base_url"

    private fun sp(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getBaseUrl(context: Context): String =
        sp(context).getString(KEY_BASE_URL, "").orEmpty()

    fun setBaseUrl(context: Context, url: String) {
        sp(context).edit().putString(KEY_BASE_URL, url.trim()).apply()
    }
}
