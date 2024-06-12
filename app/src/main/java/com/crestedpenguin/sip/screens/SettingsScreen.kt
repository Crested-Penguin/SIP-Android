package com.crestedpenguin.sip.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.crestedpenguin.sip.SipScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun SettingsScreen(navController: NavController, auth: FirebaseAuth) {
    val user = Firebase.auth.currentUser
    val context = LocalContext.current
    val firestore = Firebase.firestore

    var nickname by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    LaunchedEffect(user) {
        user?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        nickname = document.getString("nickname") ?: ""
                        weight = document.getDouble("weight")?.toString() ?: ""
                        height = document.getDouble("height")?.toString() ?: ""
                    }
                }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User email
        Text(text = "Email: ${user?.email ?: "Not available"}")
        Spacer(modifier = Modifier.height(16.dp))

        // Nickname Input
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Weight Input
        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            trailingIcon = {
                Text("kg", color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Height Input
        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            trailingIcon = {
                Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
        Button(
            onClick = {
                if (nickname.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty()) {
                    val userInfo = hashMapOf(
                        "nickname" to nickname,
                        "weight" to weight.toFloatOrNull(),
                        "height" to height.toFloatOrNull()
                    )
                    user?.let {
                        firestore.collection("users").document(it.uid).set(userInfo)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Information saved successfully", Toast.LENGTH_LONG).show()
                                navController.navigate(SipScreen.Home.route) {
                                    popUpTo(SipScreen.Login.route) { inclusive = true }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error saving information: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_LONG).show()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFEF6)
            ),
            modifier = Modifier.fillMaxWidth().padding(8.dp).border(1.dp, Color.Black)
        ) {
            Text("Save", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = {
                auth.signOut()
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFFEF6)
            ),
            modifier = Modifier.fillMaxWidth().padding(8.dp).border(1.dp, Color.Black)
        ) {
            Text("Logout", color = Color.Black)
        }
    }
}
