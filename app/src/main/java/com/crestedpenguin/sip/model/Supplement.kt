package com.crestedpenguin.sip.model
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Supplement(
    val name: String = "",
    val company: String = "",
    val price: Double = 0.0,
    val weight: Double = 0.0,
    val avrRating: Double = 0.0,
    var reviewCount: Int = 0, // 리뷰 수
    val supType: String = "",
    val flavor: List<String> = emptyList(),
    val servingSize: Double = 0.0,
    val servSizeProtein: Double = 0.0,
    val pricePerProteinWeight: Double = 0.0,
    val description: String = "", // 추가된 필드
    val nutrient: String = "" // 추가된 필드
) {
    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_COMPANY = "company"
        const val FIELD_PRICE = "price"
        const val FIELD_WEIGHT = "weight"
        const val FIELD_AVG_RATING = "avrRating"
        const val FIELD_SUP_TYPE = "supType"
        const val FIELD_FLAVOR = "flavor"
        const val FIELD_SERVING_SIZE = "servingSize"
        const val FIELD_SERV_SIZE_PROTEIN = "servSizeProtein"
        const val FIELD_PRICE_PER_PROTEIN_WEIGHT = "pricePerProteinWeight"
    }

    suspend fun getReviewCount(documentId: String): Int {
        val db = FirebaseFirestore.getInstance()
        val comments = db.collection("supplements").document(documentId).collection("comments").get().await()
        return comments.size()
    }
}
