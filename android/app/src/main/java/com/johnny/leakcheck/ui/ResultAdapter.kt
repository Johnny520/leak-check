package com.johnny.leakcheck.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.johnny.leakcheck.databinding.ItemResultBinding

data class ResultRow(val label: String, val values: List<String>)

class ResultAdapter : RecyclerView.Adapter<ResultAdapter.Holder>() {

    private val items = ArrayList<ResultRow>()

    fun submit(rows: List<ResultRow>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = items[position]
        holder.binding.txtField.text = row.label
        holder.binding.txtValues.text = row.values.joinToString("、")
    }

    override fun getItemCount(): Int = items.size

    class Holder(val binding: ItemResultBinding) : RecyclerView.ViewHolder(binding.root)
}
