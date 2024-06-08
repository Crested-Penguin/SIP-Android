package com.crestedpenguin.sip.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crestedpenguin.sip.model.CompanyImage
import com.crestedpenguin.sip.ui.CompanyViewModel
import androidx.compose.foundation.background
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun CompanyScreen(companyViewModel: CompanyViewModel) {
    val company = companyViewModel.companyDocument

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAEC)), // 배경색을 베이지로 설정
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val companyName = company?.getString("name") ?: "Empty"

        TopBar(companyName)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFFFFFAEC)) // 이미지 뒤의 배경색을 베이지로 설정
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            companyViewModel.storageRef?.let { CompanyImage(companyName, it) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFFAEC).copy(alpha = 0.8f) // 홈스크린에서 사용한 투명한 베이지 색
            ),
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // 회사 설명 표시
                Text(
                    text = company?.getString("description") ?: "No description available",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun TopBar(companyName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFAEC)) // 배경색 설정
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = companyName,
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}
