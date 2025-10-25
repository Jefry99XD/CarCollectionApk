package com.example.carcollection.featuretags.domain

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import kotlinx.serialization.Serializable

@Serializable
@IgnoreExtraProperties
data class Tag(
    @get:Exclude
    var id: String? = null,
    val name: String = "",

    val color: String? = null
) {
    constructor() : this(id = null, name = "", color = null)
}