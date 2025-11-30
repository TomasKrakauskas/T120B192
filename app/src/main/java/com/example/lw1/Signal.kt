package com.example.lw1

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Enter Signal, e.g. 12, 15, 30") },
                modifier = Modifier.width(256.dp),
                singleLine = true
            )
            Spacer(Modifier.width(16.dp))
            Button(
                enabled = input.isNotEmpty(),
                modifier = Modifier.padding(top = 8.dp).size(56.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    error = null
                    closest = null
                    try {
                        closest = onSignalClick(input, readings)
                        error = null
                    } catch (t: Throwable) {
                        error = t.message ?: "Invalid input"
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Find"
                )
            }
        }


        Spacer(Modifier.height(12.dp))

        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        closest?.let { r ->
            Column {
                Text("Closest -> x: ${r.x}, y: ${r.y}")
                Text("Readings -> ${r.readings.joinToString(", ")}")
            }
        }
    }
}

private fun onSignalClick(input: String, readings: List<Reading>): Reading {
    if (readings.isEmpty()) throw IllegalStateException("No readings in the database.")

    val rSize = readings.first().readings.size

    val parts = input.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) throw IllegalArgumentException("Enter comma-separated integers (e.g. 12, 15, 30).")
    val signal = parts.map { it.toIntOrNull() ?: throw IllegalArgumentException("Only integers are allowed.") }
    if (parts.size != rSize) throw IllegalArgumentException("Comma-separated integers do not match reading structure: \nEntered size - ${rSize}, \nReadings size - ${parts.size}")

    var best = readings.first()
    var bestSq = Double.MAX_VALUE
    for (r in readings) {
        val sq = euclidean(r.readings, signal)
        if(sq < bestSq) { bestSq = sq; best = r }
    }

    return best
}

private fun euclidean(reading: List<Int>, signal: List<Int>): Double {
    val l = signal.size
    if(reading.size != l) return Double.MAX_VALUE

    var total = 0L

    for (i in 0 until l) {
        val diff = reading[i] - signal[i]
        total += diff.toLong() * diff
    }

    return Math.sqrt(total.toDouble())
}
