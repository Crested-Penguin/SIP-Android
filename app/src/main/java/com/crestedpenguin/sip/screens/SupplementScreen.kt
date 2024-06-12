package com.crestedpenguin.sip.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.crestedpenguin.sip.model.Comment
import com.crestedpenguin.sip.model.Supplement
import com.crestedpenguin.sip.ui.SupplementViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementScreen(supplementViewModel: SupplementViewModel, auth: FirebaseAuth, navController: NavController) {
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val supplement = supplementViewModel.supplementDocument
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var showReviewForm by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var reviewCount by remember { mutableStateOf(0) }
    var averageRating by remember { mutableStateOf(0f) }
    var nickname by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("최신순") }
    val coroutineScope = rememberCoroutineScope()

    // Fetch comments using the supplement reference
    LaunchedEffect(supplement) {
        val commentsRef = supplement?.reference?.collection("comments")
        commentsRef?.orderBy("timestamp", Query.Direction.ASCENDING)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    return@addSnapshotListener
                }
                val commentsList = querySnapshot?.documents?.mapNotNull { it.toObject<Comment>() } ?: emptyList()
                comments = commentsList
                reviewCount = commentsList.size
                averageRating = if (commentsList.isNotEmpty()) {
                    commentsList.map { it.rating.toFloat() }.average().toFloat()
                } else {
                    0f
                }
            }

        supplement?.getString("name")?.let { name ->
            coroutineScope.launch {
                val count = Supplement(name).getReviewCount(supplement.id)
                reviewCount = count
            }
        }

        // Fetch user nickname
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    nickname = document.getString("nickname") ?: "Unknown"
                }
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text(text = "${supplement?.getString("company")}", fontSize = 20.sp) }, modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFAEC)),
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Backward")} }
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
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Reviews: $reviewCount, Average Rating: ${String.format("%.1f", averageRating)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Comments Section
        Text(text = "Comments", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        var dropdownExpanded by remember { mutableStateOf(false) }
        val sortOptions = listOf("최신순", "평점 높은 순", "평점 낮은 순")

        Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart)) {
            Button(
                onClick = { dropdownExpanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, // 배경색을 명확히 설정
                    contentColor = Color.Black // 텍스트 색상 설정
                ),
                border = BorderStroke(1.dp, Color.Black), // 테두리 설정
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(sortOption)
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Sort Options")
            }

            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                sortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            sortOption = option
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        val sortedComments = when (sortOption) {
            "평점 높은 순" -> comments.sortedByDescending { it.rating }
            "평점 낮은 순" -> comments.sortedBy { it.rating }
            else -> comments.sortedByDescending { it.timestamp }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(sortedComments) { comment ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = comment.userEmail, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        RatingBar(rating = comment.rating, onRatingChanged = {}, size = 16, isEditable = false)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = comment.commentText, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showReviewForm = !showReviewForm },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, // 배경색을 명확히 설정
                contentColor = Color.Black // 텍스트 색상 설정
            ),
            border = BorderStroke(1.dp, Color.Black), // 테두리 설정
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(if (showReviewForm) "Hide Review Form" else "Add a Review")
        }

        AnimatedVisibility(visible = showReviewForm) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Comment input field
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Add a comment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // RatingBar
                Text(text = "Rate this supplement", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                RatingBar(rating = rating, onRatingChanged = { newRating -> rating = newRating }, size = 32)

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, // 배경색을 명확히 설정
                        contentColor = Color.Black // 텍스트 색상 설정
                    ),
                    border = BorderStroke(1.dp, Color.Black), // 테두리 설정
                    shape = RoundedCornerShape(8.dp),
                    onClick = {
                        val userEmail = nickname // Use nickname instead of email
                        if (userEmail.isNotBlank() && commentText.isNotBlank()) {
                            val newComment = hashMapOf(
                                "userEmail" to userEmail,
                                "commentText" to commentText,
                                "rating" to rating,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                            coroutineScope.launch {
                                firestore.runTransaction { transaction ->
                                    supplement?.reference?.let {
                                        transaction.update(it, "reviewCount", FieldValue.increment(1))
                                        transaction.update(it, "avrRating", (averageRating * reviewCount + rating) / (reviewCount + 1))
                                    }
                                    supplement?.reference?.collection("comments")?.add(newComment)
                                }.addOnSuccessListener {
                                    commentText = ""
                                    rating = 0
                                    showReviewForm = false
                                }.addOnFailureListener { e ->
                                    Toast.makeText(context, "Error submitting comment: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
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
    }
}

@Composable
fun RatingBar(rating: Int, onRatingChanged: (Int) -> Unit, size: Int, isEditable: Boolean = true) {
    Row {
        for (i in 1..5) {
            val starColor = if (i <= rating) Color(0xFFFFD700) else Color.Gray

            Box(
                Modifier.clickable(
                    enabled = isEditable,
                    onClick = { onRatingChanged(i) }
                )
            ) {
                Text(
                    text = "★",
                    fontSize = size.sp,
                    color = starColor,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
