package com.crestedpenguin.sip.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@Composable
fun SettingsScreen(navController: NavController, auth: FirebaseAuth) {
    val user = Firebase.auth.currentUser

    // Remove later
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    fun uploadSupplement(supplement: Map<String, Any>, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("supplements")
            .add(supplement)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally) {
        // User email
        Text(
            text = "Email: ${user?.email ?: "Not available"}",
        )
        Spacer(modifier = Modifier.height(16.dp))
        // Logout
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        // Remove later
        Button(onClick = {
            val supplementData = hashMapOf(
                "name" to "임팩트 비건 프로틴",
                "company" to "마이프로틴",
                "supType" to "비건",
                "price" to 29900,
                "weight" to 0.5,
                "flavor" to listOf("커피 앤 호두", "무맛", "바닐라"),
                "servingSize" to 30,
                "servSizeProtein" to 24,
                "pricePerProteinWeight" to 1495,
                "avrRating" to 4.26,
                "description" to "안녕하세요",
            )

            uploadSupplement(
                supplement = supplementData,
                onSuccess = {
                    Toast.makeText(context, "Supplement uploaded successfully", Toast.LENGTH_SHORT)
                        .show()
                    navController.navigate("home")
                },
                onError = {
                    Toast.makeText(
                        context,
                        "Error uploading supplement: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }) {}
    }
}