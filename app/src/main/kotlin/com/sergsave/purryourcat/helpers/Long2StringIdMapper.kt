package com.sergsave.purryourcat.helpers

class Long2StringIdMapper {
    companion object {
        const val INVALID_ID = Long.MAX_VALUE
    }

    private val long2string = mutableMapOf<Long, String>()
    private val string2long = mutableMapOf<String, Long>()
    private var idCounter: Long = 0

    fun longIdFrom(stringId: String): Long {
        var id = string2long.get(stringId)

        if(id == null) {
            id = idCounter
            string2long.put(stringId, id)
            long2string.put(id, stringId)
            idCounter += 1
        }

        return id
    }

    fun stringIdFrom(longId: Long): String? {
        return long2string.get(longId)
    }
}