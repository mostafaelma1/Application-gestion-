package com.menucalc.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Une ligne de recette : une quantité d'un [Ingredient] dans un [Dish].
 * La quantité est exprimée dans l'unité de l'ingrédient (ex. 1.5 pour 1,5 kg).
 *
 * Supprimer le plat ou l'ingrédient supprime automatiquement la ligne.
 */
@Entity(
    tableName = "recipe_items",
    foreignKeys = [
        ForeignKey(
            entity = Dish::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("dishId"), Index("ingredientId")],
)
data class RecipeItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: Long,
    val ingredientId: Long,
    /** Quantité dans l'unité de l'ingrédient. */
    val quantity: Double,
)
