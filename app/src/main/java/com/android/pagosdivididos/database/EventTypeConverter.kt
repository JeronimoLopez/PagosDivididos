package com.android.pagosdivididos.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.Instant


class ListExpensesConverter {
    @TypeConverter
    fun fromListExpenses(list: List<Expenses>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toListExpenses(json: String): List<Expenses> {
        val type = object : TypeToken<List<Expenses>>() {}.type
        return Gson().fromJson(json, type)
    }
}

class ListMemberConverter {
    @TypeConverter
    fun fromListMember(list: List<Member>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toListMember(json: String): List<Member> {
        val type = object : TypeToken<List<Member>>() {}.type
        return Gson().fromJson(json, type)
    }
}

class InstantTypeConverter {
    @TypeConverter
    fun fromInstant(instant: Instant): Long {
        return instant.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(timestamp: Long): Instant {
        return Instant.ofEpochMilli(timestamp)
    }
}