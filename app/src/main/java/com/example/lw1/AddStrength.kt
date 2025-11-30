package com.example.lw1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lw1.db.ReadingDao
import com.example.lw1.models.Reading
import kotlinx.coroutines.launch


@Composable
fun AddStrength(readingDao: ReadingDao, navController: NavController) {
    val readings by readingDao.getAll().collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var input by remember { mutableStateOf("") }
    var xInput by remember { mutableStateOf("") }
    var yInput by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Create New Reading")
            Button(
                modifier = Modifier.size(width = 108.dp, height = 32.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
                Text("Go back")
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = xInput,
                onValueChange = { xInput = it },
                label = { Text("X - Integer value") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = yInput,
                onValueChange = { yInput = it },
                label = { Text("Y - Integer value") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Enter Signal, e.g. 12, 15, 30") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(
            enabled = input.isNotEmpty(),
            modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally),
            onClick = {
                scope.launch {
                    var messageStart = ""
                    var result: Reading? = null
                    var initial: String? = null
                    message = null
                    error = null
                    try {
                        val rSize = readings.first().readings.size
                        result = onSignalCreate(rSize, xInput, yInput, input)

                        val reading = readings.find { it.x == result.x && it.y == result.y }
                        if (reading != null) {
                            messageStart = "Updated"
                            initial = reading.readings.joinToString(", ")
                        } else {
                            messageStart = "Created"
                        }

                        readingDao.insertReading(result)
                    } catch (t: Throwable) {
                        error = t.message ?: "Invalid input"
                    } finally {
                        result?.let {
                            r ->
                                message = "$messageStart Reading ${r.x}, Y: ${r.y}\n"
                                if(initial != null) message += "Initial reading -> $initial\n"
                                message += "New reading -> ${r.readings.joinToString(", ")}"
                        }
                        input = ""
                        xInput = ""
                        yInput = ""
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add"
            )
            Text("Add new reading")
        }

        Spacer(Modifier.height(16.dp))
        error?.let { Text("Error: $it") }
        message?.let { Text(it) }
    }
}

private fun onSignalCreate(rSize: Int, xInput: String, yInput: String, input: String): Reading {
    val x = xInput.toIntOrNull()
    if(x == null) throw IllegalArgumentException("X must be a valid integer")

    val y = yInput.toIntOrNull()
    if(y == null) throw IllegalArgumentException("Y must be a valid integer")

    val parts = input.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) throw IllegalArgumentException("Enter comma-separated integers (e.g. 12, 15, 30).")

    val readings = parts.map { it.toIntOrNull() ?: throw IllegalArgumentException("Only integers are allowed.") }
    if (rSize != 0 && readings.size != rSize) throw IllegalArgumentException("Comma-separated integers do not match reading structure: \nEntered size - ${rSize}, \nReadings size - ${parts.size}")

    return Reading(x, y, readings)
}