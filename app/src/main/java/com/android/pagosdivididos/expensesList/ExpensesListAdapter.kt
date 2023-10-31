package com.android.pagosdivididos.expensesList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pagosdivididos.database.Expenses
import com.android.pagosdivididos.databinding.ListItemExpensesBinding
import java.util.UUID

class ExpensesListAdapter(
    private var expenses: List<Expenses>,
    private val onItemClickListener: (UUID, Int, Expenses) -> Unit
) : RecyclerView.Adapter<ExpensesListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemExpensesBinding.inflate(inflater, parent, false)
        return ExpensesListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpensesListViewHolder, position: Int) {
        val expense = expenses[position]
        expense?.let { holder.bind(it) }
        holder.itemView.setOnClickListener {
            onItemClickListener(expense.expensesId, position, expense)
        }

    }

    override fun getItemCount(): Int {
        return expenses?.size
    }
}

class ExpensesListViewHolder(
    private val binding: ListItemExpensesBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        expenses: Expenses
    ) {
        binding.expensesListTitle.text = expenses.expenseName
        binding.expensesListMemberPay.text = expenses.amountToPay
    }
}

