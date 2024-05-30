package com.crestedpenguin.sip.screens

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

@Composable
fun HomeScreen(navController: NavController) {
    // Accesses a Cloud Firestore instance from your Activity
    val db = Firebase.firestore
    var companyList by remember {
        mutableStateOf<List<DocumentSnapshot>>(emptyList())
    }
    val storageRef = Firebase.storage.reference

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
                CompanyItem(navController, document, storageRef)
            }
        }
    }
}

@Composable
fun CompanyItem(
    navController: NavController,
    company: DocumentSnapshot,
    storageRef: StorageReference
) {
    Card(
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

@Composable
fun CompanyImage(companyName: String, storageRef: StorageReference) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var imagePath by remember { mutableStateOf<File?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(companyName) {
        scope.launch(Dispatchers.IO) {
            try {
                val localFile = File(context.cacheDir, "$companyName.png")
                Log.d(ContentValues.TAG, "Attempting to download image: $companyName.png")
                storageRef.child("companies/${companyName}.png").getFile(localFile).await()
                Log.d(ContentValues.TAG, "Image downloaded successfully: $companyName.png")
                imagePath = localFile
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(ContentValues.TAG, "Error downloading image: $companyName.png", e)
                imageError = "Error downloading image: ${e.message}"
            }
        }
    }

    imagePath?.let { file ->
        Log.d(ContentValues.TAG, "Loading image from: ${file.absolutePath}")
        AsyncImage(
            model = file,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    } ?: imageError?.let {
        Log.e(ContentValues.TAG, it)
    }
}