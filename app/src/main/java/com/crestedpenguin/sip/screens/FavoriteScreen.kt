package com.crestedpenguin.sip.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.crestedpenguin.sip.SipScreen
import com.crestedpenguin.sip.model.Supplement
import com.crestedpenguin.sip.model.SupplementImage
import com.crestedpenguin.sip.ui.SupplementViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    navController: NavController,
    supplementViewModel: SupplementViewModel,
    auth: FirebaseAuth
) {
    val firestore = Firebase.firestore
    val storageRef = FirebaseStorage.getInstance().reference
    val currentUser = auth.currentUser
    var favoriteSupplements by remember { mutableStateOf<List<String>>(emptyList()) }
    var supplements by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    favoriteSupplements = document.get("favorites") as? List<String> ?: emptyList()
                    favoriteSupplements.forEach { supplementId ->
                        firestore.collection("supplements").document(supplementId).get()
                            .addOnSuccessListener { document ->
                                supplements = supplements + document
                            }
                    }
                }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(text = "찜", fontSize = 24.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFAEC))
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(supplements) { supplementDocument ->
                val supplement = supplementDocument.toObject(Supplement::class.java)
                supplement?.let {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable {
                                supplementViewModel.supplementDocument = supplementDocument
                                navController.navigate(SipScreen.Supplement.route)
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = it.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            SupplementImage(it.name, storageRef)
                            Text(
                                text = "회사: ${it.company}",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "유형: ${it.supType}",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "가격: ${it.price} 원",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "단백질 20g당 가격: ${it.pricePerProteinWeight} 원",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "평균 평점: %.2f".format(it.avrRating),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "리뷰 수: ${it.reviewCount}",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
