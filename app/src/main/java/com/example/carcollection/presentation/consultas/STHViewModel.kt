package com.example.carcollection.presentation.consultas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader

class STHViewModel(application: Application) : AndroidViewModel(application) {

    private val _sthEntries = MutableStateFlow<List<STHEntry>>(emptyList())
    val sthEntries: StateFlow<List<STHEntry>> = _sthEntries

    init {
        loadSTHFromAssets()
    }

    private fun loadSTHFromAssets() {
        viewModelScope.launch {
            try {
                val assetManager = getApplication<Application>().assets
                val inputStream = assetManager.open("sth.json")
                val reader = InputStreamReader(inputStream)

                val type = object : TypeToken<List<STHEntry>>() {}.type
                val entries: List<STHEntry> = Gson().fromJson(reader, type)

                _sthEntries.value = entries

                reader.close()
                inputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
