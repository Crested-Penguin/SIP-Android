package com.crestedpenguin.sip.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
                        weight = document.getDouble("weight")?.let { String.format("%.1f", it) } ?: ""
                        height = document.getDouble("height")?.let { String.format("%.1f", it) } ?: ""
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Gray,
                unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Weight Input
        OutlinedTextField(
            value = weight,
            onValueChange = { newWeight ->
                if (newWeight.isEmpty() || newWeight.matches(Regex("^\\d*\\.?\\d?\$"))) {
                    weight = newWeight
                }
            },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            trailingIcon = {
                Text("kg", color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Gray,
                unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Height Input
        OutlinedTextField(
            value = height,
            onValueChange = { newHeight ->
                if (newHeight.isEmpty() || newHeight.matches(Regex("^\\d*\\.?\\d?\$"))) {
                    height = newHeight
                }
            },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            trailingIcon = {
                Text("cm", color = Color.Gray, modifier = Modifier.padding(end = 8.dp))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Gray,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Gray,
                unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .border(1.dp, Color.Black)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .border(1.dp, Color.Black)
        ) {
            Text("Logout", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delete Account Button
        // Delete Account Button with red background and rectangular border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Red, shape = RoundedCornerShape(8.dp))
                .background(Color.Red) // 배경색을 빨간색으로 설정
        ) {
            Button(
                onClick = {
                    user?.let { currentUser ->
                        // Firestore에서 사용자 정보 삭제
                        firestore.collection("users").document(currentUser.uid).delete()
                            .addOnSuccessListener {
                                // Firebase Authentication에서 사용자 계정 삭제
                                currentUser.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
                                        navController.navigate("login") {
                                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error deleting account: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error deleting user data: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                shape = RoundedCornerShape(8.dp), // 둥근 모서리를 유지
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Red, shape = RoundedCornerShape(8.dp)) // 버튼의 테두리 색상
            ) {
                Text("Delete Account", color = Color.White)
            }
        }
    }
}
