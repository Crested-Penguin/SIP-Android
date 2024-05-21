package com.crestedpenguin.sip

import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.crestedpenguin.sip.model.Supplement
import com.crestedpenguin.sip.screens.HomeScreen
import com.crestedpenguin.sip.screens.Screen
import com.crestedpenguin.sip.screens.SearchScreen
import com.crestedpenguin.sip.screens.SettingsScreen
import com.crestedpenguin.sip.screens.StarScreen
import com.crestedpenguin.sip.screens.SupplementScreen
import com.crestedpenguin.sip.ui.SupplementViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
        storage = Firebase.storage

        enableEdgeToEdge()

        // 업로드 작업을 onCreate에서 바로 실행
        uploadTxtFileToFirestore {
            setContent {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun uploadTxtFileToFirestore(onComplete: () -> Unit) {
        val storageRef = storage.reference
        val txtRef = storageRef.child("supplementData.txt") // 경로를 올바르게 지정
        txtRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val inputStream = bytes.inputStream()
            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

            reader.use { bufferedReader ->
                val supplements = mutableListOf<Supplement>()
                var line: String?
                var currentData = mutableMapOf<String, String>()

                while (bufferedReader.readLine().also { line = it } != null) {
                    val fixedLine = line?.trim()
                    if (fixedLine.isNullOrEmpty() || fixedLine == "==================================================") {
                        if (currentData.isNotEmpty()) {
                            supplements.add(createSupplementFromData(currentData))
                            currentData = mutableMapOf()
                        }
                        continue
                    }

                    val keyValue = fixedLine.split(":").map { it.trim() }
                    if (keyValue.size == 2) {
                        currentData[keyValue[0]] = keyValue[1]
                    }
                }

                if (currentData.isNotEmpty()) {
                    supplements.add(createSupplementFromData(currentData))
                }

                // Firestore에 데이터 업로드
                supplements.forEach { supplement ->
                    val documentId = UUID.randomUUID().toString()
                    firestore.collection("supplements").document(documentId)
                        .set(supplement)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot added with ID: $documentId")
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                        }
                }
            }
            onComplete()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error downloading file", exception)
            onComplete()
        }
    }

    private fun createSupplementFromData(data: Map<String, String>): Supplement {
        return Supplement(
            name = data["이름"] ?: "",
            company = data["회사"] ?: "",
            price = data["가격"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0,
            weight = data["무게"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0,
            avrRating = data["평균평점"]?.toDoubleOrNull() ?: 0.0,
            supType = data["유형"] ?: "",
            flavor = data["맛"] ?: "",
            servingSize = data["서빙 그램"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0,
            servSizeProtein = data["서빙당 단백질"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0,
            pricePerProteinWeight = data["단백질 20g당 가격"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
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

fun createSupplementFromData(data: Map<String, String>): Supplement {
    return Supplement(
        name = data["이름"] ?: "",
        company = data["회사"] ?: "",
        price = data["가격"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0,
        weight = data["무게"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0,
        avrRating = data["평균평점"]?.toDoubleOrNull() ?: 0.0,
        supType = data["유형"] ?: "",
        flavor = data["맛"] ?: "",
        servingSize = data["서빙 그램"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0,
        servSizeProtein = data["서빙당 단백질"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0,
        pricePerProteinWeight = data["단백질 20g당 가격"]?.replace("[^\\d.]".toRegex(), "")?.toDoubleOrNull() ?: 0.0
    )
}
