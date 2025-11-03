package com.example.lw1.api

import com.example.lw1.db.ReadingDao
import com.example.lw1.models.Reading

suspend fun getReadings(readingDao: ReadingDao, api: Api) {
    val size = api.getSize()
    for (x in size.minX..size.maxX) {
        val rows = api.getColumn(x)
        val readings = rows.map { r -> Reading(x = r.x, y = r.y, readings = r.readings) }
        readingDao.insertColumn(readings)
    }
}