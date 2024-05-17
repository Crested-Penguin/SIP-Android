package com.crestedpenguin.sip.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SupplementViewModel : ViewModel() {
    var showBottomSheet: Boolean? by mutableStateOf(false)
    var supplementDocument: DocumentSnapshot? by mutableStateOf(null)

    companion object {
        private var isWPC : Boolean by mutableStateOf(value = false)
        private var isWPI : Boolean by mutableStateOf(value = false)
        private var isWPH : Boolean by mutableStateOf(value = false)

        fun getWP(propertyName: String): Boolean {
            return when (propertyName) {
                "isWPC" -> isWPC
                "isWPI" -> isWPI
                "isWPH" -> isWPH
                else -> false
            }
        }

        fun setWP(propertyName: String) {
            when (propertyName) {
                "isWPC" -> isWPC = !isWPC
                "isWPI" -> isWPI = !isWPI
                "isWPH" -> isWPH = !isWPH
            }
        }
    }
}