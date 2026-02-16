package com.example.myquizbite.data.local

import androidx.room.TypeConverter
import com.example.myquizbite.data.model.Difficulty
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromStringList(list: List<String>): String = JSONArray(list).toString()

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        val arr = JSONArray(value)
        return List(arr.length()) { arr.getString(it) }
    }

    @TypeConverter
    fun fromDifficulty(d: Difficulty): String = d.name

    @TypeConverter
    fun toDifficulty(s: String): Difficulty = Difficulty.valueOf(s)
}
