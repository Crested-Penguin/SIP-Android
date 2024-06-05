package com.crestedpenguin.sip.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crestedpenguin.sip.model.CompanyImage
import com.crestedpenguin.sip.ui.CompanyViewModel

@Composable
fun CompanyScreen(companyViewModel: CompanyViewModel) {
    val company = companyViewModel.companyDocument

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val companyName = company?.getString("name") ?: "Empty"

        companyViewModel.storageRef?.let { CompanyImage(companyName, it) }
        Text(
            text = "Brand ${company?.getString("name")}",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "${company?.getString("description")}",
            fontSize = 20.sp
        )
    }
}