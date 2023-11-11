package com.android.pagosdivididos.expensesList

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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

private const val TAG = "ExpensesListViewModel"

class ExpensesListViewModel(id: UUID) : ViewModel() {
    private val eventRepository = EventRepository.get()

    private val _events: MutableStateFlow<EventData?> = MutableStateFlow(null)
    val events: StateFlow<EventData?> = _events.asStateFlow()
    val id = id

    init {
        viewModelScope.launch {
            _events.value = eventRepository.getEvent(id)
        }
    }

    fun updateCrime(onUpdate: (EventData) -> EventData) {
        _events.update { oldEvent ->
            oldEvent?.let { onUpdate(it) }

        }
        _events.value?.let{ currentEvent->
            updateEventRepository(currentEvent)

        }
    }

    fun updateMember(newMember: Member) {
        _events.value?.let { currentEvent ->
            val updatedMemberList = currentEvent.memberList.toMutableList()
            updatedMemberList.add(newMember)
            val updatedExpenses = currentEvent.expensesList.map { expenses ->
                val updatedListMembers = expenses.expensesMemberList.toMutableList()
                updatedListMembers.add(newMember)
                expenses.copy(expensesMemberList = updatedListMembers)
            }

            //Update each member value for each expense
            val updatedEvent = currentEvent.copy(
                memberList = updatedMemberList,
                expensesList = updatedExpenses
            )
            updateEventMembers(updatedEvent)

        }
    }

    fun updateEventMembers(eventData: EventData){
        val expensesList = eventData.expensesList

        val expenses = expensesList
        for(expense in expenses){
            val memberList = expense.expensesMemberList
            var amountToPay = expense.amountToPay.toInt()
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
            expense.expensesMemberList = memberList
        }
        eventData.copy(expensesList = expensesList)
        _events.value = eventData
        updateEventRepository(eventData)
    }

    fun removeMember(memberToRemove: Member) {

        _events.value?.let { currentEvent ->
            val memberList = currentEvent.memberList.toMutableList()
            memberList.remove(memberToRemove)
            val expensesList = currentEvent.expensesList.toMutableList()
            for (expense in expensesList) {
                val removeExpenseMemberList = expense.expensesMemberList.toMutableList()
                val iterator = removeExpenseMemberList.iterator()

                while (iterator.hasNext()) {
                    val member = iterator.next()
                    if (member.memberName == memberToRemove.memberName) {
                        iterator.remove()
                    }
                }
                if(expense.memberThatPaid.memberName == memberToRemove.memberName) expense.memberThatPaid.hasToPay = false
                expense.expensesMemberList = removeExpenseMemberList
            }

            val updatedEvent = currentEvent.copy(
                memberList = memberList,
                expensesList = expensesList
            )
            updateEventMembers(updatedEvent)
        }
    }

    fun addExpenses(newExpenses: Expenses) {
        _events.value?.let { currentEvent ->
            var newExpensesMemberList = newExpenses
            newExpensesMemberList.expensesMemberList = currentEvent.memberList.map { member ->
                member.copy(payment = 0)
                member.copy(hasToPay = true)
            }
            val updatedExpensesList = currentEvent.expensesList.toMutableList()
            updatedExpensesList.add(newExpensesMemberList)
            val updatedEvent = currentEvent.copy(expensesList = updatedExpensesList)
            _events.value = updatedEvent
            updateEventRepository(updatedEvent)
        }
    }


    override fun onCleared() {
        super.onCleared()
        if (events?.value?.title.toString().trim() != "") {
            events?.value?.let {
                updateEventRepository(it)
            }
        }
    }

    fun refreshList(){
        viewModelScope.launch {
            _events.value = eventRepository.getEvent(id)
        }
    }

    fun updateEventRepository(eventData: EventData){
        val newEvent = eventData.copy(timeStamp = Instant.now())
        viewModelScope.launch {
            eventRepository.updateEvent(newEvent)
        }
    }

    fun getCurrentEvent(): EventData? {
        return _events.value
    }

    fun deleteEvent(eventData: EventData){
        viewModelScope.launch {
            eventRepository.deleteEvent(eventData)
        }
    }

    fun getMemberList():List<Member>{
        return _events.value?.memberList ?: listOf(Member("", 0, true))
    }
}

class ExpensesListViewModelFactory(
    private val id: UUID
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExpensesListViewModel(id) as T
    }
}