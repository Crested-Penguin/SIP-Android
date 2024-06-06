package com.crestedpenguin.sip.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crestedpenguin.sip.model.Comment
import com.crestedpenguin.sip.ui.SupplementViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query

@Composable
fun SupplementScreen(supplementViewModel: SupplementViewModel, auth: FirebaseAuth) {
    val context = LocalContext.current
    val supplement = supplementViewModel.supplementDocument
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var commentText by remember { mutableStateOf("") }

    // Fetch comments using the supplement reference
    LaunchedEffect(supplement) {
        val commentsRef = supplement?.reference?.collection("comments")
        commentsRef?.orderBy("timestamp", Query.Direction.ASCENDING)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.w("SupplementScreen", "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val commentsList = querySnapshot?.documents?.mapNotNull { it.toObject(Comment::class.java) } ?: emptyList()
                comments = commentsList
            }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Supplement ${supplement?.getString("company")}",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${supplement?.getString("description")}",
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${supplement?.getString("nutrient")}",
            fontSize = 10.sp
        )
        // Comments Section
        Text(text = "Comments", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        // Display comments
        comments.forEach { comment ->
            Text(text = "${comment.userEmail}: ${comment.commentText}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(5.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Comment input field
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            label = { Text("Add a comment") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val userEmail = auth.currentUser?.email
                if (userEmail != null && commentText.isNotBlank()) {
                    val newComment = hashMapOf(
                        "userEmail" to userEmail,
                        "commentText" to commentText,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    supplement?.reference?.collection("comments")?.add(newComment)
                    commentText = ""
                } else {
                    Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}