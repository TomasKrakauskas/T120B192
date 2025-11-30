package com.example.lw1

import Map
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                        val currentTopLevel = backStackEntry?.destination?.route?.substringBefore("/")

                        NavigationBar {
                            // Map tab
                            NavigationBarItem(
                                selected = currentTopLevel == "map",
                                onClick = { navController.navigate("map") },
                                icon = {
                                    Icon(
                                        contentDescription = "Map",
                                        imageVector = Icons.Default.LocationOn
                                    )
                                },
                                label = { Text("Map") }
                            )

                            // List tab
                            NavigationBarItem(
                                selected = currentTopLevel == "list",
                                onClick = { navController.navigate("list") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = "List",
                                    )
                                },
                                label = { Text("List") }
                            )

                            // Signal
                            NavigationBarItem(
                                selected = currentTopLevel == "signal",
                                onClick = { navController.navigate("signal") },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Signal",
                                    )
                                },
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
                            List(readingDao, api, navController)
                        }

                        composable(
                            "list/edit/{x}/{y}",
                            arguments = listOf(
                                navArgument("x") { type = NavType.IntType },
                                navArgument("y") { type = NavType.IntType }
                            )
                        ) { entry ->
                            val x = entry.arguments?.getInt("x")
                            val y = entry.arguments?.getInt("y")

                            EditStrength(readingDao, navController, x, y)
                        }

                        composable("list/add") {
                            AddStrength(readingDao, navController)
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
