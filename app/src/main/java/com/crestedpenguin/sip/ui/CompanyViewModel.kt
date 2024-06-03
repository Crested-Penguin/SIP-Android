package com.crestedpenguin.sip.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.StorageReference

class CompanyViewModel : ViewModel() {
    var companyDocument: DocumentSnapshot? by mutableStateOf(null)
    var storageRef: StorageReference? by mutableStateOf(null)
}