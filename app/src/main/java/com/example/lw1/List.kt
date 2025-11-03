package com.example.lw1

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lw1.api.Api
import com.example.lw1.api.getReadings
import com.example.lw1.db.ReadingDao
import kotlinx.coroutines.launch

@Composable
fun List(readingDao: ReadingDao, api: Api) {
    val scope = rememberCoroutineScope()
    val readings by readingDao.getAll().collectAsState(initial = emptyList())

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }


    Column {
        if(readings.isEmpty())
            Text("No readings loaded. Please fetch readings")
        else
            Text("${readings.size} Readings")

        Button(
            enabled = !loading,
            onClick = {
                scope.launch {
                    loading = true
                    error = null
                    try {
                        getReadings(readingDao, api)
                    } catch (t: Throwable) {
                        error = t.localizedMessage ?: t.toString()
                    } finally {
                        loading = false
                    }
                }
            }
        ) { Text(if (loading) "Updating..." else if(readings.isEmpty()) "Fetch" else "Update") }

        if (loading) {
            CircularProgressIndicator()
        }
        error?.let { Text("Error: $it") }

        LazyColumn {
            items(readings) { reading ->
                Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(text = "x: ${reading.x.toString()}, y: ${reading.y.toString()}, signals: ${reading.readings.joinToString(", ")}")
                    }
                }
            }
        }
    }

}