package com.johnny.leakcheck.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import java.io.File

/**
 * 本地 SQLite 数据库管理：
 * - 首次使用时从 assets 释放内置示例库（example.db）；
 * - 支持导入用户自有的 .db 文件（覆盖本地库）。
 */
object LocalDb {

    const val DB_NAME = "leak-check.db"
    private const val ASSET_DB = "example.db"

    fun file(context: Context): File = context.getDatabasePath(DB_NAME)

    /** 确保本地库存在；不存在则释放内置示例库。返回是否可用。 */
    fun ensure(context: Context): Boolean {
        val f = file(context)
        if (f.exists()) return true
        f.parentFile?.mkdirs()
        return try {
            context.assets.open(ASSET_DB).use { input ->
                f.outputStream().use { out -> input.copyTo(out) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun open(context: Context): SQLiteDatabase =
        SQLiteDatabase.openDatabase(file(context).absolutePath, null, SQLiteDatabase.OPEN_READONLY)

    /** 从 SAF uri 导入数据库。成功返回 null，失败返回错误信息。 */
    fun importFrom(context: Context, uri: Uri): String? {
        return try {
            val dest = file(context)
            dest.parentFile?.mkdirs()
            val tmp = File(dest.absolutePath + ".tmp")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tmp.outputStream().use { out -> input.copyTo(out) }
            } ?: return "无法读取所选文件"

            if (!isSqlite(tmp)) {
                tmp.delete()
                return "所选文件不是有效的 SQLite 数据库"
            }
            if (dest.exists() && !dest.delete()) {
                tmp.delete()
                return "覆盖原有数据库失败"
            }
            if (!tmp.renameTo(dest)) {
                tmp.delete()
                return "写入本地数据库失败"
            }
            null
        } catch (e: Exception) {
            "导入失败：" + (e.message ?: "未知错误")
        }
    }

    private fun isSqlite(f: File): Boolean = try {
        f.inputStream().use { ins ->
            val header = ByteArray(16)
            if (ins.read(header) < 16) {
                false
            } else {
                String(header, Charsets.US_ASCII) == "SQLite format 3\u0000"
            }
        }
    } catch (e: Exception) {
        false
    }
}
