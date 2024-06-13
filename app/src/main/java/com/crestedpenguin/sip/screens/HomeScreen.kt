package com.crestedpenguin.sip.screens

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.crestedpenguin.sip.ui.CompanyViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

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
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp), // 간격을 더 넓힘
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(companyList) { document ->
                CompanyItem(navController, companyViewModel, document, storageRef)
            }
        }
    }
}

@Composable
fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "BRAND",
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun CompanyItem(
    navController: NavController,
    companyViewModel: CompanyViewModel,
    company: DocumentSnapshot,
    storageRef: StorageReference
) {
    val companyName = company.getString("name") ?: "Empty"
    val keywords = company.get("keywords") as? List<*>

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                companyViewModel.companyDocument = company
                companyViewModel.storageRef = storageRef
                navController.navigate("company")
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFEF6).copy(alpha = 1f) // 투명한 베이지 색
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = companyName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .align(Alignment.Start)
            )

            HomeScreenCompanyImage(companyName, storageRef)

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                keywords?.take(3)?.forEach { keyword ->
                    Text(
                        text = "#$keyword",
                        fontSize = 16.sp,
                        style = TextStyle(lineHeight = 24.sp),
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenCompanyImage(companyName: String, storageRef: StorageReference) {
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
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp))
        )
    } ?: imageError?.let {
        Log.e(ContentValues.TAG, it)
    }
}
