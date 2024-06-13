package com.crestedpenguin.sip

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crestedpenguin.sip.screens.*
import com.crestedpenguin.sip.ui.CompanyViewModel
import com.crestedpenguin.sip.ui.SupplementViewModel
import com.crestedpenguin.sip.ui.theme.SIPTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        auth = Firebase.auth
        val db = Firebase.firestore
        val storageRef = Firebase.storage.reference

        setContent {
            SIPTheme {
                val navController = rememberNavController()
                val supplementViewModel: SupplementViewModel = viewModel()
                val companyViewModel: CompanyViewModel = viewModel()

                Scaffold(
                    bottomBar = {
                        BottomAppBar {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    IconButton(onClick = { navController.navigate(SipScreen.Home.route) }) {
                                        Icon(Icons.Filled.Home, contentDescription = "Home")
                                    }
                                    Text(text = "홈", textAlign = TextAlign.Center)
                                }
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    IconButton(onClick = { navController.navigate(SipScreen.Search.route) }) {
                                        Icon(Icons.Filled.Search, contentDescription = "Search")
                                    }
                                    Text(text = " 검색", textAlign = TextAlign.Center)
                                }
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    IconButton(onClick = { navController.navigate(SipScreen.Favorite.route) }) {
                                        Icon(Icons.Filled.Favorite, contentDescription = "Favorite")
                                    }
                                    Text(text = " 찜목록", textAlign = TextAlign.Center)
                                }
                                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                                    IconButton(onClick = { navController.navigate(SipScreen.Settings.route) }) {
                                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                                    }
                                    Text(text = "설정", textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = SipScreen.Login.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(SipScreen.Login.route) {
                            LoginScreen(
                                navController = navController,
                                auth = auth
                            )
                        }
                        composable(SipScreen.AdditionalInfo.route) {
                            AdditionalInfoScreen(
                                navController = navController,
                                auth = auth
                            )
                        }
                        composable(SipScreen.Home.route) {
                            HomeScreen(
                                navController = navController,
                                companyViewModel = companyViewModel,
                                db = db,
                                storageRef = storageRef
                            )
                        }
                        composable(SipScreen.Company.route) { CompanyScreen(companyViewModel = companyViewModel, navController = navController, storageRef = storageRef) }
                        composable(SipScreen.Search.route) {
                            SearchScreen(
                                navController = navController,
                                supplementViewModel = supplementViewModel,
                                db = db,
                                storageRef = storageRef
                            )
                        }
                        composable(SipScreen.Supplement.route) {
                            SupplementScreen(
                                supplementViewModel = supplementViewModel,
                                auth = auth,
                                navController = navController
                            )
                        }
                        composable(SipScreen.Favorite.route) {
                            FavoriteScreen(
                                navController = navController,
                                supplementViewModel = supplementViewModel,
                                auth = auth,
                            )
                        }
                        composable(SipScreen.Settings.route) {
                            SettingsScreen(
                                navController = navController,
                                auth = auth
                            )
                        }
                    }
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        // Reload user data if needed
    }
}
