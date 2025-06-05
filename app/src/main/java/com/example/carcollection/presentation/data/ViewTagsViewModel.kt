package com.example.carcollection.presentation.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.data.local.Tag
import com.example.carcollection.data.repository.TagRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewTagsViewModel(private val repository: TagRepository) : ViewModel() {

    val tags: StateFlow<List<Tag>> = repository.getAllTagsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            repository.removeTag(tag)
        }
    }
}
