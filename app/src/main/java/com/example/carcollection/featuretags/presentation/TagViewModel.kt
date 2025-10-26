package com.example.carcollection.featuretags.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carcollection.featuretags.data.TagsMethods
import com.example.carcollection.featuretags.domain.Tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TagViewModel(
    private val tagsMethods: TagsMethods
) : ViewModel() {

    private val _tagState = mutableStateOf(TagUiState())
    val tagState: State<TagUiState> = _tagState

    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags = _tags.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _uiMessage = MutableSharedFlow<String>()
    val uiMessage = _uiMessage.asSharedFlow()

    init {
        // Carga inicial de tags
        viewModelScope.launch {
            loadTagsOnce()
        }
    }

    private suspend fun loadTagsOnce() {
        _isLoading.value = true
        try {
            val result = tagsMethods.getAllTags()
            _tags.value = result
        } catch (e: Exception) {
            _uiMessage.emit("Error al cargar etiquetas: ${e.message}")
        } finally {
            _isLoading.value = false
        }
    }

    fun onEvent(event: TagsEvent) {
        when (event) {
            is TagsEvent.OnNameChanged -> _tagState.value = _tagState.value.copy(name = event.value)
            is TagsEvent.OnColorChanged -> _tagState.value = _tagState.value.copy(color = event.value)
            is TagsEvent.OnSaveClicked -> saveTag()
            is TagsEvent.OnEditClicked -> loadTag(event.id)
            is TagsEvent.OnDelete -> deleteTag(event.tag)
        }
    }

    private fun saveTag() {
        viewModelScope.launch {
            if (_tagState.value.name.isBlank()) {
                _uiMessage.emit("El nombre no puede estar vacío")
                return@launch
            }
            _isLoading.value = true
            try {
                tagsMethods.addTag(_tagState.value.name, _tagState.value.color)
                refreshTags()
                _uiMessage.emit("Etiqueta guardada")
                resetTagState()
            } catch (e: Exception) {
                _uiMessage.emit("Error al guardar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun editTag() {
        viewModelScope.launch {
            val tagId = _tagState.value.id ?: return@launch
            _isLoading.value = true
            try {
                tagsMethods.editTag(tagId, _tagState.value.name, _tagState.value.color)
                _tagState.value.originalName?.let { oldName ->
                    tagsMethods.updateTagNameInAllCars(oldName, _tagState.value.name)
                }
                refreshTags()
                _uiMessage.emit("Etiqueta actualizada")
                resetTagState()
            } catch (e: Exception) {
                _uiMessage.emit("Error al editar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                tagsMethods.deleteTag(tag.id ?: return@launch)
                refreshTags()
                _uiMessage.emit("Etiqueta eliminada")
            } catch (e: Exception) {
                _uiMessage.emit("Error al eliminar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    internal fun loadTag(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tag = tagsMethods.getTagById(id)
                tag?.let {
                    _tagState.value = TagUiState(
                        id = it.id,
                        name = it.name,
                        color = it.color?:"#FFFFFF",
                        originalName = it.name
                    )
                }
            } catch (e: Exception) {
                _uiMessage.emit("Error al cargar: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun refreshTags() {
        try {
            val result = tagsMethods.getAllTags()
            _tags.value = result
        } catch (e: Exception) {
            _uiMessage.emit("Error al refrescar etiquetas: ${e.message}")
        }
    }

    private fun resetTagState() {
        _tagState.value = TagUiState()
    }

    fun saveEdit(onComplete: () -> Unit) {
        viewModelScope.launch {
            editTag() // ya maneja ID internamente
            onComplete()
        }
    }
}