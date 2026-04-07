package com.example.uptime.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toMap(json: String): Map<String, String> {
        return gson.fromJson(json, object: TypeToken<Map<String, String>>() {}.type)
    }

    @TypeConverter
    fun fromStringSet(set: Set<String>): String {
        return gson.toJson(set)
    }

    @TypeConverter
    fun toStringSet(json: String): Set<String> {
        return gson.fromJson(json, object: TypeToken<Set<String>>() {}.type)
    }
}