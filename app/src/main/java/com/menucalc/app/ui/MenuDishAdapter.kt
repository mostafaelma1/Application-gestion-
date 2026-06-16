package com.menucalc.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.menucalc.app.data.MenuDishRow
import com.menucalc.app.databinding.ItemMenuDishBinding
import com.menucalc.app.util.Money

/** Une ligne de menu avec le coût par portion du plat. */
data class MenuDishItem(val row: MenuDishRow, val costPerPortion: Double)

class MenuDishAdapter(
    private val currency: String,
    private val onDelete: (MenuDishRow) -> Unit,
) : RecyclerView.Adapter<MenuDishAdapter.VH>() {

    private val items = mutableListOf<MenuDishItem>()

    fun submit(list: List<MenuDishItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemMenuDishBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMenuDishBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.b
        b.lineName.text = item.row.dishName
        b.lineSub.text = "${Money.format(item.costPerPortion, currency)} / portion"
        b.lineDelete.setOnClickListener { onDelete(item.row) }
    }
}
