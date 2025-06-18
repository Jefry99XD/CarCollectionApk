package com.example.carcollection.presentation.consultas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class THViewModel(application: Application) : AndroidViewModel(application) {

    private val _thEntries = MutableStateFlow<List<STHEntry>>(emptyList())
    val thEntries: StateFlow<List<STHEntry>> = _thEntries

    init {
        loadTHEntries()
    }

    private fun loadTHEntries() {
        viewModelScope.launch {
            try {
                val inputStream = getApplication<Application>().assets.open("th.json")
                val json = inputStream.bufferedReader().use { it.readText() }

                val gson = Gson()
                val type = object : TypeToken<List<STHEntry>>() {}.type
                val entries: List<STHEntry> = gson.fromJson(json, type)

                _thEntries.value = entries
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}