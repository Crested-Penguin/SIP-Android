package com.crestedpenguin.sip

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val supplementViewModel: SupplementViewModel = viewModel()

            Scaffold(
                bottomBar = {
                    BottomAppBar(
                        actions = {
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
                    )
                },
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
                if (supplementViewModel.showBottomSheet == true) {
                    ModalBottomSheet(
                        onDismissRequest = { supplementViewModel.showBottomSheet = false },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                FilterChip(target = "isWPC")
                                FilterChip(target = "isWPI")
                                FilterChip(target = "isWPH")
                            }
                        }
                    }
                }
            }
        }
    }
}