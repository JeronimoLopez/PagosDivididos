package com.android.pagosdivididos.eventlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.pagosdivididos.EventRepository
import com.android.pagosdivididos.database.EventData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventListViewModel : ViewModel() {

    private val eventRepository = EventRepository.get()

    private val _events: MutableStateFlow<List<EventData>> = MutableStateFlow(emptyList())
    val events: StateFlow<List<EventData>>
        get() = _events.asStateFlow()

    val isDatabaseEmpty: StateFlow<Boolean> =
        _events.map { it.isEmpty() }.stateIn(viewModelScope, SharingStarted.Lazily, true)

    init {
        viewModelScope.launch {
            eventRepository.getEventList().collect() {
                _events.value = it
            }
        }
    }

    suspend fun addEvent(event: EventData) {
        eventRepository.addEvent(event)
    }



}