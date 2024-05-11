package com.crestedpenguin.sip.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchScreen() {

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxSize()
    ) {
        item {
            SupplementItem("Example1")
            SupplementItem("Example2")
            SupplementItem("Example3")
            SupplementItem("Example4")
            SupplementItem("Example5")
        }
    }
}

@Composable
fun SupplementItem(product: String) {
    Card(modifier = Modifier
        .padding(4.dp)
        .fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SupplementImage()
            Text(
                text = "Supplement $product",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun SupplementImage() {
}