package com.johnny.leakcheck.data

/**
 * 与服务端 ModelResponsePersonAggregatedMasking 对应的脱敏聚合结果。
 * 所有字段均为「已脱敏」的字符串集合。
 */
data class AggregatedResult(
    val id: List<String>,
    val name: List<String>,
    val receiver: List<String>,
    val nickname: List<String>,
    val phone: List<String>,
    val address: List<String>,
    val car: List<String>,
    val email: List<String>,
    val qq: List<String>,
    val weibo: List<String>,
    val contact: List<String>,
    val company: List<String>,
    val source: List<String>
) {
    fun isEmpty(): Boolean =
        id.isEmpty() && name.isEmpty() && receiver.isEmpty() && nickname.isEmpty() &&
            phone.isEmpty() && address.isEmpty() && car.isEmpty() && email.isEmpty() &&
            qq.isEmpty() && weibo.isEmpty() && contact.isEmpty() && company.isEmpty() &&
            source.isEmpty()
}

sealed interface ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>
    data class Failure(val message: String) : ApiResult<Nothing>
}
