package com.menucalc.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.menucalc.app.data.MenuEntity
import com.menucalc.app.databinding.ItemMenuBinding

/** Un menu accompagné d'un sous-titre déjà formaté (couverts, prix/pers.). */
data class MenuListItem(val menu: MenuEntity, val subtitle: String)

class MenuAdapter(
    private val onClick: (MenuEntity) -> Unit,
    private val onDelete: (MenuEntity) -> Unit,
) : RecyclerView.Adapter<MenuAdapter.VH>() {

    private val items = mutableListOf<MenuListItem>()

    fun submit(list: List<MenuListItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemMenuBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.b
        b.itemName.text = item.menu.name
        b.itemSub.text = item.subtitle
        b.root.setOnClickListener { onClick(item.menu) }
        b.itemDelete.setOnClickListener { onDelete(item.menu) }
    }
}
