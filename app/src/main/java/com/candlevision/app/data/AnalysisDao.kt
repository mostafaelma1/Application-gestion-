package com.candlevision.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AnalysisDao {

    @Insert
    suspend fun insert(analysis: Analysis): Long

    /** Most recent analyses first — used by the home screen list. */
    @Query("SELECT * FROM analyses ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<Analysis>

    @Query("SELECT * FROM analyses WHERE id = :id")
    suspend fun byId(id: Long): Analysis?
}
