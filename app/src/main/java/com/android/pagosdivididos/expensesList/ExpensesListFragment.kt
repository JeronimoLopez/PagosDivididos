package com.android.pagosdivididos.expensesList

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.pagosdivididos.R
import com.android.pagosdivididos.database.EventData
import com.android.pagosdivididos.database.Expenses
import com.android.pagosdivididos.database.Member
import com.android.pagosdivididos.databinding.ExpensesListFragmentBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class ExpensesListFragment : Fragment() {

    private var _binding: ExpensesListFragmentBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: ExpensesListFragmentArgs by navArgs()

    private val expensesListViewModel: ExpensesListViewModel by viewModels {
        ExpensesListViewModelFactory(args.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ExpensesListFragmentBinding.inflate(layoutInflater, container, false)
        binding.expenseRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val title = expensesTitle.text.toString().trim()
                    if (title.isEmpty()) {
                        val snackbar = Snackbar.make(
                            requireView(),
                            getString(R.string.empty_title_snackbar),
                            Snackbar.LENGTH_SHORT
                        )

                        snackbar.setAction(getString(R.string.snackbar_dismiss)) {
                            snackbar.dismiss()
                        }

                        snackbar.show()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressed()
                    }
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                onBackPressedCallback
            )

            expensesTitle.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val title = expensesTitle.text.toString().trim()
                    if (title.isNotEmpty()) {
                        expensesListViewModel.updateCrime { oldEvent ->
                            oldEvent.copy(title = title)
                        }
                    }
                    true
                } else {
                    false
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                expensesListViewModel.events.collect() { event ->
                    event?.let {
                        updateUi(it)
                        val adapter =
                            ExpensesListAdapter(event.expensesList) { eventDataId, expensesPosition, expense ->
                                findNavController().navigate(
                                    ExpensesListFragmentDirections.showExpensesDetail(
                                        eventDataId,
                                        expensesPosition,
                                        expense
                                    )
                                )
                            }
                        binding.expenseRecyclerView.adapter = adapter
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_expenseslist_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_member -> {
                addMember()
                true
            }

            R.id.menu_expenseslist_add -> {
                addExpenses()
                true
            }

            R.id.menu_delete_event -> {
                deleteEvent()
                true
            }

            R.id.menu_member_list -> {
                showMemberList()
                true
            }

            R.id.menu_get_expenses -> {
                checkShare()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addMember() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_set_member_name, null)

        builder.setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_save)) { dialog, _ ->
                val editTextMemberName = dialogView.findViewById<EditText>(R.id.editTextMemberName)
                val memberName = editTextMemberName.text.toString()
                memberName.replaceFirstChar { it.uppercase() }


                if (memberName.isNotBlank()) {
                    val existingEvent: EventData? = expensesListViewModel.events.value
                    val newMember = Member(memberName, 0, true)
                    val updatedMemberList =
                        existingEvent?.memberList?.toMutableList() ?: mutableListOf()
                    updatedMemberList.add(newMember)
                    expensesListViewModel.updateMember(newMember)
                } else {
                    val snackbar = Snackbar.make(
                        requireView(),
                        getString(R.string.add_member_snackbar),
                        Snackbar.LENGTH_SHORT
                    )
                    snackbar.setAction(getString(R.string.snackbar_dismiss)) {
                        snackbar.dismiss()
                    }
                    snackbar.show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun addExpenses() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_set_expenses_name, null)

        builder.setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_save)) { dialog, _ ->
                val editTextExpensesName =
                    dialogView.findViewById<EditText>(R.id.editTextExpensesName)
                val expensesName = editTextExpensesName.text.toString()

                if (expensesName.isNotBlank()) {
                    val newExpenses =
                        Expenses(
                            args.id,
                            expensesName,
                            Member("", 0, false),
                            "0",
                            emptyList()
                        )
                    expensesListViewModel.addExpenses(newExpenses)
                } else {
                    val snackbar = Snackbar.make(
                        requireView(),
                        getString(R.string.add_expense_snackbar),
                        Snackbar.LENGTH_SHORT
                    )

                    snackbar.setAction(getString(R.string.snackbar_dismiss)) {
                        snackbar.dismiss()
                    }

                    snackbar.show()
                }

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun deleteEvent() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_delete_event, null)

        builder.setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ ->
                val eventToDelete = expensesListViewModel.getCurrentEvent()
                viewLifecycleOwner.lifecycleScope.launch {
                    eventToDelete?.let {
                        expensesListViewModel.deleteEvent(eventToDelete)
                        findNavController().navigate(
                            ExpensesListFragmentDirections.deleteEvent()
                        )
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun showMemberList() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_member_list, null)
        val memberList: List<Member> = expensesListViewModel.getMemberList()
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.member_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapterMember = MemberListAdapter(memberList) { clickedMember ->
            expensesListViewModel.removeMember(clickedMember)
        }
        recyclerView.adapter = adapterMember

        builder.setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()

    }

    private fun updateUi(eventData: EventData) {
        binding.apply {
            expensesListTitle.setText(eventData.title)
            expensesTitle.setText(eventData.title)
        }
    }

    //Checks if every expense has a memberThatPaid = true
    private fun checkShare() {
        val eventData: EventData? = expensesListViewModel.getCurrentEvent()
        eventData?.let {
            val expensesList = eventData.expensesList
            var flag = true
            for (expense in expensesList) {
                if (!expense.memberThatPaid.hasToPay) {
                    noMemberSelected(expense.expenseName)
                    flag = false
                }
            }
            if (flag) shareExpenses(expensesString(eventData))
        }
    }

    //Adds payments
    private fun expensesString(eventData: EventData): List<Member> {
        val expensesList = eventData.expensesList
        val memberList = eventData.memberList.toMutableList()
        for (expense in expensesList) {
            val expensesMemberList = expense.expensesMemberList
            for (expensesMember in expensesMemberList) {
                val memberToUpdate = memberList.find { it.memberName == expensesMember.memberName }
                if (memberToUpdate != null) {
                    memberToUpdate.payment += expensesMember.payment
                }
            }
        }
        return memberList
    }

    //Removes expenses
    private fun shareExpenses(memberList: List<Member>) {
        val updatedMemberList = memberList.map { it.copy() }.toMutableList()
        val eventData = expensesListViewModel.getCurrentEvent()
        val logStringBuilder = StringBuilder()
        eventData?.let { event ->
            val expensesList = event.expensesList
            for (expense in expensesList) {
                val allHasToPayFalse = expense.expensesMemberList.all { !it.hasToPay }
                val memberToUpdate =
                    updatedMemberList.find { it.memberName == expense.memberThatPaid.memberName }
                if (memberToUpdate != null && !allHasToPayFalse) {
                    memberToUpdate.payment -= expense.memberThatPaid.payment
                }
            }
            eventData.copy(memberList = updatedMemberList)

            val membersWithNegativeBalance = updatedMemberList.filter { it.payment < 0 }
            val membersWithPositiveBalance = updatedMemberList.filter { it.payment > 0 }
            for (negativeBalanceMember in membersWithNegativeBalance) {
                for (positiveBalanceMember in membersWithPositiveBalance) {
                    val amountToTransfer = minOf(-negativeBalanceMember.payment, positiveBalanceMember.payment)
                    negativeBalanceMember.payment += amountToTransfer
                    positiveBalanceMember.payment -= amountToTransfer
                    if(amountToTransfer > 0){
                        val logMessage = "${positiveBalanceMember.memberName} ${getString(R.string.has_to_pay)} $amountToTransfer ${getString(R.string.to)} ${negativeBalanceMember.memberName}"
                        logStringBuilder.append(logMessage).append("\n")
                    }
                }
            }
        }
        val logString = logStringBuilder.toString()
        getExpensesReport(logString)
    }

    private fun getExpensesReport(expensesString:String) {
        if(!expensesString.isBlank()){
            val reportText = expensesString

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, reportText)

            if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } else {
                val snackbar = Snackbar.make(
                    requireView(),
                    getString(R.string.sharing_app_snackbar),
                    Snackbar.LENGTH_SHORT
                )

                snackbar.setAction(getString(R.string.snackbar_dismiss)) {
                    snackbar.dismiss()
                }

                snackbar.show()
            }
        } else noExpenses()
    }

    private fun noExpenses() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_no_member_selected, null)

        val expenseNameTextView = dialogView.findViewById<TextView>(R.id.dialog_no_member_message)
        expenseNameTextView.text = "${getString(R.string.no_expense)}"

        builder.setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()

    }


    private fun noMemberSelected(expenseName: String) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_no_member_selected, null)

        val expenseNameTextView = dialogView.findViewById<TextView>(R.id.dialog_no_member_message)
        expenseNameTextView.text = "${getString(R.string.no_member_expense)} $expenseName"

        builder.setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()

    }

    override fun onResume() {
        super.onResume()
        expensesListViewModel.refreshList()
    }

}