package com.menucalc.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.menucalc.app.data.RecipeLine
import com.menucalc.app.databinding.ItemRecipeBinding
import com.menucalc.app.util.Money
import java.util.Locale

class RecipeAdapter(
    private val currency: String,
    private val onDelete: (RecipeLine) -> Unit,
) : RecyclerView.Adapter<RecipeAdapter.VH>() {

    private val items = mutableListOf<RecipeLine>()

    fun submit(list: List<RecipeLine>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemRecipeBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRecipeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val line = items[position]
        val b = holder.b
        b.lineName.text = line.ingredientName
        val qty = String.format(Locale.FRANCE, "%s %s", trim(line.quantity), line.unit)
        b.lineQty.text = "$qty × ${Money.format(line.pricePerUnit, currency)}"
        b.lineCost.text = Money.format(line.lineCost, currency)
        b.lineDelete.setOnClickListener { onDelete(line) }
    }

    private fun trim(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else String.format(Locale.FRANCE, "%.3f", value).trimEnd('0').trimEnd(',')
}
