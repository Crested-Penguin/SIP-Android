// CompanyViewModel.kt
package com.crestedpenguin.sip.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

class CompanyViewModel : ViewModel() {
    var companyDocument: DocumentSnapshot? = null
    var storageRef: StorageReference? = null // Adjusted to correct type

    suspend fun getProductsByCompany(companyName: String): List<DocumentSnapshot> {
        return try {
            val result = FirebaseFirestore.getInstance()
                .collection("supplements")
                .whereEqualTo("company", companyName)
                .get()
                .await()
            result.documents
        } catch (e: Exception) {
            emptyList()
        }
    }
}
