package com.menucalc.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AppDao {

    // ---------- Ingrédients ----------

    @Insert
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Update
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    @Query("SELECT * FROM ingredients ORDER BY name COLLATE NOCASE ASC")
    suspend fun allIngredients(): List<Ingredient>

    // ---------- Plats ----------

    @Insert
    suspend fun insertDish(dish: Dish): Long

    @Update
    suspend fun updateDish(dish: Dish)

    @Delete
    suspend fun deleteDish(dish: Dish)

    @Query("SELECT * FROM dishes ORDER BY name COLLATE NOCASE ASC")
    suspend fun allDishes(): List<Dish>

    @Query("SELECT * FROM dishes WHERE id = :id")
    suspend fun dishById(id: Long): Dish?

    /** Coût matière total de la recette d'un plat (toutes portions confondues). */
    @Query(
        """
        SELECT COALESCE(SUM(ri.quantity * i.pricePerUnit), 0)
        FROM recipe_items ri
        JOIN ingredients i ON i.id = ri.ingredientId
        WHERE ri.dishId = :dishId
        """
    )
    suspend fun dishCost(dishId: Long): Double

    // ---------- Lignes de recette ----------

    @Insert
    suspend fun insertRecipeItem(item: RecipeItem): Long

    @Update
    suspend fun updateRecipeItem(item: RecipeItem)

    @Query("DELETE FROM recipe_items WHERE id = :itemId")
    suspend fun deleteRecipeItem(itemId: Long)

    @Query(
        """
        SELECT ri.id AS itemId, i.id AS ingredientId, i.name AS ingredientName,
               i.unit AS unit, i.pricePerUnit AS pricePerUnit, ri.quantity AS quantity
        FROM recipe_items ri
        JOIN ingredients i ON i.id = ri.ingredientId
        WHERE ri.dishId = :dishId
        ORDER BY i.name COLLATE NOCASE ASC
        """
    )
    suspend fun recipeLines(dishId: Long): List<RecipeLine>

    // ---------- Menus ----------

    @Insert
    suspend fun insertMenu(menu: MenuEntity): Long

    @Update
    suspend fun updateMenu(menu: MenuEntity)

    @Delete
    suspend fun deleteMenu(menu: MenuEntity)

    @Query("SELECT * FROM menus ORDER BY createdAt DESC")
    suspend fun allMenus(): List<MenuEntity>

    @Query("SELECT * FROM menus WHERE id = :id")
    suspend fun menuById(id: Long): MenuEntity?

    // ---------- Plats d'un menu ----------

    @Insert
    suspend fun insertMenuDish(menuDish: MenuDish): Long

    @Query("DELETE FROM menu_dishes WHERE id = :menuDishId")
    suspend fun deleteMenuDish(menuDishId: Long)

    @Query(
        """
        SELECT md.id AS menuDishId, d.id AS dishId, d.name AS dishName, d.portions AS portions
        FROM menu_dishes md
        JOIN dishes d ON d.id = md.dishId
        WHERE md.menuId = :menuId
        ORDER BY d.name COLLATE NOCASE ASC
        """
    )
    suspend fun menuDishRows(menuId: Long): List<MenuDishRow>
}
