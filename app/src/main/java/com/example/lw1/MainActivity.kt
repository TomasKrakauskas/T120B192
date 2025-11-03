package com.example.lw1

import Map
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.lw1.api.provideApiService
import com.example.lw1.db.AppDatabase
import com.example.lw1.ui.theme.LW1Theme

class MainActivity : ComponentActivity() {
    lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // init Room database
        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "app.db").build()

        setContent {
            val readingDao = remember { db.readingDao() }
            val api = remember { provideApiService() }

            LW1Theme {

                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // read current route so we know which tab is selected
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = backStackEntry?.destination?.route

                        NavigationBar {
                            // Map tab
                            NavigationBarItem(
                                selected = currentRoute == "map",
                                onClick = { navController.navigate("map") },
                                icon = { Text("M") },
                                label = { Text("Map") }
                            )

                            // List tab
                            NavigationBarItem(
                                selected = currentRoute == "list",
                                onClick = { navController.navigate("list") },
                                icon = { Text("L") },
                                label = { Text("List") }
                            )

                            // Signal
                            NavigationBarItem(
                                selected = currentRoute == "signal",
                                onClick = { navController.navigate("signal") },
                                icon = { Text("S") },
                                label = { Text("Signal") }
                            )
                        }
                    }) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "list",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("map") {
                            Map(readingDao, api)
                        }

                        composable("list") {
                            List(readingDao, api)
                        }

                        composable("signal") {
                            Signal(readingDao)
                        }
                    }
                }
            }
        }
    }
}
