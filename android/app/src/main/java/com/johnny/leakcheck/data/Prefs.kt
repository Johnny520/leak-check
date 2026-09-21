package com.johnny.leakcheck.data

import android.content.Context

/** 配置存储：服务器地址（默认 + 自定义列表）、当前地址、查询模式 */
object Prefs {
    private const val FILE = "leak_check_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_SERVERS = "servers"
    private const val KEY_MODE = "query_mode"

    /** 预置默认服务器地址（内置，不可删除） */
    const val DEFAULT_BASE_URL = "http://172.16.1.4/leak-check"

    const val MODE_ONLINE = "online"
    const val MODE_LOCAL = "local"

    private fun sp(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    /** 当前使用的服务器地址 */
    fun getBaseUrl(context: Context): String {
        val v = sp(context).getString(KEY_BASE_URL, DEFAULT_BASE_URL).orEmpty().trim()
        return v.ifEmpty { DEFAULT_BASE_URL }
    }

    /** 设为当前地址，并加入自定义列表（默认地址不再重复加入） */
    fun setBaseUrl(context: Context, url: String) {
        val v = url.trim().ifEmpty { DEFAULT_BASE_URL }
        sp(context).edit().putString(KEY_BASE_URL, v).apply()
        addServer(context, v)
    }

    /** 自定义服务器地址列表（不含默认地址） */
    fun getServers(context: Context): List<String> {
        val raw = sp(context).getString(KEY_SERVERS, "").orEmpty()
        return raw.split('\n')
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != DEFAULT_BASE_URL }
            .distinct()
    }

    /** 全部可选地址：默认地址 + 自定义地址 */
    fun getAllServers(context: Context): List<String> =
        (listOf(DEFAULT_BASE_URL) + getServers(context)).distinct()

    fun addServer(context: Context, url: String) {
        val v = url.trim()
        if (v.isEmpty() || v == DEFAULT_BASE_URL) return
        val list = getServers(context).toMutableList()
        if (list.contains(v)) return
        list.add(v)
        sp(context).edit().putString(KEY_SERVERS, list.joinToString("\n")).apply()
    }

    fun removeServer(context: Context, url: String) {
        val v = url.trim()
        if (v == DEFAULT_BASE_URL) return
        val list = getServers(context).toMutableList()
        if (!list.remove(v)) return
        sp(context).edit().putString(KEY_SERVERS, list.joinToString("\n")).apply()
        if (getBaseUrl(context) == v) {
            sp(context).edit().putString(KEY_BASE_URL, DEFAULT_BASE_URL).apply()
        }
    }

    /** 当前地址恢复为默认地址 */
    fun resetToDefault(context: Context) {
        sp(context).edit().putString(KEY_BASE_URL, DEFAULT_BASE_URL).apply()
    }

    fun getMode(context: Context): String =
        if (sp(context).getString(KEY_MODE, MODE_ONLINE) == MODE_LOCAL) MODE_LOCAL else MODE_ONLINE

    fun setMode(context: Context, mode: String) {
        sp(context).edit().putString(KEY_MODE, mode).apply()
    }
}
