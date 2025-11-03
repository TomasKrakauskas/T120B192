package com.example.lw1.db

import androidx.room.TypeConverter

class IntListConverter {
    @TypeConverter
    fun listToCsv(list: List<Int>): String = list.joinToString(",")

    @TypeConverter
    fun csvToList(csv: String): List<Int> =
        if (csv.isBlank()) emptyList()
        else csv.split(",").map { it.trim().toInt() }
}