package com.example.carcollection.data.car

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties // Allows Firestore to ignore properties not present in the document
data class Car(
    // Firestore will auto-generate a document ID, so we don't need a specific 'id' field here
    // If you wanted to store the Firestore document ID inside the Car object AFTER creation,
    // you'd add a property like 'firestoreDocId: String? = null' and set it separately.
    val brand: String? = null,
    val name: String? = null,
    val serie: String? = null,
    val year: String? = null,
    val photoUrl: String? = null,
    val color: String? = null,
    val type: String? = null,
    val tags: List<String> = emptyList(), // Firestore handles List<String> as an array
    val backgroundName: String? = null
) {
    // No-argument constructor required by Firestore for deserialization
    constructor() : this(null, null, null, null, null, null, null, emptyList(), null)
}