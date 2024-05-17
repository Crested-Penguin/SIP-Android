package com.crestedpenguin.sip

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.crestedpenguin.sip.ui.SupplementViewModel

@Composable
fun FilterChip(target: String) {
    FilterChip(
        selected = SupplementViewModel.getWP(target),
        onClick = { SupplementViewModel.setWP(target) },
        label = { Text(text = "WPC") },
        leadingIcon = if (SupplementViewModel.getWP(target)) {
            {
                Icon(
                    painter = painterResource(id = R.drawable.check_24px),
                    contentDescription = "Done Icon",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
    )
}