// utils/MusicPlayer.kt
package com.example.carcollection.utils


import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import kotlin.random.Random
import android.media.MediaMetadataRetriever
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore // Import preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "music_preferences")


object MusicPreferences {
    private val MUSIC_PLAYING = booleanPreferencesKey("music_playing")

    fun isPlayingFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs -> // Now context.dataStore should resolve
            prefs[MUSIC_PLAYING] ?: true
        }

    suspend fun setIsPlaying(context: Context, playing: Boolean) {
        context.dataStore.edit { prefs -> // And here as well
            prefs[MUSIC_PLAYING] = playing
        }
    }
}

data class SongMetadata(
    val title: String?,
    val album: String?,
    val artist: String?,
    val albumArt: ByteArray?
)

fun getSongMetadata(context: Context, assetFileName: String): SongMetadata {
    val retriever = MediaMetadataRetriever()
    val afd = context.assets.openFd("songs/$assetFileName")
    retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)

    val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
    val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
    val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
    val art = retriever.embeddedPicture

    retriever.release()

    return SongMetadata(
        title = title,
        album = album,
        artist = artist,
        albumArt = art
    )
}

object MusicPlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var songs: MutableList<String> = mutableListOf()
    private var currentIndex = 0

    fun initialize(context: Context, songList: List<String>) {
        songs = songList.toMutableList()
        currentIndex = 0
        playSong(context)
    }

    private fun playSong(context: Context) {
        stop()
        if (songs.isNotEmpty()) {
            val afd: AssetFileDescriptor = context.assets.openFd("songs/${songs[currentIndex]}")
            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                isLooping = false
                prepare()
                start()
                setOnCompletionListener {
                    currentIndex = (currentIndex + 1) % songs.size
                    playSong(context)
                }
            }
        }
    }

    fun currentSongFile(): String {
        return songs.getOrNull(currentIndex) ?: ""
    }


    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun toggle(context: Context) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        } else {
            mediaPlayer?.start()
        }
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }

    fun next(context: Context) {
        if (songs.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % songs.size
            playSong(context)
        }
    }

    fun shuffle(context: Context) {
        if (songs.isNotEmpty()) {
            songs.shuffle(Random(System.currentTimeMillis()))
            currentIndex = 0
            playSong(context)
        }
    }
    fun currentSongName(): String {
        return songs.getOrNull(currentIndex)?.removeSuffix(".mp3") ?: ""
    }
    fun playPrevious(context: Context) {
        if (songs.isNotEmpty()) {
            currentIndex = if (currentIndex - 1 < 0) songs.size - 1 else currentIndex - 1
            playSong(context)
        }
    }


}
