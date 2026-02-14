package com.example.carcollection.featureWishlist.domain

import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@Serializable
@IgnoreExtraProperties
data class WishlistItem(
    val id: String = "",
    val userId: String = "",
    val carName: String = "",
    val brand: String = "",
    val serie: String = "",
    val imageUrl: String = "",
    val priority: String = "Media", // Baja, Media, Alta, Urgente
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "", "Media", "", System.currentTimeMillis())
}

