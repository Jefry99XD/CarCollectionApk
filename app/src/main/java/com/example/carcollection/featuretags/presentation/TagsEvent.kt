package com.example.carcollection.featuretags.presentation

import com.example.carcollection.featuretags.domain.Tag

sealed class TagsEvent {
    data class OnNameChanged(val value: String) : TagsEvent()
    data class OnColorChanged(val value: String) : TagsEvent()
    object OnSaveClicked : TagsEvent()
    data class OnEditClicked(val id: String) : TagsEvent()
    data class OnDelete(val tag: Tag) : TagsEvent()
}