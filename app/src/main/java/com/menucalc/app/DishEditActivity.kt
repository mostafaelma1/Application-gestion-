package com.menucalc.app

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.menucalc.app.data.AppDao
import com.menucalc.app.data.Dish
import com.menucalc.app.data.Ingredient
import com.menucalc.app.data.RecipeItem
import com.menucalc.app.databinding.ActivityDishEditBinding
import com.menucalc.app.databinding.DialogRecipeLineBinding
import com.menucalc.app.ui.RecipeAdapter
import com.menucalc.app.util.Money
import kotlinx.coroutines.launch

class DishEditActivity : AppCompatActivity() {

    private lateinit var b: ActivityDishEditBinding
    private lateinit var dao: AppDao
    private lateinit var adapter: RecipeAdapter
    private lateinit var currency: String

    private var dishId: Long = 0L
    private var portions: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDishEditBinding.inflate(layoutInflater)
        setContentView(b.root)

        val app = application as MenuCalcApp
        dao = app.database.dao()
        currency = app.prefs.currency
        dishId = intent.getLongExtra(EXTRA_DISH_ID, 0L)

        b.header.headerTitle.setText(if (dishId == 0L) R.string.dish_new else R.string.dish_edit)
        b.header.headerBack.setOnClickListener { finish() }

        adapter = RecipeAdapter(currency) { line ->
            lifecycleScope.launch {
                dao.deleteRecipeItem(line.itemId)
                refreshRecipe()
            }
        }
        b.recyclerRecipe.layoutManager = LinearLayoutManager(this)
        b.recyclerRecipe.adapter = adapter

        b.btnSaveDish.setOnClickListener { saveDish() }
        b.btnAddIngredient.setOnClickListener { addIngredientLine() }

        if (dishId != 0L) loadDish()
    }

    private fun loadDish() = lifecycleScope.launch {
        val dish = dao.dishById(dishId) ?: return@launch
        portions = dish.portions
        b.editName.setText(dish.name)
        b.editPortions.setText(dish.portions.toString())
        refreshRecipe()
    }

    private fun saveDish() {
        val name = b.editName.text.toString().trim()
        val portionsInput = b.editPortions.text.toString().toIntOrNull() ?: 0
        when {
            name.isEmpty() -> toast(R.string.error_name_required)
            portionsInput < 1 -> toast(R.string.error_portions_invalid)
            else -> lifecycleScope.launch {
                portions = portionsInput
                if (dishId == 0L) {
                    dishId = dao.insertDish(Dish(name = name, portions = portionsInput))
                    b.header.headerTitle.setText(R.string.dish_edit)
                } else {
                    dao.updateDish(Dish(id = dishId, name = name, portions = portionsInput))
                }
                toast(R.string.settings_saved)
                refreshRecipe()
            }
        }
    }

    private fun addIngredientLine() {
        if (dishId == 0L) {
            Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val ingredients = dao.allIngredients()
            if (ingredients.isEmpty()) {
                toast(R.string.no_ingredients_yet)
                return@launch
            }
            showLineDialog(ingredients)
        }
    }

    private fun showLineDialog(ingredients: List<Ingredient>) {
        val dialogBinding = DialogRecipeLineBinding.inflate(layoutInflater)
        val labels = ingredients.map { "${it.name} (${it.unit})" }
        dialogBinding.spinnerIngredient.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, labels
        )

        AlertDialog.Builder(this)
            .setTitle(R.string.pick_ingredient)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.add, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val pos = dialogBinding.spinnerIngredient.selectedItemPosition
                        val qty = dialogBinding.editQuantity.text.toString()
                            .replace(',', '.').toDoubleOrNull()
                        if (qty == null || qty <= 0) {
                            toast(R.string.error_quantity_invalid)
                        } else {
                            val ingredient = ingredients[pos]
                            lifecycleScope.launch {
                                dao.insertRecipeItem(
                                    RecipeItem(
                                        dishId = dishId,
                                        ingredientId = ingredient.id,
                                        quantity = qty,
                                    )
                                )
                                refreshRecipe()
                            }
                            dismiss()
                        }
                    }
                }
            }
            .show()
    }

    private fun refreshRecipe() = lifecycleScope.launch {
        val lines = dao.recipeLines(dishId)
        adapter.submit(lines)
        b.recipeEmpty.visibility = if (lines.isEmpty()) View.VISIBLE else View.GONE

        val total = lines.sumOf { it.lineCost }
        val perPortion = if (portions > 0) total / portions else 0.0
        b.txtTotalCost.text = Money.format(total, currency)
        b.txtPerPortion.text = Money.format(perPortion, currency)
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_DISH_ID = "dish_id"
    }
}
