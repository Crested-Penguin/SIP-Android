package com.crestedpenguin.sip

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crestedpenguin.sip.screens.HomeScreen
import com.crestedpenguin.sip.screens.Screen
import com.crestedpenguin.sip.screens.SearchScreen
import com.crestedpenguin.sip.screens.SettingsScreen
import com.crestedpenguin.sip.screens.StarScreen
import com.crestedpenguin.sip.screens.SupplementScreen
import com.crestedpenguin.sip.ui.SupplementViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val supplementViewModel: SupplementViewModel = viewModel()

            Scaffold(
                bottomBar = {
                    BottomAppBar {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            IconButton(onClick = { navController.navigate("home") }) {
                                Icon(Icons.Filled.Home, contentDescription = "Home")
                            }
                            IconButton(onClick = { navController.navigate("search") }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search")
                            }
                            IconButton(onClick = { navController.navigate("star") }) {
                                Icon(Icons.Filled.Star, contentDescription = "Star")
                            }
                            IconButton(onClick = { navController.navigate("settings") }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Home.route) { HomeScreen() }
                    composable(Screen.Search.route) {
                        SearchScreen(
                            navController = navController,
                            supplementViewModel = supplementViewModel
                        )
                    }
                    composable(Screen.Supplement.route) { SupplementScreen(supplementViewModel = supplementViewModel) }
                    composable(Screen.Star.route) { StarScreen() }
                    composable(Screen.Settings.route) { SettingsScreen() }
                }
            }
        }
    }
}