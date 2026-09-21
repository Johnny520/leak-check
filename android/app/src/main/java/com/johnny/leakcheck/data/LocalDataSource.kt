package com.johnny.leakcheck.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.johnny.leakcheck.util.TypeDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 本地离线查询：复刻服务端 db/crud.py 的 BFS 溯源逻辑，配合 Masking 脱敏。
 * 参数与服务端一致：max_depth = 2，max_records = 64。
 */
object LocalDataSource {

    private const val MAX_DEPTH = 2
    private const val MAX_RECORDS = 64

    private const val COLS =
        "p.rowid AS rid, p.id, p.name, p.receiver, p.nickname, p.phone, p.address, " +
            "p.car, p.email, p.qq, p.weibo, p.contact, p.company, s.source AS src"

    private data class PersonRow(
        val rowid: Long,
        val id: String?,
        val name: String?,
        val receiver: String?,
        val nickname: String?,
        val phone: String?,
        val address: String?,
        val car: String?,
        val email: String?,
        val qq: Long?,
        val weibo: Long?,
        val contact: String?,
        val company: String?,
        val source: String?
    )

    suspend fun count(context: Context): ApiResult<String> = withContext(Dispatchers.IO) {
        try {
            if (!LocalDb.ensure(context)) {
                return@withContext ApiResult.Failure("本地数据库不可用")
            }
            LocalDb.open(context).use { db ->
                db.rawQuery("SELECT MAX(rowid) FROM person", null).use { c ->
                    val v = if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else 0L
                    ApiResult.Success(v.toString())
                }
            }
        } catch (e: Exception) {
            ApiResult.Failure("本地查询失败：" + (e.message ?: "未知错误"))
        }
    }

    suspend fun query(context: Context, type: String, q: String): ApiResult<AggregatedResult> =
        withContext(Dispatchers.IO) {
            try {
                if (!LocalDb.ensure(context)) {
                    return@withContext ApiResult.Failure("本地数据库不可用")
                }
                LocalDb.open(context).use { db ->
                    val persons = when (type) {
                        TypeDetector.TYPE_PHONE -> bfs(db, phoneArg = q)
                        TypeDetector.TYPE_EMAIL -> bfs(db, emailArg = q)
                        TypeDetector.TYPE_ID -> bfs(db, idArg = q.uppercase())
                        TypeDetector.TYPE_QQ -> {
                            val n = q.toLongOrNull()
                                ?: return@withContext ApiResult.Failure("QQ 号格式不正确")
                            bfs(db, qqArg = n)
                        }
                        else -> emptyList()
                    }
                    ApiResult.Success(aggregate(persons))
                }
            } catch (e: Exception) {
                ApiResult.Failure("本地查询失败：" + (e.message ?: "未知错误"))
            }
        }

