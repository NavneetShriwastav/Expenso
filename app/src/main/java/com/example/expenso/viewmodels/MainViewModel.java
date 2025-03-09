package com.example.expenso.viewmodels;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.expenso.models.Transaction;
import com.example.expenso.utils.Constants;
import java.util.Calendar;
import java.util.Date;
import io.realm.Realm;
import io.realm.RealmResults;
public class MainViewModel extends AndroidViewModel {

    public MutableLiveData<RealmResults<Transaction>> transactions = new MutableLiveData<>();
    public MutableLiveData<RealmResults<Transaction>> categoriesTransactions = new MutableLiveData<>();

    public MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    public MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    public MutableLiveData<Double> totalAmount = new MutableLiveData<>();

    private Realm realm;
    private Calendar calendar;

    public MainViewModel(@NonNull Application application) {
        super(application);
        Realm.init(application);
        setupDatabase();
    }

    public void getTransactions(Calendar calendar, String type) {
        Calendar cal = (Calendar) calendar.clone(); // Clone the calendar object
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        RealmResults<Transaction> newTransactions;
        if (Constants.SELECTED_TAB_STATS == Constants.DAILY) {
            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", cal.getTime())
                    .lessThan("date", new Date(cal.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", type)
                    .findAll();
        } else if (Constants.SELECTED_TAB_STATS == Constants.MONTHLY) {
            cal.set(Calendar.DAY_OF_MONTH, 1);

            Date startTime = cal.getTime();
            cal.add(Calendar.MONTH, 1);
            Date endTime = cal.getTime();

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", type)
                    .findAll();
        } else {
            newTransactions = null;
        }

        categoriesTransactions.setValue(newTransactions);
    }

    public void getTransactions(Calendar calendar) {
        if (calendar == null) {
            calendar = Calendar.getInstance(); // Default to current date if null
        }
        this.calendar = calendar;
        Calendar cal = (Calendar) calendar.clone(); // Clone the calendar object
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        double income = 0;
        double expense = 0;
        double total = 0;
        RealmResults<Transaction> newTransactions;

        if (Constants.SELECTED_TAB == Constants.DAILY) {
            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", cal.getTime())
                    .lessThan("date", new Date(cal.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .findAll();

            income = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", cal.getTime())
                    .lessThan("date", new Date(cal.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", Constants.INCOME)
                    .sum("amount")
                    .doubleValue();

            expense = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", cal.getTime())
                    .lessThan("date", new Date(cal.getTime().getTime() + (24 * 60 * 60 * 1000)))
                    .equalTo("type", Constants.EXPENSE)
                    .sum("amount")
                    .doubleValue();
        } else if (Constants.SELECTED_TAB == Constants.MONTHLY) {
            cal.set(Calendar.DAY_OF_MONTH, 1);

            Date startTime = cal.getTime();
            cal.add(Calendar.MONTH, 1);
            Date endTime = cal.getTime();

            newTransactions = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .findAll();

            income = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", Constants.INCOME)
                    .sum("amount")
                    .doubleValue();

            expense = realm.where(Transaction.class)
                    .greaterThanOrEqualTo("date", startTime)
                    .lessThan("date", endTime)
                    .equalTo("type", Constants.EXPENSE)
                    .sum("amount")
                    .doubleValue();
        } else if (Constants.SELECTED_TAB == Constants.CALENDAR) {
            // For the calendar tab, fetch all transactions regardless of the year
            newTransactions = realm.where(Transaction.class).findAll(); // Fetch all transactions

            income = realm.where(Transaction.class)
                    .equalTo("type", Constants.INCOME)
                    .sum("amount")
                    .doubleValue();

            expense = realm.where(Transaction.class)
                    .equalTo("type", Constants.EXPENSE)
                    .sum("amount")
                    .doubleValue();
        } else {
            newTransactions = null;
        }

        total = income - expense;

        totalIncome.setValue(income);
        totalExpense.setValue(expense);
        totalAmount.setValue(total);
        transactions.setValue(newTransactions);


        // Log results for debugging
        Log.d("Transactions", "Total Income: " + income);
        Log.d("Transactions", "Total Expense: " + expense);
        Log.d("Transactions", "Total Transactions Count: " + (newTransactions != null ? newTransactions.size() : 0));
    }


    public void addTransaction(Transaction transaction) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(transaction);
        realm.commitTransaction();
    }

    public void deleteTransaction(Transaction transaction) {
        realm.beginTransaction();
        transaction.deleteFromRealm();
        realm.commitTransaction();

        if (calendar == null) {
            calendar = Calendar.getInstance(); // Fallback to current date if null
        }
        getTransactions(calendar);
    }

    public void addTransactions() {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Business", "Cash", "Some note here", new Date(), 500, new Date().getTime()));
        realm.copyToRealmOrUpdate(new Transaction(Constants.EXPENSE, "Investment", "Bank", "Some note here", new Date(), -900, new Date().getTime()));
        realm.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Rent", "Other", "Some note here", new Date(), 500, new Date().getTime()));
        realm.copyToRealmOrUpdate(new Transaction(Constants.INCOME, "Business", "Card", "Some note here", new Date(), 500, new Date().getTime()));
        realm.commitTransaction();
    }

    private void setupDatabase() {
        realm = Realm.getDefaultInstance();
    }
}