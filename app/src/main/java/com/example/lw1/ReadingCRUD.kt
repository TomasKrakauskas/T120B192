package com.example.lw1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lw1.api.Api
import com.example.lw1.api.getReadings
import com.example.lw1.db.ReadingDao
import com.example.lw1.models.Reading
import kotlinx.coroutines.launch

@Composable
fun ReadingCRUD(readingDao: ReadingDao, api: Api, navController: NavController, readings: List<Reading>, onFinished: () -> Unit) {
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column {
        if(readings.isEmpty())
            Text("No readings loaded. Please fetch readings", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        else
            Text("Current Readings: ${readings.size}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                enabled = !loading,
                onClick = { navController.navigate("list/add") }) {
                Text("Add new")
            }
            Spacer(Modifier.width(16.dp))
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
                            loading = false
                        } finally {
                            loading = false
                            onFinished()
                        }
                    }
                }
            ) {
                Text(if (loading) "Updating..." else if (readings.isEmpty()) "Fetch" else "Update")
            }
            if (readings.isNotEmpty() && !loading) {
                Spacer(Modifier.width(16.dp))
                Button(
                    onClick = {
                        scope.launch {
                            error = null
                            try {
                                readingDao.deleteAll()
                            } catch (t: Throwable) {
                                error = t.localizedMessage ?: t.toString()
                            }
                        }
                    }
                ) {
                    Text("Delete all")
                }
            }
        }

        error?.let { Text("Error: $it") }

        if (loading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator(modifier = Modifier.size(108.dp), strokeWidth= 16.dp)
            }
        }
    }
}