package com.example.carcollection.presentation.data

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Tag
import com.example.carcollection.data.repository.CarRepository
import com.example.carcollection.data.repository.TagRepository
import kotlinx.coroutines.launch

class AddEditTagViewModel(private val repository: TagRepository, private val carRepository: CarRepository) : ViewModel() {
    var tagId: Int? = null
    var tagName = mutableStateOf("")
    val tagColor = mutableStateOf("#F44336")
    private var originalTagName: String? = null

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
    fun loadTag(tagId: Int) {
        viewModelScope.launch {
            val tag = repository.getTagById(tagId)
            if (tag != null) {
                this@AddEditTagViewModel.tagId = tag.id
                tagName.value = tag.name
                tagColor.value = tag.color
                originalTagName = tag.name
            }
        }
    }
    fun editTag(onEdited: () -> Unit) {
        viewModelScope.launch {
            tagId?.let { id ->
                val updatedTag = Tag(id = id, name = tagName.value, color = tagColor.value)
                repository.updateTag(updatedTag)
                originalTagName?.let { oldName ->
                    carRepository.updateTagNameInAllCars(oldName, tagName.value)
                }
                onEdited()
            }
        }
    }


}
