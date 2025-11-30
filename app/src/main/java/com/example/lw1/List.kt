package com.example.lw1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import com.example.lw1.api.Api
import com.example.lw1.db.ReadingDao
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun List(readingDao: ReadingDao, api: Api, navController: NavController) {
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }


    val readings by readingDao.getAll()
        .map { list ->
            list.sortedWith(compareBy({ it.x }, { it.y }))
        }
        .collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp),
    ) {
        ReadingCRUD(readingDao, api, navController, readings, onFinished = {})
        // manual delete error
        error?.let { Text("Error: $it") }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            LazyColumn {
                items(readings) { reading ->
                    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("x: ${reading.x}, y: ${reading.y}, signals: ${reading.readings.joinToString(", ")}")
                            Row {
                                Button(
                                    modifier = Modifier.size(width = 32.dp, height = 32.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    onClick = { navController.navigate("list/edit/${reading.x}/${reading.y}") }) {
                                    Icon(
                                        modifier = Modifier.size(16.dp),
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Button"
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    modifier = Modifier.size(width = 32.dp, height = 32.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    onClick = {
                                        scope.launch {
                                            error = null
                                            try {
                                                readingDao.deleteOne(reading.x, reading.y)
                                            } catch (t: Throwable) {
                                                error = t.localizedMessage ?: t.toString()
                                            }
                                        }

                                    }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(16.dp),
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Button"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }

}