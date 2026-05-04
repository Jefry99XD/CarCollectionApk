package com.example.carcollection.featureconfig.data

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Extension para crear DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "music_settings")

class MusicManager private constructor(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val MUSIC_ENABLED_KEY = booleanPreferencesKey("music_enabled")
    private val MUSIC_VOLUME_KEY  = floatPreferencesKey("music_volume")

    // ─── Point 1: tracks loaded from companion-level cache (once per process) ──
    private val tracks: List<String> = getCachedTracks(context)

    // Playlist actual a reproducir (puede estar en orden o shuffleado)
    private var playlistQueue: MutableList<String> = tracks.toMutableList()
    private var currentIndex: Int = 0

    // Behavior flags (default to enabled so it "does loop and shuffle by itself")
    var shuffleEnabled: Boolean = true
    var loopEnabled: Boolean = true

    // ─── Point 4: current volume (updated from DataStore or direct calls) ───────
    @Volatile var currentVolume: Float = DEFAULT_VOLUME

    // Flow para observar el estado de la música
    val isMusicEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[MUSIC_ENABLED_KEY] ?: false
        }

    // Flow para observar el volumen guardado
    val musicVolume: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[MUSIC_VOLUME_KEY] ?: DEFAULT_VOLUME
        }

    init {
        if (shuffleEnabled) {
            playlistQueue.shuffle()
        }
    }

    // ─── Point 4: restore volume from DataStore (call before startMusic) ────────
    suspend fun restoreVolume() {
        currentVolume = musicVolume.first()
    }

    // ─── Point 4: save volume and apply immediately ──────────────────────────────
    suspend fun setMusicVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        context.dataStore.edit { preferences ->
            preferences[MUSIC_VOLUME_KEY] = clamped
        }
        currentVolume = clamped
        mediaPlayer?.setVolume(clamped, clamped)
    }

    private fun ensureMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer().apply {
                isLooping = false
                setOnCompletionListener { onTrackCompleted() }
                setOnPreparedListener  { mp -> mp.start() }
            }
        }
    }

    private fun onTrackCompleted() { playNext() }

    private fun playTrack(index: Int) {
        if (playlistQueue.isEmpty()) return
        val safeIndex = index.coerceIn(0, playlistQueue.size - 1)
        currentIndex = safeIndex

        ensureMediaPlayer()
        mediaPlayer?.reset()
        try {
            val assetPath = "music/${playlistQueue[currentIndex]}"
            val afd = context.assets.openFd(assetPath)
            mediaPlayer?.apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setVolume(currentVolume, currentVolume) // Point 4: use saved volume
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playNext() {
        if (playlistQueue.isEmpty()) return
        val nextIndex = currentIndex + 1
        if (nextIndex >= playlistQueue.size) {
            if (loopEnabled) {
                if (shuffleEnabled) playlistQueue.shuffle()
                playTrack(0)
            } else {
                pauseMusic()
            }
        } else {
            playTrack(nextIndex)
        }
    }

    fun startMusic() {
        if (playlistQueue.isEmpty()) return
        ensureMediaPlayer()
        if (mediaPlayer?.isPlaying == true) return
        try {
            playTrack(currentIndex.coerceIn(0, playlistQueue.size - 1))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pauseMusic() {
        mediaPlayer?.let { if (it.isPlaying) it.pause() }
    }

    /** Skip to the next track in the playlist. */
    fun skipToNext() {
        playNext()
    }

    /** Skip to the previous track in the playlist. */
    fun skipToPrevious() {
        if (playlistQueue.isEmpty()) return
        val prevIndex = if (currentIndex - 1 < 0) playlistQueue.size - 1 else currentIndex - 1
        playTrack(prevIndex)
    }

    /** Name of the track currently loaded (filename without extension). */
    fun currentTrackName(): String {
        val raw = playlistQueue.getOrNull(currentIndex) ?: return ""
        return raw.substringBeforeLast('.')
    }

    fun release() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.reset()
            it.release()
            mediaPlayer = null
        }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MUSIC_ENABLED_KEY] = enabled
        }
        if (enabled) startMusic() else pauseMusic()
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    // ─────────────────────────────────────────────────────────────────────────────
    // Point 1 + singleton: shared instance + companion-level tracks cache
    // ─────────────────────────────────────────────────────────────────────────────
    companion object {
        private const val DEFAULT_VOLUME = 0.3f

        /** Singleton instance — uses applicationContext so no memory leak */
        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: MusicManager? = null

        fun getInstance(context: Context): MusicManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicManager(context.applicationContext).also { INSTANCE = it }
            }

        /** Cached track list — assets.list("music") called once per process life */
        @Volatile private var cachedTrackList: List<String>? = null

        private fun getCachedTracks(context: Context): List<String> {
            cachedTrackList?.let { return it }
            return synchronized(this) {
                cachedTrackList ?: run {
                    val list = try {
                        context.assets.list("music")
                            ?.filter { it.isNotBlank() }
                            ?.toList() ?: emptyList()
                    } catch (e: Exception) { emptyList() }
                    cachedTrackList = list
                    list
                }
            }
        }
    }
}
