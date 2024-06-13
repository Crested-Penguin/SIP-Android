// CompanyScreen.kt
package com.crestedpenguin.sip.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.crestedpenguin.sip.model.CompanyImage
import com.crestedpenguin.sip.model.Supplement
import com.crestedpenguin.sip.model.SupplementImage
import com.crestedpenguin.sip.ui.CompanyViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(navController: NavController, companyViewModel: CompanyViewModel, storageRef: StorageReference) {
    val company = companyViewModel.companyDocument
    var products by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }
    val companyName = company?.getString("name") ?: "Empty"
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val scrollOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }
    val showDescription = remember { derivedStateOf { scrollOffset.value == 0 } }

    LaunchedEffect(companyName) {
        coroutineScope.launch {
            products = companyViewModel.getProductsByCompany(companyName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAEC)), // 배경색을 베이지로 설정
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text(text = companyName, fontSize = 16.sp) }, modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFAEC)),
            navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Backward")} }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFFFFAEC)) // 이미지 뒤의 배경색을 베이지로 설정
                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        ) {
            CompanyImage(companyName, storageRef)
        }

        //Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = showDescription.value,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
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

                    // 회사 설명 표시
                    Text(
                        text = company?.getString("description") ?: "No description available",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Products",
            fontSize = 20.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            items(products) { productDocument ->
                val product = productDocument.toObject(Supplement::class.java)
                product?.let {
                    ProductItem(navController, productDocument, it, storageRef)
                }
            }
        }
    }
}

@Composable
fun ProductItem(navController: NavController, productDocument: DocumentSnapshot, supplement: Supplement, storageRef: StorageReference) {
    Card(
        onClick = {
            //
        },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                val supplementName = supplement.name
                SupplementImage(supplementName, storageRef)
            }
            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(16.dp)
            ) {
                Text(
                    text = supplement.name,
                    fontSize = 20.sp
                )
                Text(
                    text = "회사: ${supplement.company}",
                    fontSize = 16.sp
                )
                Text(
                    text = "유형: ${supplement.supType}",
                    fontSize = 14.sp
                )
                Text(
                    text = "가격: ${supplement.price} 원",
                    fontSize = 14.sp
                )
                Text(
                    text = "단백질 20g당 가격: ${supplement.pricePerProteinWeight} 원",
                    fontSize = 14.sp
                )
                Text(
                    text = "평균 평점: %.2f".format(supplement.avrRating),
                    fontSize = 14.sp
                )
                Text(
                    text = "리뷰 수: ${supplement.reviewCount}",
                    fontSize = 14.sp
                )
            }
        }
    }
}