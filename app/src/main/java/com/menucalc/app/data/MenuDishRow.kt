package com.menucalc.app.data

/**
 * Une ligne de menu : le plat associé, accompagné de son id de liaison
 * (pour le retirer du menu) et du nombre de portions de sa recette.
 */
data class MenuDishRow(
    val menuDishId: Long,
    val dishId: Long,
    val dishName: String,
    val portions: Int,
)
