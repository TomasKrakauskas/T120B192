package com.example.lw1.api

import retrofit2.http.GET
import retrofit2.http.Query
data class SizeDto(val minX: Int, val minY: Int, val maxX: Int, val maxY: Int)

data class ColumnRowDto(
    val x: Int,
    val y: Int,
    val readings: List<Int>
)

interface Api {
    @GET("size")
    suspend fun getSize(): SizeDto

    @GET("column")
    suspend fun getColumn(@Query("x") x: Int): List<ColumnRowDto>
}