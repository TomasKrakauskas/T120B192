package com.example.lw1

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lw1.db.ReadingDao
import com.example.lw1.models.Reading

@Composable
fun Signal(readingDao: ReadingDao) {

    val readings by readingDao.getAll().collectAsState(initial = emptyList())

    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var closest by remember { mutableStateOf<Reading?>(null) }

    Column {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter Signal, e.g. 12, 15, 30") },
            singleLine = true
        )
        Spacer(Modifier.height(8.dp))

        Spacer(Modifier.height(8.dp))

        Button(onClick = {
            error = null
            closest = null
            try {
                closest = onSignalClick(input, readings)
                error = null
            } catch (t: Throwable) {
                error = t.message ?: "Invalid input"
            }
        }) {
            Text("Find")
        }

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        closest?.let { r ->
            Text("Closest: (x:${r.x}, y:${r.y})  readings: ${r.readings.joinToString(", ")}")
        }
    }
}

private fun onSignalClick(input: String, readings: List<Reading>): Reading {
    if (readings.isEmpty()) throw IllegalStateException("No readings in the database.")

    val parts = input.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) throw IllegalArgumentException("Enter comma-separated integers (e.g. 12, 15, 30).")
    val signal = parts.map { it.toIntOrNull() ?: throw IllegalArgumentException("Only integers are allowed.") }

    var best = readings.first()
    var bestSq = Double.MAX_VALUE
    for (r in readings) {
        Log.i("Reading", r.readings.joinToString("-"))
        val sq = euclidean(r.readings, signal)
        if(sq < bestSq) { bestSq = sq; best = r }
    }

    return best
}

private fun euclidean(reading: List<Int>, signal: List<Int>): Double {
    val l = signal.size;
    if(reading.size != l) return Double.MAX_VALUE

    var total = 0L;

    for (i in 0 until l) {
        val diff = reading[i] - signal[i]
        total += diff.toLong() * diff
    }

    return Math.sqrt(total.toDouble())
}
