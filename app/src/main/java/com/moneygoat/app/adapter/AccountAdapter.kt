package com.moneygoat.app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.moneygoat.app.R
import com.moneygoat.app.data.entity.Account

class AccountAdapter : ListAdapter<Account, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false)
        return AccountViewHolder(view)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AccountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvAccountName)
        private val tvType: TextView = view.findViewById(R.id.tvAccountType)
        private val tvBalance: TextView = view.findViewById(R.id.tvAccountBalance)

        fun bind(account: Account) {
            tvName.text = account.name
            tvType.text = account.type
            tvBalance.text = "R %.2f".format(account.balance)
        }
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean = oldItem == newItem
    }
}
