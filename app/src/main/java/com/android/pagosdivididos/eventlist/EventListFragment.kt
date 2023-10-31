package com.android.pagosdivididos.eventlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.pagosdivididos.R
import com.android.pagosdivididos.databinding.EventListFragmentBinding
import kotlinx.coroutines.launch
import com.android.pagosdivididos.database.EventData
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Instant
import java.util.UUID

class EventListFragment : Fragment() {

    private var _binding: EventListFragmentBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val eventListViewModel: EventListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        activity?.setTitle(getString(R.string.app_name))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = EventListFragmentBinding.inflate(inflater, container, false)
        binding.eventRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noEventTextView = view.findViewById<TextView>(R.id.noEventsTextView)

        viewLifecycleOwner.lifecycleScope.launch {
            eventListViewModel.isDatabaseEmpty
                .onEach { isEmpty ->
                    if (isEmpty) {
                        noEventTextView.visibility = View.VISIBLE
                    } else {
                        noEventTextView.visibility = View.GONE
                    }
                }.launchIn(this)
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                eventListViewModel.events.collect() { event ->
                    val reversedEventData = event.sortedByDescending { it.timeStamp }
                    binding.eventRecyclerView.adapter =
                        EventListAdapter(reversedEventData) { event ->
                            findNavController().navigate(
                                EventListFragmentDirections.showExpensesList(event.id)
                            )
                        }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_eventlist_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_eventlist_add -> {
                showNewEvent()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showNewEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newEvent = EventData(
                id = UUID.randomUUID(),
                title = "",
                expensesList = emptyList(),
                memberList = emptyList(),
                Instant.now()
            )
            eventListViewModel.addEvent(newEvent)
            findNavController().navigate(
                EventListFragmentDirections.showExpensesList(newEvent.id)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}