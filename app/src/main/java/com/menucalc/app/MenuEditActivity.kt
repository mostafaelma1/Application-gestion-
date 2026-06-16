package com.menucalc.app

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.menucalc.app.calc.CostCalculator
import com.menucalc.app.calc.DishCost
import com.menucalc.app.data.AppDao
import com.menucalc.app.data.MenuDish
import com.menucalc.app.data.MenuEntity
import com.menucalc.app.databinding.ActivityMenuEditBinding
import com.menucalc.app.ui.MenuDishAdapter
import com.menucalc.app.ui.MenuDishItem
import com.menucalc.app.util.Money
import kotlinx.coroutines.launch

class MenuEditActivity : AppCompatActivity() {

    private lateinit var b: ActivityMenuEditBinding
    private lateinit var dao: AppDao
    private lateinit var adapter: MenuDishAdapter
    private lateinit var currency: String

    private var menuId: Long = 0L
    private var couverts: Int = 0
    private var feesPercent: Double = 0.0
    private var marginPercent: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMenuEditBinding.inflate(layoutInflater)
        setContentView(b.root)

        val app = application as MenuCalcApp
        dao = app.database.dao()
        currency = app.prefs.currency
        menuId = intent.getLongExtra(EXTRA_MENU_ID, 0L)

        b.header.headerTitle.setText(if (menuId == 0L) R.string.menu_new else R.string.menu_edit)
        b.header.headerBack.setOnClickListener { finish() }

        adapter = MenuDishAdapter(currency) { row ->
            lifecycleScope.launch {
                dao.deleteMenuDish(row.menuDishId)
                refresh()
            }
        }
        b.recyclerDishes.layoutManager = LinearLayoutManager(this)
        b.recyclerDishes.adapter = adapter

        b.btnSaveMenu.setOnClickListener { saveMenu() }
        b.btnAddDish.setOnClickListener { addDish() }

        if (menuId != 0L) loadMenu()
    }

    private fun loadMenu() = lifecycleScope.launch {
        val menu = dao.menuById(menuId) ?: return@launch
        couverts = menu.couverts
        feesPercent = menu.feesPercent
        marginPercent = menu.marginPercent
        b.editName.setText(menu.name)
        b.editCouverts.setText(menu.couverts.toString())
        b.editFees.setText(trimNumber(menu.feesPercent))
        b.editMargin.setText(trimNumber(menu.marginPercent))
        refresh()
    }

    private fun saveMenu() {
        val name = b.editName.text.toString().trim()
        val couvertsInput = b.editCouverts.text.toString().toIntOrNull() ?: 0
        val fees = b.editFees.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        val margin = b.editMargin.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        if (name.isEmpty()) {
            toast(R.string.error_name_required)
            return
        }
        lifecycleScope.launch {
            couverts = couvertsInput.coerceAtLeast(0)
            feesPercent = fees
            marginPercent = margin
            if (menuId == 0L) {
                menuId = dao.insertMenu(
                    MenuEntity(
                        name = name, couverts = couverts,
                        feesPercent = fees, marginPercent = margin,
                    )
                )
                b.header.headerTitle.setText(R.string.menu_edit)
            } else {
                val current = dao.menuById(menuId)
                dao.updateMenu(
                    MenuEntity(
                        id = menuId, name = name, couverts = couverts,
                        feesPercent = fees, marginPercent = margin,
                        createdAt = current?.createdAt ?: System.currentTimeMillis(),
                    )
                )
            }
            toast(R.string.settings_saved)
            refresh()
        }
    }

    private fun addDish() {
        if (menuId == 0L) {
            Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val dishes = dao.allDishes()
            if (dishes.isEmpty()) {
                toast(R.string.no_dishes_yet)
                return@launch
            }
            val labels = dishes.map { it.name }.toTypedArray()
            AlertDialog.Builder(this@MenuEditActivity)
                .setTitle(R.string.pick_dish)
                .setItems(labels) { _, which ->
                    lifecycleScope.launch {
                        dao.insertMenuDish(MenuDish(menuId = menuId, dishId = dishes[which].id))
                        refresh()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun refresh() = lifecycleScope.launch {
        val rows = dao.menuDishRows(menuId)
        val items = rows.map { row ->
            val total = dao.dishCost(row.dishId)
            val perPortion = if (row.portions > 0) total / row.portions else 0.0
            MenuDishItem(row, perPortion)
        }
        adapter.submit(items)
        b.dishesEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE

        val result = CostCalculator.menuResult(
            items.map { DishCost(dao.dishCost(it.row.dishId), it.row.portions) },
            couverts, feesPercent, marginPercent,
        )
        b.resMaterialPp.text = Money.format(result.materialPerPerson, currency)
        b.resFeesPp.text = Money.format(result.feesPerPerson, currency)
        b.resCostPricePp.text = Money.format(result.costPricePerPerson, currency)
        b.resMarginPp.text = Money.format(result.marginPerPerson, currency)
        b.resSellingPp.text = Money.format(result.sellingPerPerson, currency)
        b.resFoodCost.text = Money.percent(result.foodCostPercent)
        b.resMaterialTotal.text = Money.format(result.materialTotal, currency)
        b.resSellingTotal.text = Money.format(result.sellingTotal, currency)
        b.resMarginTotal.text = Money.format(result.marginTotal, currency)
    }

    private fun trimNumber(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

    companion object {
        const val EXTRA_MENU_ID = "menu_id"
    }
}
