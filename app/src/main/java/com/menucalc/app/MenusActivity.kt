package com.menucalc.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.menucalc.app.calc.CostCalculator
import com.menucalc.app.calc.DishCost
import com.menucalc.app.data.AppDao
import com.menucalc.app.data.MenuEntity
import com.menucalc.app.databinding.ActivityListBinding
import com.menucalc.app.ui.MenuAdapter
import com.menucalc.app.ui.MenuListItem
import com.menucalc.app.util.Money
import kotlinx.coroutines.launch

class MenusActivity : AppCompatActivity() {

    private lateinit var b: ActivityListBinding
    private lateinit var dao: AppDao
    private lateinit var adapter: MenuAdapter
    private lateinit var currency: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityListBinding.inflate(layoutInflater)
        setContentView(b.root)

        val app = application as MenuCalcApp
        dao = app.database.dao()
        currency = app.prefs.currency

        b.header.headerTitle.setText(R.string.menus_title)
        b.header.headerBack.setOnClickListener { finish() }
        b.emptyState.setText(R.string.menus_empty)

        adapter = MenuAdapter(
            onClick = { openMenu(it.id) },
            onDelete = { confirmDelete(it) },
        )
        b.recycler.layoutManager = LinearLayoutManager(this)
        b.recycler.adapter = adapter

        b.fab.setOnClickListener { openMenu(0L) }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() = lifecycleScope.launch {
        val menus = dao.allMenus()
        val items = menus.map { menu ->
            val dishes = dao.menuDishRows(menu.id).map {
                DishCost(totalCost = dao.dishCost(it.dishId), portions = it.portions)
            }
            val result = CostCalculator.menuResult(
                dishes, menu.couverts, menu.feesPercent, menu.marginPercent
            )
            val subtitle = "${menu.couverts} couverts · " +
                "${Money.format(result.sellingPerPerson, currency)} / pers."
            MenuListItem(menu, subtitle)
        }
        adapter.submit(items)
        b.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openMenu(id: Long) {
        startActivity(
            Intent(this, MenuEditActivity::class.java)
                .putExtra(MenuEditActivity.EXTRA_MENU_ID, id)
        )
    }

    private fun confirmDelete(menu: MenuEntity) {
        AlertDialog.Builder(this)
            .setMessage("${getString(R.string.delete)} « ${menu.name} » ?")
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    dao.deleteMenu(menu)
                    load()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
