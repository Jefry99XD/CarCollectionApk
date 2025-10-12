package com.example.carcollection.data.user

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@Serializable
@IgnoreExtraProperties // Allows Firestore to ignore properties not present in the document
data class User(
    @get:Exclude // Don't store UID as a separate field in the document if it's the doc ID
    val uid: String = "", // The document ID will be the UID
    val username: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis() // Automatically set creation time
) {
    // No-argument constructor required by Firestore for deserialization
    constructor() : this("", null, null, null, 0)
}
