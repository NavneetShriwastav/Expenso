package com.example.expenso.views.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.expenso.R;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.expenso.adapters.AccountsAdapter;
import com.example.expenso.adapters.CategoryAdapter;
import com.example.expenso.databinding.FragmentAddTransactionBinding;
import com.example.expenso.databinding.ListDialogBinding;
import com.example.expenso.models.Account;
import com.example.expenso.models.Category;
import com.example.expenso.models.Transaction;
import com.example.expenso.utils.Constants;
import com.example.expenso.utils.Helper;
import com.example.expenso.views.activities.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.Calendar;

public class AddTransactionFragment extends BottomSheetDialogFragment {

    public AddTransactionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentAddTransactionBinding binding;
    Transaction transaction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater);


        transaction = new Transaction();

        binding.incomeBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.income_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.greenColor));

            transaction.setType(Constants.INCOME);
        });

        binding.expenseBtn.setOnClickListener(view -> {
            binding.incomeBtn.setBackground(getContext().getDrawable(R.drawable.default_selector));
            binding.expenseBtn.setBackground(getContext().getDrawable(R.drawable.expense_selector));
            binding.incomeBtn.setTextColor(getContext().getColor(R.color.textColor));
            binding.expenseBtn.setTextColor(getContext().getColor(R.color.redColor));

            transaction.setType(Constants.EXPENSE);
        });

        binding.date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext());
                datePickerDialog.setOnDateSetListener((datePicker, i, i1, i2) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    calendar.set(Calendar.MONTH, datePicker.getMonth());
                    calendar.set(Calendar.YEAR, datePicker.getYear());

                    //SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM, yyyy");
                    String dateToShow = Helper.formatDate(calendar.getTime());

                    binding.date.setText(dateToShow);

                    transaction.setDate(calendar.getTime());
                    transaction.setId(calendar.getTime().getTime());
                });
                datePickerDialog.show();
            }
        });

        binding.category.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
            categoryDialog.setView(dialogBinding.getRoot());



            CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), Constants.categories, new CategoryAdapter.CategoryClickListener() {
                @Override
                public void onCategoryClicked(Category category) {
                    binding.category.setText(category.getCategoryName());
                    transaction.setCategory(category.getCategoryName());
                    categoryDialog.dismiss();
                }
            });
            dialogBinding.recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
            dialogBinding.recyclerView.setAdapter(categoryAdapter);

            categoryDialog.show();
        });

        binding.account.setOnClickListener(c-> {
            ListDialogBinding dialogBinding = ListDialogBinding.inflate(inflater);
            AlertDialog accountsDialog = new AlertDialog.Builder(getContext()).create();
            accountsDialog.setView(dialogBinding.getRoot());

            ArrayList<Account> accounts = new ArrayList<>();
            accounts.add(new Account(0, "Cash"));
            accounts.add(new Account(0, "Check"));
            accounts.add(new Account(0, "UPI"));
            accounts.add(new Account(0, "Credit/Debit Card"));
            accounts.add(new Account(0, "Other"));


            AccountsAdapter adapter = new AccountsAdapter(getContext(), accounts, new AccountsAdapter.AccountsClickListener() {
                @Override
                public void onAccountSelected(Account account) {
                    binding.account.setText(account.getAccountName());
                    transaction.setAccount(account.getAccountName());
                    accountsDialog.dismiss();
                }
            });
            dialogBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            //dialogBinding.recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            dialogBinding.recyclerView.setAdapter(adapter);

            accountsDialog.show();

        });

        binding.saveTransactionBtn.setOnClickListener(c -> {
            // Validate the type (Income or Expense) selection
            if (transaction.getType() == null) {
                Helper.showToast(getContext(), "Please select Income or Expense before saving.");
                return;
            }

            // Validate mandatory fields
            if (binding.date.getText().toString().isEmpty()) {
                Helper.showToast(getContext(), "Please select a date.");
                return;
            }

            if (binding.amount.getText().toString().isEmpty()) {
                Helper.showToast(getContext(), "Please enter an amount.");
                return;
            }

            if (binding.category.getText().toString().isEmpty()) {
                Helper.showToast(getContext(), "Please select a category.");
                return;
            }

            if (binding.account.getText().toString().isEmpty()) {
                Helper.showToast(getContext(), "Please select an account.");
                return;
            }

            if (!binding.note.getText().toString().isEmpty() && binding.note.getText().toString().length() >= 30) {
                Helper.showToast(getContext(), "Note is too long. Please limit the length to less than 30 characters.");
                return;
            }

            double amount = Double.parseDouble(binding.amount.getText().toString());
            if (amount <= 0 || amount > 10000000) { // Replace 10000 with the maximum amount you want
                Helper.showToast(getContext(), "Please enter a valid amount between 0 and 10,000,000.");
                return;
            }

            // Parse the amount

            String note = binding.note.getText().toString();

            // Set amount based on type
            if (transaction.getType().equals(Constants.EXPENSE)) {
                transaction.setAmount(amount * -1);
            } else {
                transaction.setAmount(amount);
            }

            transaction.setNote(note);

            // Save the transaction
            ((MainActivity) getActivity()).viewModel.addTransaction(transaction);
            ((MainActivity) getActivity()).getTransactions();
            dismiss();
        });


        return binding.getRoot();
    }
}