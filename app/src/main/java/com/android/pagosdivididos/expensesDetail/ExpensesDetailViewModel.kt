package com.android.pagosdivididos.expensesDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.pagosdivididos.EventRepository
import com.android.pagosdivididos.database.EventData
import com.android.pagosdivididos.database.Expenses
import com.android.pagosdivididos.database.Member
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID


class ExpensesDetailViewModel(id: UUID, position: Int, expenses: Expenses) : ViewModel() {

    private val eventRepository = EventRepository.get()

    private val _events: MutableStateFlow<EventData?> = MutableStateFlow(null)
    val events: StateFlow<EventData?> = _events.asStateFlow()

    var expenseToAccess: Expenses = expenses
    val position = position
    val id = id

    init {
        viewModelScope.launch {
            _events.value = eventRepository.getEvent(id)
            getPayment(expenseToAccess)
        }
    }

    fun getTitle(): String {
        return expenseToAccess.expenseName
    }

    fun getMemberList(): List<Member> {
        return expenseToAccess.expensesMemberList
    }

    fun getExpense(): Expenses {
        return expenseToAccess
    }

    fun deleteExpense(newExpense: Expenses) {
        _events.value?.let { currentEvent ->
            val expensesList = currentEvent.expensesList.toMutableList()
            expensesList.remove(newExpense)
            val updatedEvent = currentEvent.copy(expensesList = expensesList, timeStamp = Instant.now())
            viewModelScope.launch {
                eventRepository.updateEvent(updatedEvent)
            }
        }
    }

    fun setSelectedMember(member: Member) {
        val newExpense = expenseToAccess
        val newMember = member.copy(payment = expenseToAccess.amountToPay.toInt())
        newExpense.memberThatPaid = newMember
        newExpense.memberThatPaid.hasToPay = true
        expenseToAccess = newExpense
        updateExpense(newExpense)
    }

    fun getSelectedMemberIndex(): Int {
        val selectedMemberName = expenseToAccess.memberThatPaid.memberName
        val index = expenseToAccess.expensesMemberList.indexOfFirst { it.memberName == selectedMemberName }
        return if (index != -1) {
            index
        } else {
            0
        }
    }

    fun updateSwitchState(memberName: String, isChecked: Boolean) {
        val currentEvent = _events.value

        if (currentEvent != null) {
            val updatedMemberList = expenseToAccess.expensesMemberList
            for (member in updatedMemberList) {
                if (member.memberName == memberName) {
                    member.hasToPay = isChecked
                }
            }
            val updatedExpense = expenseToAccess.copy(expensesMemberList = updatedMemberList)
            getPayment(updatedExpense)
        }
    }

    fun updateExpense(newExpense: Expenses) {
        val currentEvent = _events.value
        if (currentEvent != null && position >= 0 && position < currentEvent.expensesList.size) {
            val updatedExpensesList = currentEvent.expensesList.toMutableList()
            updatedExpensesList[position] = newExpense


            val updatedMemberList: MutableList<Member> = currentEvent.memberList.toMutableList()
            val oldMemberList = expenseToAccess.expensesMemberList
            val newMemberList = newExpense.expensesMemberList
            for (newMember in newMemberList) {
                val oldMember = oldMemberList.firstOrNull { it.memberName == newMember.memberName }
                if (oldMember != null && oldMember.payment != newMember.payment) {
                    val index = updatedMemberList.indexOfFirst { it.memberName == newMember.memberName }
                    if (index != -1) {
                        val newPaymentValue = updatedMemberList[index].payment -oldMember.payment+newMember.payment
                        updatedMemberList[index] = updatedMemberList[index].copy(payment = newPaymentValue)
                    }
                }
            }


            val updatedEvent = currentEvent.copy(
                expensesList = updatedExpensesList,
                timeStamp = Instant.now()
            )
            _events.value = updatedEvent
            expenseToAccess = newExpense
            viewModelScope.launch {
                eventRepository.updateEvent(updatedEvent)
            }
        }
    }

    fun getPayment(expenses: Expenses) {
        val memberList = expenses.expensesMemberList
        var amountToPay = expenses.amountToPay.toInt()
        var counter: Int = 0
        for (member in memberList) {
            if (member.hasToPay) {
                counter++
            }
        }
        if (counter > 0) amountToPay = amountToPay / counter
        for (member in memberList) {
            if (member.hasToPay) {
                member.payment = amountToPay
            } else member.payment = 0
        }
        val updatedExpense = expenses.copy(expensesMemberList = memberList)
        updateExpense(updatedExpense)
    }

}


class ExpensesDetailViewModelFactory(
    private val id: UUID,
    private val position: Int,
    private val expenses: Expenses
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpensesDetailViewModel(id, position, expenses) as T
    }
}
