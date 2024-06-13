package com.crestedpenguin.sip.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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