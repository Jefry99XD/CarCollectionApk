package com.example.carcollection.presentation.data

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Tag
import com.example.carcollection.data.repository.TagRepository
import kotlinx.coroutines.launch

class AddEditTagViewModel(private val repository: TagRepository) : ViewModel() {
    var tagName = mutableStateOf("")
    val tagColor = mutableStateOf("#F44336")

    fun onTagNameChange(value: String) {
        tagName.value = value
    }
    fun onTagColorChange(value: String) {
        tagColor.value = value
    }

    fun saveTag(onSaved: () -> Unit) {
        viewModelScope.launch {
            if (tagName.value.isNotBlank()) {
                repository.addTag(Tag(name = tagName.value, color = tagColor.value))
                onSaved()
            }
        }
    }
}
