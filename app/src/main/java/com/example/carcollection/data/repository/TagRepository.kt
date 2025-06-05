package com.example.carcollection.data.repository

import com.example.carcollection.data.local.Tag
import com.example.carcollection.data.local.TagDao
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {

    suspend fun addTag(tag: Tag) {
        tagDao.insertTag(tag)
    }
    suspend fun getAllTags(): List<Tag> {
        return tagDao.getAllTags()
    }
    fun getAllTagsFlow(): Flow<List<Tag>> {
        return tagDao.getAllTagsFlow()
    }


    suspend fun removeTag(tag: Tag) {
        tagDao.deleteTag(tag)
    }

    suspend fun clearTags() {
        tagDao.deleteAllTags()
    }


    suspend fun getTagById(id: Int): Tag? {
        return tagDao.getTagById(id)
    }

    fun containsTag(tagName: String): Flow<Boolean> {
        return tagDao.containsTag(tagName)
    }
}
