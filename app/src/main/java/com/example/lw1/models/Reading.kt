package com.example.lw1.models

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "readings",
    primaryKeys = ["x", "y"],
    indices = [
        Index("x"),
        Index("y")
    ]
)
data class Reading(
    val x: Int,
    val y: Int,
    val readings: List<Int>
)