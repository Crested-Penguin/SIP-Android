package com.crestedpenguin.sip.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crestedpenguin.sip.ui.SupplementViewModel

@Composable
fun SupplementScreen(supplementViewModel: SupplementViewModel) {
    val supplement = supplementViewModel.supplementDocument

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Supplement ${supplement?.getString("company")}",
            fontSize = 20.sp
        )
    }
}