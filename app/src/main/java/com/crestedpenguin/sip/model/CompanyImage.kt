package com.crestedpenguin.sip.model

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
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
                .padding(8.dp) // 이미지 주위에 패딩 추가
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)) // 라운드 처리
                .background(  Color(0xFFFFFEF6).copy(alpha = 1f))

        )
    } ?: imageError?.let {
        Log.e(ContentValues.TAG, it)
    }
}
