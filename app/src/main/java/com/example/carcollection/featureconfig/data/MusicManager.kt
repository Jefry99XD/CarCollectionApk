package com.example.carcollection.featureconfig.data

import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension para crear DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "music_settings")

class MusicManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")

    // Flow para observar el estado de la música
    val isMusicEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MUSIC_ENABLED_KEY] ?: false // Por defecto apagada
        }

    /**
     * Inicializar el MediaPlayer
     */
    private fun initMediaPlayer() {
        if (mediaPlayer == null) {
            try {
                // Crear MediaPlayer desde archivo en assets
                val afd = context.assets.openFd("maintheme.mp3")
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true // Loop infinito
                    setVolume(0.3f, 0.3f) // Volumen al 30% para no ser intrusivo
                    prepare()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Iniciar la música
     */
    fun startMusic() {
        initMediaPlayer()
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    /**
     * Pausar la música
     */
    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    /**
     * Liberar recursos del MediaPlayer
     */
    fun release() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    /**
     * Guardar preferencia de música
     */
    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }

        // Aplicar cambio inmediatamente
        if (enabled) {
            startMusic()
        } else {
            pauseMusic()
        }
    }

    /**
     * Verificar si está reproduciendo
     */
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
}

