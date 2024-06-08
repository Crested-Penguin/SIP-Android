package com.crestedpenguin.sip.model

import com.google.firebase.Timestamp

// Data class for comment
data class Comment(
    val userEmail: String = "",
    val commentText: String = "",
    val timestamp: Timestamp? = null,
    val ratingNum: Int = 0,
    val rating: Int = 0
)