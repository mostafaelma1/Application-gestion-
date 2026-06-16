package com.menucalc.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Une matière première achetée à un prix donné par unité.
 * Exemple : "Pomme de terre", unité "kg", prix 8.0 DH/kg.
 */
@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Unité d'achat : kg, L, pièce, botte… */
    val unit: String,
    /** Prix d'achat pour une unité (DH par kg, par L…). */
    val pricePerUnit: Double,
)
