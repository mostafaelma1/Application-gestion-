package com.menucalc.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.menucalc.app.data.Dish
import com.menucalc.app.databinding.ItemDishBinding
import com.menucalc.app.util.Money

/** Un plat accompagné de son coût par portion, prêt à afficher. */
data class DishListItem(val dish: Dish, val costPerPortion: Double)

class DishAdapter(
    private val currency: String,
    private val onClick: (Dish) -> Unit,
    private val onDelete: (Dish) -> Unit,
) : RecyclerView.Adapter<DishAdapter.VH>() {

    private val items = mutableListOf<DishListItem>()

    fun submit(list: List<DishListItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemDishBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemDishBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.b
        b.itemName.text = item.dish.name
        b.itemSub.text = "${item.dish.portions} portions · " +
            "${Money.format(item.costPerPortion, currency)} / portion"
        b.root.setOnClickListener { onClick(item.dish) }
        b.itemDelete.setOnClickListener { onDelete(item.dish) }
    }
}
