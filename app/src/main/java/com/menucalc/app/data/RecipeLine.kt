package com.menucalc.app.data

/**
 * Vue d'une ligne de recette enrichie des infos de l'ingrédient, telle que
 * renvoyée par la jointure du DAO. Le coût de la ligne vaut
 * `quantity * pricePerUnit`.
 */
data class RecipeLine(
    val itemId: Long,
    val ingredientId: Long,
    val ingredientName: String,
    val unit: String,
    val pricePerUnit: Double,
    val quantity: Double,
) {
    val lineCost: Double get() = quantity * pricePerUnit
}
