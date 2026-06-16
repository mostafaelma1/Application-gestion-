package com.menucalc.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un plat (fiche technique). La recette est donnée pour [portions] couverts ;
 * le coût d'une portion est obtenu en divisant le coût total des ingrédients
 * par ce nombre.
 */
@Entity(tableName = "dishes")
data class Dish(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Nombre de portions que produit la recette (au moins 1). */
    val portions: Int,
)
