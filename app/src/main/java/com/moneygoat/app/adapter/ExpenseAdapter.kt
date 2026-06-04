package com.moneygoat.app.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Expense
import com.moneygoat.app.data.entity.ExpenseWithCategory
import java.io.File

/**
 * Adapter for displaying a list of Expenses in a RecyclerView.
 * Extends ListAdapter for efficient list updates using DiffUtil.
 */
class ExpenseAdapter(private val context: Context) : ListAdapter<ExpenseWithCategory, ExpenseAdapter.VH>(object : DiffUtil.ItemCallback<ExpenseWithCategory>() {
    override fun areItemsTheSame(a: ExpenseWithCategory, b: ExpenseWithCategory) = a.expense.id == b.expense.id
    override fun areContentsTheSame(a: ExpenseWithCategory, b: ExpenseWithCategory) = a == b
}) {
    /**
     * ViewHolder class for individual expense items.
     */
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDesc: TextView = v.findViewById(R.id.tvExpenseDescription)
        val tvAmt: TextView = v.findViewById(R.id.tvExpenseAmount)
        val tvDate: TextView = v.findViewById(R.id.tvExpenseDate)
        val tvTime: TextView = v.findViewById(R.id.tvExpenseTime)
        val tvCat: TextView = v.findViewById(R.id.tvExpenseCategory)
        val ivPhoto: ImageView = v.findViewById(R.id.ivHasPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = getItem(pos)
        val e = item.expense
        h.tvDesc.text = e.description
        h.tvAmt.text = "R %.2f".format(e.amount)
        h.tvDate.text = e.date
        h.tvTime.text = "${e.startTime} - ${e.endTime}"
        h.tvCat.text = item.category?.name ?: "Uncategorized"
        
        // Show photo icon if a receipt photo exists
        if (e.photoPath != null && File(e.photoPath).exists()) {
            h.ivPhoto.visibility = View.VISIBLE
            // Clicking an item with a photo opens a dialog to view the receipt
            h.itemView.setOnClickListener {
                val iv = ImageView(context).apply { 
                    Glide.with(context).load(File(e.photoPath)).into(this)
                    adjustViewBounds = true
                    setPadding(16,16,16,16) 
                }
                AlertDialog.Builder(context)
                    .setTitle("Receipt: ${e.description}")
                    .setView(iv)
                    .setPositiveButton("Close", null)
                    .show()
            }
        } else { 
            h.ivPhoto.visibility = View.GONE
            h.itemView.setOnClickListener(null) 
        }
    }
}
