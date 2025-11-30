package com.example.lw1

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

private val ReadingPlaceholder = Reading(x = -1, y = -1, readings = emptyList())

@Composable
fun EditStrength(readingDao: ReadingDao, navController: NavController, x: Int?, y: Int?) {
    val scope = rememberCoroutineScope()

    val isValid = x != null && y != null

    val reading by remember(x, y) {
        if (isValid) readingDao.getOne(x, y) else flowOf(null)
    }.collectAsState(initial = ReadingPlaceholder)


    LaunchedEffect(reading) {
        if (!isValid || reading == null) {
            Log.i("Edit Strength", "No Reading found")
            navController.popBackStack()
        }
    }

    if (reading == ReadingPlaceholder) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Loading or reading not found...")
            CircularProgressIndicator(modifier = Modifier.size(108.dp), strokeWidth= 16.dp)
        }
        return
    }

    var input by remember { mutableStateOf("") }

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
            Text("Editing -> X: ${x.toString()}, Y: ${y.toString()}")
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
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = {
                    Text(
                        "Readings: ${reading!!.readings.joinToString(", ")}"
                    )
                },
                modifier = Modifier.width(256.dp),
                singleLine = true
            )
            Spacer(Modifier.width(16.dp))
            Button(
                enabled = input.isNotEmpty(),
                modifier = Modifier.padding(top = 8.dp).size(56.dp),
                contentPadding = PaddingValues(0.dp),
                onClick = {
                    scope.launch {
                        message = null
                        error = null
                        val initial = reading!!.readings.joinToString(", ")
                        var result: Reading? = null
                        try {
                            result = onSignalEdit(reading, input)
                            readingDao.insertReading(result)
                        } catch (t: Throwable) {
                            error = t.message ?: "Invalid input"
                        } finally {
                            result?.let {
                                r ->
                                    message =
                                        "Updated reading X: ${x.toString()}, Y: ${y.toString()}\n"
                                    message += "Initial reading -> $initial\n"
                                    message += "New reading -> ${r.readings.joinToString(", ")}"
                            }
                            input = ""
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit"
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        error?.let { Text("Error: $it") }
        message?.let { Text(it) }
    }
}

private fun onSignalEdit(reading: Reading?, input: String): Reading {
    if (reading == null) throw IllegalStateException("Could not find reading in database")

    val rSize = reading.readings.size

    val parts = input.split(',').map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.isEmpty()) throw IllegalArgumentException("Enter comma-separated integers (e.g. 12, 15, 30).")
    val readings = parts.map { it.toIntOrNull() ?: throw IllegalArgumentException("Only integers are allowed.") }
    if (parts.size != rSize) throw IllegalArgumentException("Comma-separated integers do not match reading structure: \nEntered size - ${rSize}, \nReadings size - ${parts.size}")

    return Reading(reading.x, reading.y, readings)
}