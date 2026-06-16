package com.candlevision.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.candlevision.app.data.Analysis
import com.candlevision.app.databinding.ItemHistoryBinding
import com.candlevision.app.util.ImageUtils
import com.candlevision.app.util.Ui
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onClick: (Analysis) -> Unit,
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    private val items = mutableListOf<Analysis>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale.FRANCE)

    fun submit(list: List<Analysis>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.binding
        val ctx = b.root.context

        b.itemDirection.text = item.direction
        b.itemDirection.setTextColor(Ui.directionColor(ctx, item.direction))

        b.itemDate.text = dateFormat.format(Date(item.createdAt))

        b.itemRisk.text = item.risk
        b.itemRisk.setTextColor(Ui.riskColor(ctx, item.risk))

        b.thumb.setImageBitmap(ImageUtils.decodeThumb(item.imagePath))

        b.root.setOnClickListener { onClick(item) }
    }
}
