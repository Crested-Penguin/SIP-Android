package com.crestedpenguin.sip

import android.os.Bundle
import androidx.activity.compose.setContent
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
import com.crestedpenguin.sip.screens.AdditionalInfoScreen
import com.crestedpenguin.sip.screens.CompanyScreen
import com.crestedpenguin.sip.screens.HomeScreen
import com.crestedpenguin.sip.screens.LoginScreen
//import com.crestedpenguin.sip.screens.Screen
import com.crestedpenguin.sip.screens.SearchScreen
import com.crestedpenguin.sip.screens.SettingsScreen
import com.crestedpenguin.sip.screens.StarScreen
import com.crestedpenguin.sip.screens.SupplementScreen
import com.crestedpenguin.sip.ui.CompanyViewModel
import com.crestedpenguin.sip.ui.SupplementViewModel
import com.crestedpenguin.sip.ui.theme.SIPTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
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
        // Initialize Firebase Auth
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        auth = Firebase.auth
        // Accesses a Cloud Firestore instance from your Activity
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
                                IconButton(onClick = { navController.navigate(SipScreen.Home.route) }) {
                                    Icon(Icons.Filled.Home, contentDescription = "Home")
                                }
                                IconButton(onClick = { navController.navigate(SipScreen.Search.route) }) {
                                    Icon(Icons.Filled.Search, contentDescription = "Search")
                                }
                                IconButton(onClick = { navController.navigate(SipScreen.Star.route) }) {
                                    Icon(Icons.Filled.Star, contentDescription = "Star")
                                }
                                IconButton(onClick = { navController.navigate(SipScreen.Settings.route) }) {
                                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
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
                                navController = navController
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
                        composable(SipScreen.Company.route) { CompanyScreen(companyViewModel = companyViewModel) }
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
                                auth = auth
                            )
                        }
                        composable(SipScreen.Star.route) { StarScreen() }
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
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun reload() {
        // Reload user data if needed
    }
}
