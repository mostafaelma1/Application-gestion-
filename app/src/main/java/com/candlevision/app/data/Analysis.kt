package com.candlevision.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One saved chart analysis. The image is copied into the app's internal
 * storage and referenced here by absolute path.
 */
@Entity(tableName = "analyses")
data class Analysis(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val imagePath: String,
    val direction: String,
    val trend: String,
    val risk: String,
    val analysis: String,
    val zones: String,
    val scenario: String,
    val advice: String,
    val language: String,
    val market: String,
)