    private fun bfs(
        db: SQLiteDatabase,
        idArg: String? = null,
        phoneArg: String? = null,
        emailArg: String? = null,
        qqArg: Long? = null
    ): List<PersonRow> {
        var idSet = LinkedHashSet<String>()
        if (!idArg.isNullOrEmpty()) {
            val s = idArg.trim()
            idSet.add(s)
            idSet.add(s.uppercase())
            idSet.add(s.lowercase())
        }
        var phoneSet = LinkedHashSet<String>().also { if (phoneArg != null) it.add(phoneArg) }
        var emailSet = LinkedHashSet<String>().also { if (emailArg != null) it.add(emailArg) }
        var qqSet = LinkedHashSet<Long>().also { if (qqArg != null) it.add(qqArg) }

        val all = LinkedHashMap<Long, PersonRow>()
        var depth = 0
        while (depth < MAX_DEPTH) {
            depth++
            val newIds = LinkedHashSet<String>()
            val newPhones = LinkedHashSet<String>()
            val newEmails = LinkedHashSet<String>()
            val newQqs = LinkedHashSet<Long>()

            val results = ArrayList<PersonRow>()
            if (idSet.isNotEmpty()) results.addAll(selectIn(db, "p.id", idSet.toList()))
            if (phoneSet.isNotEmpty()) results.addAll(selectIn(db, "p.phone", phoneSet.toList()))
            if (emailSet.isNotEmpty()) results.addAll(selectIn(db, "p.email", emailSet.toList()))
            if (qqSet.isNotEmpty()) results.addAll(selectIn(db, "p.qq", qqSet.map { it.toString() }))

            var hasNew = false
            for (p in results) {
                if (all.containsKey(p.rowid)) continue
                all[p.rowid] = p
                hasNew = true
                val pid = p.id
                if (!pid.isNullOrBlank() && pid !in idSet) newIds.add(pid)
                val ph = p.phone
                if (!ph.isNullOrBlank() && ph !in phoneSet) newPhones.add(ph)
                val em = p.email
                if (!em.isNullOrBlank() && em !in emailSet) newEmails.add(em)
                val q = p.qq
                if (q != null && q != 0L && q !in qqSet) newQqs.add(q)
            }

            if (all.size >= MAX_RECORDS) break
            if (!hasNew) break

            idSet = newIds
            phoneSet = newPhones
            emailSet = newEmails
            qqSet = newQqs
        }
        return all.values.toList()
    }

    private fun selectIn(db: SQLiteDatabase, col: String, values: List<String>): List<PersonRow> {
        if (values.isEmpty()) return emptyList()
        val placeholders = values.joinToString(",") { "?" }
        val sql = "SELECT $COLS FROM person p LEFT JOIN source s ON p.source_id = s.id " +
            "WHERE $col IN ($placeholders)"
        return db.rawQuery(sql, values.toTypedArray()).use { c -> readAll(c) }
    }

    private fun readAll(c: Cursor): List<PersonRow> {
        val out = ArrayList<PersonRow>(c.count)
        while (c.moveToNext()) {
            out.add(
                PersonRow(
                    rowid = c.getLong(0),
                    id = c.strOrNull(1),
                    name = c.strOrNull(2),
                    receiver = c.strOrNull(3),
                    nickname = c.strOrNull(4),
                    phone = c.strOrNull(5),
                    address = c.strOrNull(6),
                    car = c.strOrNull(7),
                    email = c.strOrNull(8),
                    qq = c.longOrNull(9),
                    weibo = c.longOrNull(10),
                    contact = c.strOrNull(11),
                    company = c.strOrNull(12),
                    source = c.strOrNull(13)
                )
            )
        }
        return out
    }

    private fun Cursor.strOrNull(i: Int): String? = if (isNull(i)) null else getString(i)

    private fun Cursor.longOrNull(i: Int): Long? = if (isNull(i)) null else getLong(i)

    private fun aggregate(persons: List<PersonRow>): AggregatedResult = AggregatedResult(
        id = maskList("id", persons.map { it.id }),
        name = maskList("name", persons.map { it.name }),
        receiver = maskList("receiver", persons.map { it.receiver }),
        nickname = maskList("nickname", persons.map { it.nickname }),
        phone = maskList("phone", persons.map { it.phone }),
        address = maskList("address", persons.map { it.address }),
        car = maskList("car", persons.map { it.car }),
        email = maskList("email", persons.map { it.email }),
        qq = maskList("qq", persons.map { it.qq }),
        weibo = maskList("weibo", persons.map { it.weibo }),
        contact = maskList("contact", persons.map { it.contact }),
        company = maskList("company", persons.map { it.company }),
        source = persons.mapNotNull { it.source }.filter { it.isNotBlank() }.toSet().toList()
    )

    private fun maskList(field: String, values: List<Any?>): List<String> {
        val out = LinkedHashSet<String>()
        for (v in values) {
            if (v == null) continue
            val s = v.toString()
            if (s.isBlank()) continue
            out.add(Masking.maskValue(field, s))
        }
        return out.toList()
    }
}
