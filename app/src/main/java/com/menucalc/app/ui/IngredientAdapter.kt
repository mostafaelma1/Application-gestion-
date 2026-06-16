package com.menucalc.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.menucalc.app.data.Ingredient
import com.menucalc.app.databinding.ItemIngredientBinding
import com.menucalc.app.util.Money

class IngredientAdapter(
    private val currency: String,
    private val onClick: (Ingredient) -> Unit,
    private val onDelete: (Ingredient) -> Unit,
) : RecyclerView.Adapter<IngredientAdapter.VH>() {

    private val items = mutableListOf<Ingredient>()

    fun submit(list: List<Ingredient>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemIngredientBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemIngredientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.b
        b.itemName.text = item.name
        b.itemUnit.text = "par ${item.unit}"
        b.itemPrice.text = Money.format(item.pricePerUnit, currency)
        b.root.setOnClickListener { onClick(item) }
        b.itemDelete.setOnClickListener { onDelete(item) }
    }
}
