package com.crestedpenguin.sip.model

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

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
                Text(text = "리뷰 작성", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    label = { Text("의견 감사합니다") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Increase the height to ensure it is not cut off
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "평가해주세요", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                        Text("취소")
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
                        Text("등록하기")
                    }
                }
            }
        }
    }
}