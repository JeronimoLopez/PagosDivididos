package com.android.pagosdivididos.expensesList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pagosdivididos.database.Member
import com.android.pagosdivididos.databinding.DialogMemberItemBinding

class MemberListAdapter(
    private val members: List<Member>,
    private val onItemClick: (Member) -> Unit
) : RecyclerView.Adapter<MemberViewHolder>() {
    var memberList = members

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DialogMemberItemBinding.inflate(inflater, parent, false)
        return MemberViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = memberList[position]
        holder.bind(member)
        holder.deleteMember.setOnClickListener{
            onItemClick(member)
            val updatedMembers = memberList.filter { it != member }
            updateData(updatedMembers)
        }
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    fun updateData(newData:List<Member>){
        memberList = newData
        notifyDataSetChanged()

    }
}

class MemberViewHolder(
    private val binding: DialogMemberItemBinding,
    private val onItemClick: (Member) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    private val memberTitle = binding.memberTitle
    val deleteMember = binding.deleteMember

    fun bind(
        member: Member
    ) {
        memberTitle.text = member.memberName

    }

}