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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.crestedpenguin.sip.model.Comment
import com.crestedpenguin.sip.model.Supplement
import com.crestedpenguin.sip.model.SupplementImage
import com.crestedpenguin.sip.ui.SupplementViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementScreen(
    supplementViewModel: SupplementViewModel,
    auth: FirebaseAuth,
    navController: NavController
) {
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val storageRef = FirebaseStorage.getInstance().reference
    val supplement = supplementViewModel.supplementDocument
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var showReviewDialog by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }
    var reviewCount by remember { mutableStateOf(0) }
    var averageRating by remember { mutableStateOf(0f) }
    var nickname by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("최신순") }
    var isFavorite by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch comments using the supplement reference
    LaunchedEffect(supplement) {
        val commentsRef = supplement?.reference?.collection("comments")
        commentsRef?.orderBy("timestamp", Query.Direction.ASCENDING)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    return@addSnapshotListener
                }
                val commentsList =
                    querySnapshot?.documents?.mapNotNull { it.toObject<Comment>() } ?: emptyList()
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

        // Fetch user nickname and favorite status
        auth.currentUser?.uid?.let { uid ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    nickname = document.getString("nickname") ?: "Unknown"
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    isFavorite = supplement?.id in favorites
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
        supplement?.let {
            TopAppBar(
                title = { Text(text = it.getString("name") ?: "Supplement", fontSize = 20.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFAEC)),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Backward"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Supplement Image
            SupplementImage(it.getString("name") ?: "", storageRef)

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = it.getString("description") ?: "",
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = it.getString("nutrient") ?: "",
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Reviews: $reviewCount | Average Rating: ${
                    String.format(
                        "%.1f",
                        averageRating
                    )
                }",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Display flavors
            if (it.get("flavor") is List<*>) {
                val flavors = it.get("flavor") as List<*>
                if (flavors.isNotEmpty()) {
                    Text(
                        text = "Flavors: ${flavors.joinToString(", ")}",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Favorite Button
            Button(
                onClick = {
                    val currentUser = auth.currentUser
                    if (currentUser != null && supplement != null) {
                        val userRef = firestore.collection("users").document(currentUser.uid)
                        val updateAction =
                            if (isFavorite) FieldValue.arrayRemove(supplement.id) else FieldValue.arrayUnion(
                                supplement.id
                            )
                        userRef.update("favorites", updateAction)
                            .addOnSuccessListener {
                                isFavorite = !isFavorite
                                val message =
                                    if (isFavorite) "Added to favorites" else "Removed from favorites"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error updating favorites: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFavorite) Color.Red else Color.White, // Favorite 버튼 배경색
                    contentColor = if (isFavorite) Color.White else Color.Black // Favorite 버튼 텍스트색
                ),
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite"
                )
                Text(text = if (isFavorite) "찜 취소" else "찜하기")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Comments Section
        Text(text = "Comments", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        var dropdownExpanded by remember { mutableStateOf(false) }
        val sortOptions = listOf("최신순", "평점 높은 순", "평점 낮은 순")

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopStart)
        ) {
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
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.Start // 리뷰를 왼쪽 정렬로 변경
                    ) {
                        Text(
                            text = comment.userEmail,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        RatingBar(
                            rating = comment.rating,
                            onRatingChanged = {},
                            size = 16,
                            isEditable = false
                        )
                        Spacer(modifier = Modifier.height(4.dp)) // add space between rating and comment text
                        Text(
                            text = "Posted on: ${
                                comment.timestamp?.let {
                                    SimpleDateFormat(
                                        "yyyy-MM-dd",
                                        Locale.getDefault()
                                    ).format(it.toDate())
                                } ?: "Unknown"
                            }",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = comment.commentText, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { showReviewDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White, // 배경색을 명확히 설정
                contentColor = Color.Black // 텍스트 색상 설정
            ),
            border = BorderStroke(1.dp, Color.Black), // 테두리 설정
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add a Review")
        }

        if (showReviewDialog) {
            ReviewDialog(
                onDismiss = { showReviewDialog = false },
                onSubmit = { commentText, rating ->
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
                                    transaction.update(
                                        it,
                                        "avrRating",
                                        (averageRating * reviewCount + rating) / (reviewCount + 1)
                                    )
                                }
                                supplement?.reference?.collection("comments")?.add(newComment)
                            }.addOnSuccessListener {
                                showReviewDialog = false
                            }.addOnFailureListener { e ->
                                Toast.makeText(
                                    context,
                                    "Error submitting comment: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Comment cannot be empty", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            )
        }
    }
}

@Composable
fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (String, Int) -> Unit) {
    var commentText by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Add a Review", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("Add a comment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Increase the height to ensure it is not cut off
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Rate this supplement", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                RatingBar(
                    rating = rating,
                    onRatingChanged = { newRating -> rating = newRating },
                    size = 32
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray, // 배경색을 명확히 설정
                            contentColor = Color.Black // 텍스트 색상 설정
                        )
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { onSubmit(commentText, rating) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, // 배경색을 명확히 설정
                            contentColor = Color.Black // 텍스트 색상 설정
                        ),
                        border = BorderStroke(1.dp, Color.Black) // 테두리 설정
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    size: Int,
    isEditable: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start // 별을 왼쪽 정렬로 변경
    ) {
        for (i in 1..5) {
            val starColor = if (i <= rating) Color(0xFFFFD700) else Color.Gray

            Box(
                Modifier
                    .size(size.dp * 1.5f) // Ensure the size is properly set for each star
                    .clickable(
                        enabled = isEditable,
                        onClick = { onRatingChanged(i) }
                    )
                    .padding(4.dp) // Add padding for better spacing
            ) {
                Text(
                    text = "★",
                    fontSize = size.sp,
                    color = starColor,
                    modifier = Modifier.align(Alignment.Center) // Center align the star
                )
            }
        }
    }
}
