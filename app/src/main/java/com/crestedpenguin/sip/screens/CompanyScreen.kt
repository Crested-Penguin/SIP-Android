package com.crestedpenguin.sip.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.crestedpenguin.sip.model.CompanyImage
import com.crestedpenguin.sip.ui.CompanyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(companyViewModel: CompanyViewModel, navController: NavController) {
    val company = companyViewModel.companyDocument

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAEC)), // 배경색을 베이지로 설정
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val companyName = company?.getString("name") ?: "Empty"

        TopAppBar(
            title = { Text(text = companyName, fontSize = 16.sp) }, modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFAEC)),
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Backward")} }
        )

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