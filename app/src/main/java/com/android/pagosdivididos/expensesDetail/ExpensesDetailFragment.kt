package com.android.pagosdivididos.expensesDetail

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.pagosdivididos.R
import com.android.pagosdivididos.databinding.ExpensesDetailFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ExpensesDetailFragment : Fragment(), ExpensesDetailAdapter.SwitchToggleListener {

    private var _binding: ExpensesDetailFragmentBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }


    private val args: ExpensesDetailFragmentArgs by navArgs()

    private val expensesDetailViewModel: ExpensesDetailViewModel by viewModels {
        ExpensesDetailViewModelFactory(args.id, args.position, args.expense)
    }
    private var isUserInteraction = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ExpensesDetailFragmentBinding.inflate(layoutInflater, container, false)
        binding.expenseDetailRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {
            //Spinner
            val spinner: Spinner = binding.spinnerMember
            val spinnerMemberList: List<String> =
                expensesDetailViewModel.getMemberList().map { it.memberName }
            val spinnerAdapter =
                ArrayAdapter(requireContext(), R.layout.custom_spinner_item, spinnerMemberList)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter
            val initialSelection = expensesDetailViewModel.getSelectedMemberIndex()
            if (initialSelection >= 0) {
                spinner.setSelection(initialSelection)
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (isUserInteraction) {
                        val selectedMember = expensesDetailViewModel.getMemberList()[position]
                        expensesDetailViewModel.setSelectedMember(selectedMember)
                    } else {
                        isUserInteraction = true
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            //Title
            expenseTitle.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val title = expenseTitle.text.toString().trim()
                    if (title.isNotEmpty() && title != expensesDetailViewModel.getTitle()) {
                        val currentExpense = expensesDetailViewModel.getExpense()
                        val updatedExpense = currentExpense.copy(expenseName = title)
                        expensesDetailViewModel.updateExpense(updatedExpense)
                    }
                    true
                } else {
                    false
                }
            }

            expenseAmountText.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val price = expenseAmountText.text.toString().trim()
                    if (price.isNotEmpty()) {
                        val currentExpense = expensesDetailViewModel.getExpense()
                        val updatedExpense = currentExpense.copy(amountToPay = price)
                        expensesDetailViewModel.getPayment(updatedExpense)
                    }
                    true
                } else {
                    false
                }
            }

        }
        val adapter = ExpensesDetailAdapter(expensesDetailViewModel.getMemberList())
        adapter.setSwitchToggleListener(this)
        binding.expenseDetailRecyclerView.adapter = adapter


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                expensesDetailViewModel.events.collect {
                    val memberList = expensesDetailViewModel.getMemberList()
                    withContext(Dispatchers.Main) {
                        adapter.updateData(memberList)
                        onUpdate()
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_expenses_detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_detail_delete_event -> {
                deleteExpense()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onUpdate() {
        val expenseToDisplay = expensesDetailViewModel.getExpense()
        binding.apply {
            binding.expenseTitleText.text = expenseToDisplay.expenseName
            binding.expenseTitle.setText(expenseToDisplay.expenseName)
            binding.expenseAmountText.setText(expenseToDisplay.amountToPay)
        }
    }

    private fun deleteExpense() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView =
            inflater.inflate(R.layout.dialog_delete_event, null)

        builder.setView(dialogView)
            .setPositiveButton("Ok") { dialog, _ ->
                val expenseToDelete = expensesDetailViewModel.getExpense()
                viewLifecycleOwner.lifecycleScope.launch {
                    expenseToDelete.let {
                        expensesDetailViewModel.deleteExpense(expenseToDelete)
                        findNavController().popBackStack()
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onSwitchToggled(memberName: String, isChecked: Boolean) {
        expensesDetailViewModel.updateSwitchState(memberName, isChecked)
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


/*
val onBackPressedCallback = object : OnBackPressedCallback(true) {
    override fun handleOnBackPressed() {
        val title = expenseTitle.text.toString().trim()
        if (title.isEmpty()) {
            val toast = Toast.makeText(
                requireContext(),
                "The title is empty",
                Toast.LENGTH_SHORT
            )
            toast.show()
        } else {
            isEnabled = false
            requireActivity().onBackPressed()
        }
    }
}
requireActivity().onBackPressedDispatcher.addCallback(
viewLifecycleOwner,
onBackPressedCallback
)*/

