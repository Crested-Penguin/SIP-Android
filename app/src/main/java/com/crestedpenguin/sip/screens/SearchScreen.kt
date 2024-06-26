// SearchScreen.kt
package com.crestedpenguin.sip.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.crestedpenguin.sip.R
import com.crestedpenguin.sip.model.Supplement
import com.crestedpenguin.sip.model.SupplementImage
import com.crestedpenguin.sip.ui.SupplementViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "SearchScreen"

@Composable
fun SearchScreen(navController: NavController, supplementViewModel: SupplementViewModel, db: FirebaseFirestore, storageRef: StorageReference) {
    val coroutineScope = rememberCoroutineScope()
    var supplementList by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val filterOptions = listOf("리뷰 많은 순", "평점 높은 순", "평점 낮은 순", "가격 낮은 순", "단백질 20g당 가격 낮은 순")
    val proteinOptions = listOf("WPC", "WPI", "비건", "혼합")
    val flavorOptions = listOf("초콜릿", "딸기", "바닐라", "무첨가", "기타")
    var selectedProteinFilters = remember { mutableStateListOf(*BooleanArray(proteinOptions.size) { false }.toTypedArray()) }
    var selectedFlavorFilters = remember { mutableStateListOf(*BooleanArray(flavorOptions.size) { false }.toTypedArray()) }
    var selectedSortFilter by remember { mutableIntStateOf(-1) }
    val listState = rememberLazyListState()
    val scrollOffset = remember { derivedStateOf { listState.firstVisibleItemScrollOffset } }

    // 새로운 문서를 업데이트하는 함수
    suspend fun updateNewDocuments() {
        try {
            val result = db.collection("supplements").get().await()
            val supplements = result.documents

            supplements.forEach { document ->
                val name = document.getString("name") ?: ""
                val company = document.getString("company") ?: ""
                val supType = document.getString("supType") ?: ""

                // 필요한 필드를 업데이트
                val updates = hashMapOf<String, Any>(
                    "nameLower" to name.lowercase(),
                    "companyLower" to company.lowercase(),
                    "supTypeLower" to supType.lowercase()
                )

                db.collection("supplements").document(document.id).update(updates)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error updating document", e)
                    }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error updating documents.", e)
        }
    }

    // Fetch review count
    suspend fun getReviewCount(supplementId: String): Int {
        return try {
            val comments = db.collection("supplements").document(supplementId).collection("comments").get().await()
            comments.size()
        } catch (e: Exception) {
            Log.w(TAG, "Error fetching review count.", e)
            0
        }
    }

    // Firestore 데이터 불러오기 및 업데이트
    suspend fun fetchData() {
        try {
            // 새로운 문서를 확인하고 업데이트
            updateNewDocuments()

            val result = db.collection("supplements").get().await()
            val supplements = result.documents

            // 검색어 필터링
            var filteredResult = if (searchQuery.isNotEmpty()) {
                val searchQueryLower = searchQuery.lowercase()
                supplements.filter { document ->
                    document.getString("nameLower")?.contains(searchQueryLower) == true ||
                            document.getString("companyLower")?.contains(searchQueryLower) == true ||
                            document.getString("supTypeLower")?.contains(searchQueryLower) == true
                }
            } else {
                supplements
            }

            // 유형 필터링
            if (selectedProteinFilters.contains(true)) {
                filteredResult = filteredResult.filter { document ->
                    proteinOptions.filterIndexed { index, _ ->
                        selectedProteinFilters[index]
                    }.any {
                        document.getString("supType")?.contains(it, ignoreCase = true) == true
                    }
                }
            }

            // 맛 필터링
            if (selectedFlavorFilters.contains(true)) {
                filteredResult = filteredResult.filter { document ->
                    val flavorList = document.get("flavor") as? List<*> ?: emptyList<Any>()
                    val predefinedFlavors = listOf("초코", "초콜렛", "초콜릿", "딸기", "스트로베리", "바닐라", "바나나", "무첨가", "무맛")

                    val isSelected = flavorOptions.filterIndexed { index, flavorOption ->
                        selectedFlavorFilters[index] && when (flavorOption) {
                            "초콜릿" -> listOf("초코", "초콜렛", "초콜릿")
                            "딸기" -> listOf("딸기", "스트로베리")
                            "바닐라" -> listOf("바닐라", "바나나")
                            "무첨가" -> listOf("무첨가", "무맛")
                            else -> listOf() // 기타
                        }.any { flavor ->
                            flavorList.contains(flavor)
                        }
                    }.isNotEmpty()

                    val isOtherFlavor = selectedFlavorFilters[4] && flavorList.any { flavor ->
                        flavor !in predefinedFlavors
                    }

                    isSelected || isOtherFlavor
                }
            }


            // 정렬
            supplementList = when (selectedSortFilter) {
                0 -> filteredResult.sortedByDescending { it.getLong("reviewCount") }
                1 -> filteredResult.sortedByDescending { it.getDouble("avrRating") }
                2 -> filteredResult.sortedBy { it.getDouble("avrRating") }
                3 -> filteredResult.sortedBy { it.getDouble("price") }
                4 -> filteredResult.sortedBy { it.getDouble("pricePerProteinWeight") }
                else -> filteredResult
            }

            for (document in supplementList) {
                Log.d(TAG, document.toString())
            }
        } catch (e: Exception) {
            Log.w(TAG, "문서를 가져오는 중 오류 발생.", e)
        }
    }

    LaunchedEffect(searchQuery, selectedProteinFilters, selectedFlavorFilters, selectedSortFilter) {
        coroutineScope.launch {
            fetchData()
        }
    }

    val showFilters = remember { derivedStateOf { scrollOffset.value == 0 } }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            label = { Text(text = "검색") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search_24px),
                    contentDescription = "검색 아이콘"
                )
            }
        )

        AnimatedVisibility(
            visible = showFilters.value,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 0.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(text = "단백질 종류", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    proteinOptions.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {
                                    selectedProteinFilters[index] = !selectedProteinFilters[index]
                                    coroutineScope.launch { fetchData() } // 선택할 때마다 데이터 갱신
                                }
                        ) {
                            Checkbox(
                                checked = selectedProteinFilters[index],
                                onCheckedChange = {
                                    selectedProteinFilters[index] = it
                                    coroutineScope.launch { fetchData() } // 체크박스를 선택할 때마다 데이터 갱신
                                }
                            )
                            Text(text = option)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "맛 종류", fontSize = 16.sp, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    flavorOptions.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .clickable {
                                    selectedFlavorFilters[index] = !selectedFlavorFilters[index]
                                    coroutineScope.launch { fetchData() } // 선택할 때마다 데이터 갱신
                                }
                        ) {
                            Checkbox(
                                checked = selectedFlavorFilters[index],
                                onCheckedChange = {
                                    selectedFlavorFilters[index] = it
                                    coroutineScope.launch { fetchData() } // 체크박스를 선택할 때마다 데이터 갱신
                                }
                            )
                            Text(text = option)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = !dropdownExpanded },
                        border = BorderStroke(1.dp, Color.Black)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (selectedSortFilter == -1) "정렬 기준 선택" else filterOptions[selectedSortFilter],
                                color = Color.Black // 다른 글자색과 동일하게 설정
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "드롭다운 아이콘",
                                tint = Color.Black // 화살표 색상 변경
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        filterOptions.forEachIndexed { index, filter ->
                            DropdownMenuItem({
                                Text(text = filter, color = Color.Black) // 다른 글자색과 동일하게 설정
                            },
                                onClick = {
                                    selectedSortFilter = index
                                    dropdownExpanded = false
                                    coroutineScope.launch { fetchData() } // 정렬 기준을 선택할 때마다 데이터 갱신
                                }
                            )
                        }
                    }
                }

            }
        }

        if (supplementList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("검색 결과 없음", fontSize = 20.sp)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxSize()
            ) {
                itemsIndexed(supplementList) { _, document ->
                    SupplementItem(navController, supplementViewModel, document, storageRef)
                }
            }
        }
    }
}

@Composable
fun SupplementItem(
    navController: NavController,
    supplementViewModel: SupplementViewModel,
    supplement: DocumentSnapshot,
    storageRef: StorageReference
) {
    val supplementData = supplement.toObject(Supplement::class.java)
    Card(
        onClick = {
            supplementViewModel.supplementDocument = supplement
            navController.navigate("supplement")
        },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFEF6).copy(alpha = 1f) // 투명한 베이지 색
        ),
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        supplementData?.let {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    val supplementName = supplement.getString("name") ?: "Empty"
                    SupplementImage(supplementName, storageRef)
                }
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .padding(16.dp)
                ) {
                    Text(
                        text = it.name,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "회사: ${it.company}",
                        fontSize = 16.sp
                    )
                    Text(
                        text = "유형: ${it.supType}",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "가격: ${it.price} 원",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "단백질 20g당 가격: ${it.pricePerProteinWeight} 원",
                        fontSize = 14.sp
                    )
                    Text(
                        text = "평균 평점: %.2f".format(it.avrRating),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "리뷰 수: ${it.reviewCount}",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
//4