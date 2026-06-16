package com.menucalc.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Association d'un [Dish] à un [MenuEntity] (un plat dans un menu). */
@Entity(
    tableName = "menu_dishes",
    foreignKeys = [
        ForeignKey(
            entity = MenuEntity::class,
            parentColumns = ["id"],
            childColumns = ["menuId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Dish::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("menuId"), Index("dishId")],
)
data class MenuDish(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val menuId: Long,
    val dishId: Long,
)
