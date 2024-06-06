package com.crestedpenguin.sip.screens

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.crestedpenguin.sip.model.CompanyImage
import com.crestedpenguin.sip.ui.CompanyViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController, companyViewModel: CompanyViewModel, db: FirebaseFirestore, storageRef: StorageReference) {
    var companyList by remember {
        mutableStateOf<List<DocumentSnapshot>>(emptyList())
    }

    LaunchedEffect(Unit) {
        companyList =
            db.collection("companies")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        Log.d(ContentValues.TAG, "${document.id} = ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting documents.", exception)
                }
                .await()
                .documents
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
        ) {
            itemsIndexed(companyList) { _, document ->
                CompanyItem(navController, companyViewModel, document, storageRef)
            }
        }
    }
}

@Composable
fun CompanyItem(
    navController: NavController,
    companyViewModel: CompanyViewModel,
    company: DocumentSnapshot,
    storageRef: StorageReference
) {
    Card(
        onClick = {
            companyViewModel.companyDocument = company
            companyViewModel.storageRef = storageRef
            navController.navigate("company")
        },
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val companyName = company.getString("name") ?: "Empty"
            val keywords = company.get("keywords") as? List<*>

            CompanyImage(companyName, storageRef)
            Row {
                keywords?.take(3)?.forEach { keyword ->
                    Text(
                        text = "#$keyword",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    }
}