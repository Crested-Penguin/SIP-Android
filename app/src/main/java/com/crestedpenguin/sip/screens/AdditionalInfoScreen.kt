package com.crestedpenguin.sip.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.crestedpenguin.sip.SipScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun AdditionalInfoScreen(navController: NavController, auth: FirebaseAuth) {
    val context = LocalContext.current
    val user = auth.currentUser
    val db = Firebase.firestore

    var nickname by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Additional Information", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Nickname Input
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Nickname") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Save and Continue Button
        Button(
            onClick = {
                if (nickname.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty()) {
                    val userInfo = hashMapOf(
                        "nickname" to nickname,
                        "weight" to weight.toFloatOrNull(),
                        "height" to height.toFloatOrNull()
                    )
                    user?.let {
                        db.collection("users").document(it.uid).set(userInfo)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(1.dp, Color.Black, shape = RoundedCornerShape(8.dp))
        ) {
            Text("Save and Continue", color = Color.Black)
        }
    }
}
