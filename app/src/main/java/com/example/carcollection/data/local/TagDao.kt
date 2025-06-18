package com.example.carcollection.data.local
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao{
    @Query("SELECT * FROM tags")
    suspend fun getAllTags(): List<Tag>

    @Query("SELECT * FROM tags")
    fun getAllTagsFlow(): Flow<List<Tag>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Int): Tag?
    @Query("DELETE FROM tags")
    suspend fun deleteAllTags()

    @Query("SELECT EXISTS(SELECT 1 FROM tags WHERE name = :tagName)")
    fun containsTag(tagName: String): Flow<Boolean>




}