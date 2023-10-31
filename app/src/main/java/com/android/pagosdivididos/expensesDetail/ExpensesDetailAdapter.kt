package com.android.pagosdivididos.expensesDetail

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import com.android.pagosdivididos.database.Member
import com.android.pagosdivididos.databinding.ListItemExpensesDetailBinding

class ExpensesDetailAdapter(
    private var memberList: List<Member>
) : RecyclerView.Adapter<ExpensesDetailViewHolder>() {

    interface SwitchToggleListener {
        fun onSwitchToggled(memberName: String, isChecked: Boolean)
    }

    private var switchToggleListener: SwitchToggleListener? = null
    private val switchStates: MutableMap<String, Boolean> = mutableMapOf() // Sto
    private var dataList: List<Member> = emptyList()

    fun setSwitchToggleListener(listener: SwitchToggleListener) {
        switchToggleListener = listener
    }

    init {
        // Initialize switchStates with default values
        memberList.forEach { member ->
            switchStates[member.memberName] = member.hasToPay
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesDetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemExpensesDetailBinding.inflate(inflater, parent, false)
        return ExpensesDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpensesDetailViewHolder, position: Int) {
        val member = memberList[position]
        holder.bind(member)
        holder.switch.isChecked = switchStates[member.memberName] ?: false
        holder.switch.setOnCheckedChangeListener { _, isChecked ->
            switchStates[member.memberName] = isChecked
            switchToggleListener?.onSwitchToggled(member.memberName, isChecked)
            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    fun updateData(newData: List<Member>) {
        dataList = newData
        notifyDataSetChanged()
    }
}

class ExpensesDetailViewHolder(
    private val binding: ListItemExpensesDetailBinding
) : RecyclerView.ViewHolder(binding.root) {
    val switch: Switch = binding.memberDetailSwitch

    fun bind(
        member: Member
    ) {
        binding.memberDetailTitle.text = member.memberName
        binding.memberPay.text = member.payment.toString()
    }

}