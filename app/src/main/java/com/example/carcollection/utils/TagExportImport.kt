package com.example.carcollection.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.example.carcollection.data.local.Tag
import com.example.carcollection.data.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Serializable
private data class TagDTO(val id: Int, val name: String, val color: String)

/*---------------------------------------*
 *  EXPORTAR  TAGS  →  JSON  en un Uri    *
 *---------------------------------------*/
suspend fun exportTagsToUri(
    context: Context,
    tagRepository: TagRepository,
    targetUri: Uri
) = withContext(Dispatchers.IO) {
    val tags: List<Tag> = tagRepository.getAllTags()     // suspending DAO call
    val dtoList = tags.map { TagDTO(it.id, it.name, it.color) }
    val json = Json { prettyPrint = true }.encodeToString(dtoList)

    context.contentResolver.openOutputStream(targetUri)?.use { output ->
        OutputStreamWriter(output).use { writer ->
            writer.write(json)
            writer.flush()
        }
    }
}

/*---------------------------------------*
 *  IMPORTAR  TAGS  ←  JSON  desde un Uri *
 *---------------------------------------*/
suspend fun importTagsFromUri(
    context: Context,
    tagRepository: TagRepository,
    sourceUri: Uri
) = withContext(Dispatchers.IO) {
    val json = buildString {
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BufferedReader(InputStreamReader(input)).forEachLine { appendLine(it) }
        }
    }
    if (json.isBlank()) return@withContext

    val dtoList: List<TagDTO> = Json.decodeFromString(json)

    // Estrategia: update si existe (por nombre), insert si no existe.
    val existing = tagRepository.getAllTags()

    dtoList.forEach { dto ->
        val current = existing.find { it.name.equals(dto.name, ignoreCase = true) }
        if (current == null) {
            tagRepository.addTag(Tag(name = dto.name, color = dto.color))
        } else {
            tagRepository.updateTag(current.copy(color = dto.color))
        }
    }
}