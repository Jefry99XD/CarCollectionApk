package com.example.carcollection.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import kotlin.random.Random

private val Context.dataStore by preferencesDataStore(name = "music_preferences")

object MusicPreferences {
    private val MUSIC_PLAYING = booleanPreferencesKey("music_playing")

    fun isPlayingFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[MUSIC_PLAYING] ?: false
        }

    suspend fun setIsPlaying(context: Context, playing: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MUSIC_PLAYING] = playing
        }
    }
}

data class Song(
    val name: String,
    val url: String,
    val album: String,
    val albumCover: String
)

object MusicPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var songs: MutableList<Song> = mutableListOf()
    private var currentIndex = 0

    suspend fun initialize(context: Context) {
        val songList = loadSongsFromJson()
        if (songList.isNotEmpty()) {
            songs = songList.toMutableList()
            currentIndex = 0

            // 👇 Verificar DataStore antes de reproducir
            val shouldPlay = MusicPreferences.isPlayingFlow(context)
                .first() // suspende y obtiene el valor actual

            if (shouldPlay) {
                playCurrent()
            } else {
                Log.d("MusicPlayer", "Music is disabled by user preference")
            }
        }
    }



    private fun playCurrent() {
        stop() // Release previous player first
        if (songs.isNotEmpty()) {
            val songToPlay = songs[currentIndex]
            mediaPlayer = MediaPlayer().apply {
                try {
                    Log.d("MusicPlayer", "Attempting to play URL: ${songToPlay.url}")
                    setDataSource(songToPlay.url)
                    setOnPreparedListener { mp ->
                        mp.start()
                        // Update UI or state to indicate playback has started
                    }
                    setOnErrorListener { _, what, extra ->
                        // Handle errors during preparation or playback
                        // Log the error, inform the user, try next song, etc.
                        Log.e("MusicPlayer", "MediaPlayer Error: what=$what, extra=$extra for ${songToPlay.name}")
                        stop() // Release the failed player
                        // Optionally, try playing the next song or show an error message
                        // next() // Be careful of infinite loops if all songs are bad
                        true // Indicate that the error has been handled
                    }
                    setOnCompletionListener {
                        next()
                    }
                    prepareAsync() // Prepare asynchronously
                } catch (e: Exception) {
                    Log.e("MusicPlayer", "Error setting data source or preparing media player for ${songToPlay.name}", e)
                    stop() // Ensure media player is released on exception
                    // Optionally, try playing the next song
                }
            }
        }
    }


    fun next() {
        if (songs.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % songs.size
            playCurrent()
        }
    }

    fun playPrevious() {
        if (songs.isNotEmpty()) {
            currentIndex = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
            playCurrent()
        }
    }

    fun shuffle() {
        if (songs.isNotEmpty()) {
            songs.shuffle(Random(System.currentTimeMillis()))
            currentIndex = 0
            playCurrent()
        }
    }

    fun toggle() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause() else it.start()
        }
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun currentSongName(): String {
        return songs.getOrNull(currentIndex)?.name ?: "Unknown Song"
    }
    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    /** Carga canciones desde JSON en assets */
    private suspend fun loadSongsFromJson(dispatcher: CoroutineDispatcher = Dispatchers.IO): List<Song> =
        withContext(dispatcher) {
            val list = mutableListOf<Song>()
            try {
                val url = URL("https://raw.githubusercontent.com/Jefry99XD/CarCollectionApk/main/app/src/main/assets/songs.json")
                val json = url.readText()

                val jsonArray = JSONArray(json)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val urlSong = obj.getString("url")
                    val album = obj.optString("album", "")
                    val albumCover = obj.optString("albumCover", "")
                    if (urlSong.endsWith(".mp3")) {
                        list.add(Song(name, urlSong, album, albumCover))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            list
        }

    fun currentSong(): Song? {
        return songs.getOrNull(currentIndex)
    }


}
