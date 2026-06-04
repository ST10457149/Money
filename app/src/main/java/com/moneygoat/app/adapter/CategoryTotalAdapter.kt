package com.moneygoat.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.CategoryTotal

/**
 * Adapter for displaying category-wise spending totals in a RecyclerView.
 * This is used in the analytics/history view.
 */
class CategoryTotalAdapter : ListAdapter<CategoryTotal, CategoryTotalAdapter.VH>(object : DiffUtil.ItemCallback<CategoryTotal>() {
    override fun areItemsTheSame(a: CategoryTotal, b: CategoryTotal) = a.categoryName == b.categoryName
    override fun areContentsTheSame(a: CategoryTotal, b: CategoryTotal) = a == b
}) {
    /**
     * ViewHolder for the category total item.
     */
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvCatTotalName)
        val tvAmt: TextView = v.findViewById(R.id.tvCatTotalAmount)
    }

    override fun onCreateViewHolder(p: ViewGroup, vt: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_category_total, p, false))

    override fun onBindViewHolder(h: VH, pos: Int) { 
        val i = getItem(pos)
        h.tvName.text = i.categoryName
        h.tvAmt.text = "R %.2f".format(i.totalAmount) 
    }
}
