package com.android.pagosdivididos.eventlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.pagosdivididos.database.EventData
import com.android.pagosdivididos.databinding.ListItemEventBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EventListAdapter(
    private var eventData: List<EventData>,
    private val onEventClicked: (EventData) -> Unit
) : RecyclerView.Adapter<EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemEventBinding.inflate(inflater, parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val events = eventData[position]
        holder.bind(events, onEventClicked)
    }

    override fun getItemCount() = eventData.size
}

class EventViewHolder(
    private val binding: ListItemEventBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(
        event: EventData,
        onEventClicked: (EventData) -> Unit
    ){
        val currentTimestamp = event.timeStamp
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            .withZone(ZoneId.systemDefault())
        val formattedDate = formatter.format(currentTimestamp)
        binding.eventTitle.text = event.title
        binding.eventTime.text = formattedDate
        binding.root.setOnClickListener{
            onEventClicked(event)
        }
    }
}