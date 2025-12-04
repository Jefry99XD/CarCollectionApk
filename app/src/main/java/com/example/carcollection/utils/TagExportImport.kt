package com.example.carcollection.utils

import android.content.Context
import android.net.Uri
import com.example.carcollection.featuretags.domain.Tag
import com.example.carcollection.featuretags.data.TagsMethods
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

@Serializable
private data class TagDTO(val name: String, val color: String?)

private val jsonFormat = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

/*---------------------------------------*
 *  EXPORTAR  TAGS  →  JSON  en un Uri    *
 *---------------------------------------*/
suspend fun exportTagsToUri(
    context: Context,
    targetUri: Uri
) = withContext(Dispatchers.IO) {
    val tagsMethods = TagsMethods()
    val tags: List<Tag> = tagsMethods.getAllTags()
    val dtoList = tags.map { TagDTO(it.name, it.color) }
    val json = jsonFormat.encodeToString(dtoList)

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
    sourceUri: Uri
): Pair<Int, Int> = withContext(Dispatchers.IO) {
    val json = buildString {
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BufferedReader(InputStreamReader(input)).forEachLine { appendLine(it) }
        }
    }
    if (json.isBlank()) return@withContext Pair(0, 0)

    val dtoList: List<TagDTO> = jsonFormat.decodeFromString(json)
    val tagsMethods = TagsMethods()
    val existing = tagsMethods.getAllTags()

    var addedCount = 0
    var updatedCount = 0

    dtoList.forEach { dto ->
        val current = existing.find { it.name.equals(dto.name, ignoreCase = true) }
        if (current == null) {
            // Tag doesn't exist, add it
            tagsMethods.addTag(dto.name, dto.color ?: "")
            addedCount++
        } else {
            // Tag exists, update it if color is different
            if (current.color != dto.color) {
                tagsMethods.editTag(current.id ?: "", dto.name, dto.color ?: "")
                updatedCount++
            }
        }
    }

    Pair(addedCount, updatedCount)
}