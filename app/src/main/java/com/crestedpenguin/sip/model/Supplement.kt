package com.crestedpenguin.sip.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Supplement(
    val name: String = "",
    val company: String = "",
    val price: Double = 0.0,
    val weight: Double = 0.0,
    val avrRating: Double = 0.toDouble(),
    val supType: String = "",
    val flavor: String = "",
    val servingSize: Double = 0.0,// 무게
    val servSizeProtein: Double = 0.0,// 무게당 프로틴
    val pricePerProteinWeight: Double = 0.toDouble() // 프로틴 20g당 가격
) {
    companion object {
        const val FIELD_NAME = "name"
        const val FIELD_COMPANY = "company"
        const val FIELD_PRICE = "price"
        const val WEIGHT = "weight"
        const val FIELD_AVG_RATING = "avrRating"
        const val FIELD_SUP_TYPE = "supType"
        const val FIELD_FLAVOR = "flavor"
        const val FIELD_SERVING_SIZE = "servingSize"
        const val FIELD_SERV_SIZE_PROTEIN = "servSizeProtein"
        const val FIELD_PRICE_PER_PROTEIN_WEIGHT = "pricePerProteinWeight"
    }
}
