package com.crestedpenguin.sip.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot

class SupplementViewModel : ViewModel() {
    var supplementDocument: DocumentSnapshot? by mutableStateOf(null)
}