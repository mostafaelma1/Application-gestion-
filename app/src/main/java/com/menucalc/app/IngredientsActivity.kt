package com.menucalc.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.menucalc.app.data.AppDao
import com.menucalc.app.data.Ingredient
import com.menucalc.app.databinding.ActivityListBinding
import com.menucalc.app.databinding.DialogIngredientBinding
import com.menucalc.app.ui.IngredientAdapter
import kotlinx.coroutines.launch

class IngredientsActivity : AppCompatActivity() {

    private lateinit var b: ActivityListBinding
    private lateinit var dao: AppDao
    private lateinit var adapter: IngredientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityListBinding.inflate(layoutInflater)
        setContentView(b.root)

        val app = application as MenuCalcApp
        dao = app.database.dao()

        b.header.headerTitle.setText(R.string.ingredients_title)
        b.header.headerBack.setOnClickListener { finish() }
        b.emptyState.setText(R.string.ingredients_empty)

        adapter = IngredientAdapter(
            currency = app.prefs.currency,
            onClick = { showDialog(it) },
            onDelete = { confirmDelete(it) },
        )
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter

        b.fab.setOnClickListener { showDialog(null) }

        load()
    }

    private fun load() = lifecycleScope.launch {
        val list = dao.allIngredients()
        adapter.submit(list)
        b.emptyState.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showDialog(existing: Ingredient?) {
        val dialogBinding = DialogIngredientBinding.inflate(layoutInflater)
        existing?.let {
            dialogBinding.editName.setText(it.name)
            dialogBinding.editUnit.setText(it.unit)
            dialogBinding.editPrice.setText(it.pricePerUnit.toString())
        }
        val titleRes = if (existing == null) R.string.ingredient_new else R.string.ingredient_edit

        AlertDialog.Builder(this)
            .setTitle(titleRes)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null)
            .create()
            .apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val name = dialogBinding.editName.text.toString().trim()
                        val unit = dialogBinding.editUnit.text.toString().trim().ifEmpty { "unité" }
                        val price = dialogBinding.editPrice.text.toString()
                            .replace(',', '.').toDoubleOrNull()
                        when {
                            name.isEmpty() ->
                                toast(R.string.error_name_required)
                            price == null || price < 0 ->
                                toast(R.string.error_price_invalid)
                            else -> {
                                save(existing, name, unit, price)
                                dismiss()
                            }
                        }
                    }
                }
            }
            .show()
    }

    private fun save(existing: Ingredient?, name: String, unit: String, price: Double) =
        lifecycleScope.launch {
            if (existing == null) {
                dao.insertIngredient(Ingredient(name = name, unit = unit, pricePerUnit = price))
            } else {
                dao.updateIngredient(existing.copy(name = name, unit = unit, pricePerUnit = price))
            }
            load()
        }

    private fun confirmDelete(ingredient: Ingredient) {
        AlertDialog.Builder(this)
            .setMessage("${getString(R.string.delete)} « ${ingredient.name} » ?")
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    dao.deleteIngredient(ingredient)
                    load()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}
