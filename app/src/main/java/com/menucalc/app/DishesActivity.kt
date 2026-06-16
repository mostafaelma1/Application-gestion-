package com.menucalc.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.menucalc.app.data.AppDao
import com.menucalc.app.data.Dish
import com.menucalc.app.databinding.ActivityListBinding
import com.menucalc.app.ui.DishAdapter
import com.menucalc.app.ui.DishListItem
import kotlinx.coroutines.launch

class DishesActivity : AppCompatActivity() {

    private lateinit var b: ActivityListBinding
    private lateinit var dao: AppDao
    private lateinit var adapter: DishAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityListBinding.inflate(layoutInflater)
        setContentView(b.root)

        val app = application as MenuCalcApp
        dao = app.database.dao()

        b.header.headerTitle.setText(R.string.dishes_title)
        b.header.headerBack.setOnClickListener { finish() }
        b.emptyState.setText(R.string.dishes_empty)

        adapter = DishAdapter(
            currency = app.prefs.currency,
            onClick = { openDish(it.id) },
            onDelete = { confirmDelete(it) },
        )
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter

        b.fab.setOnClickListener { openDish(0L) }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() = lifecycleScope.launch {
        val dishes = dao.allDishes()
        val items = dishes.map { dish ->
            val total = dao.dishCost(dish.id)
            val perPortion = if (dish.portions > 0) total / dish.portions else 0.0
            DishListItem(dish, perPortion)
        }
        adapter.submit(items)
        b.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openDish(id: Long) {
        startActivity(
            Intent(this, DishEditActivity::class.java)
                .putExtra(DishEditActivity.EXTRA_DISH_ID, id)
        )
    }

    private fun confirmDelete(dish: Dish) {
        AlertDialog.Builder(this)
            .setMessage("${getString(R.string.delete)} « ${dish.name} » ?")
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    dao.deleteDish(dish)
                    load()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
