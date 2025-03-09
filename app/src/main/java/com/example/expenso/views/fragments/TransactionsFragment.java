package com.example.expenso.views.fragments;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TransactionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.expenso.adapters.TransactionsAdapter;
import com.example.expenso.databinding.FragmentTransactionBinding;
import com.example.expenso.models.Transaction;
import com.example.expenso.utils.Constants;
import com.example.expenso.utils.Helper;
import com.example.expenso.viewmodels.MainViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.Calendar;

import io.realm.RealmResults;


public class TransactionsFragment extends Fragment {

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentTransactionBinding binding;
    Calendar calendar;
    /*
    0 = Daily
    1 = Monthly
    2 = Calendar
    3 = Summary
    4 = Notes
     */

    public MainViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTransactionBinding.inflate(inflater);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        calendar = Calendar.getInstance();
        updateDate();

        binding.nextDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB == Constants.DAILY) {
                calendar.add(Calendar.DATE, 1);
            } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, 1);
            }
            updateDate();
        });

        binding.previousDateBtn.setOnClickListener(c -> {
            if (Constants.SELECTED_TAB == Constants.DAILY) {
                calendar.add(Calendar.DATE, -1);
            } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
                calendar.add(Calendar.MONTH, -1);
            }
            updateDate();
        });

        binding.floatingActionButton.setOnClickListener(c -> {
            new AddTransactionFragment().show(getParentFragmentManager(), null);
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText().equals("Monthly")) {
                    Constants.SELECTED_TAB = 1;
                    updateDate();
                } else if (tab.getText().equals("Daily")) {
                    Constants.SELECTED_TAB = 0;
                    updateDate();
                } else if (tab.getText().equals("Calendar")) {
                    Constants.SELECTED_TAB = 2; // Added case for Calendar tab
                    updateDate();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.transactionsList.setLayoutManager(new LinearLayoutManager(getContext()));

        viewModel.transactions.observe(getViewLifecycleOwner(), new Observer<RealmResults<Transaction>>() {
            @Override
            public void onChanged(RealmResults<Transaction> transactions) {
                TransactionsAdapter transactionsAdapter = new TransactionsAdapter(getActivity(), transactions);
                binding.transactionsList.setAdapter(transactionsAdapter);
                if (transactions != null && transactions.size() > 0) {
                    binding.emptyState.setVisibility(View.GONE);
                } else {
                    binding.emptyState.setVisibility(View.VISIBLE);
                }
                // Log the number of transactions for debugging
                Log.d("TransactionsFragment", "Number of transactions: " + (transactions != null ? transactions.size() : 0));
            }
        });

        viewModel.totalIncome.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                binding.incomeLbl.setText(String.valueOf(aDouble));
            }
        });

        viewModel.totalExpense.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                binding.expenseLbl.setText(String.valueOf(aDouble));
            }
        });

        viewModel.totalAmount.observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double aDouble) {
                binding.totalLbl.setText(String.valueOf(aDouble));
            }
        });

        // Initial fetch of transactions
        viewModel.getTransactions(calendar);

        return binding.getRoot();
    }

   public  void updateDate() {
        if (Constants.SELECTED_TAB == Constants.DAILY) {
            binding.currentDate.setText(Helper.formatDate(calendar.getTime()));
        } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
            binding.currentDate.setText(Helper.formatDateByMonth(calendar.getTime()));
        } else if (Constants.SELECTED_TAB == Constants.CALENDAR) {
            binding.currentDate.setText("All Transactions"); // Display text for Calendar tab
        }
        viewModel.getTransactions(calendar); // Fetch transactions based on selected tab
    }
}