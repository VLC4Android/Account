package com.silence.account.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.dao.Dao;
import com.silence.account.pojo.Expense;
import com.silence.account.pojo.ExpenseCat;
import com.silence.account.pojo.ExpenseStatistics;
import com.silence.account.pojo.Income;
import com.silence.account.utils.DBOpenHelper;
import com.silence.account.utils.DateUtils;
import com.silence.account.utils.FormatUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Silence on 2016/3/12 0012.
 */
public class ExpenseDao {
    private Dao<Expense, Income> mDao;
    private Context mContext;

    public ExpenseDao(Context context) {
        mContext = context;
        mDao = DBOpenHelper.getInstance(context).getDao(Expense.class);
    }

    public boolean addExpense(Expense expense) {
        int row = 0;
        try {
            row = mDao.create(expense);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row > 0;
    }

    public float getPeriodSumExpense(Date start, Date end) {
        List<Expense> expenses = getPeriodExpense(start, end);
        try {
            expenses = mDao.queryBuilder().where().between("date", start, end).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        float sum = 0;
        if (expenses != null && expenses.size() > 0) {
            for (int i = 0; i < expenses.size(); i++) {
                sum += expenses.get(i).getAmount();
            }
        }
        return sum;
    }

    public boolean deleteExpense(Expense expense) {
        int row = 0;
        try {
            row = mDao.delete(expense);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row > 0;
    }

    public boolean updateExpense(Expense expense) {
        int row = 0;
        try {
            row = mDao.update(expense);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return row > 0;
    }

    public List<ExpenseStatistics> getPeriodCatSumExpense(Date start, Date end) {
        List<ExpenseStatistics> expenseStatisticses = null;
        String sql = "select expenseCat.name, expenseCat.image, sum(amount) sumCatIncome from expense " +
                ", expenseCat where expense.categoryId = expenseCat.id and date between ? and ? and " +
                "categoryId in (select distinct(categoryId) from expense) group by categoryId;";
        SQLiteDatabase database = DBOpenHelper.getInstance(mContext).getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[]{DateUtils.date2Str(start), DateUtils.date2Str(end)});
        float sumExpense = getPeriodSumExpense(start, end);
        if (cursor.moveToFirst()) {
            expenseStatisticses = new ArrayList<>(cursor.getCount());
            do {
                String categoryName = cursor.getString(0);
                int imageId = cursor.getInt(1);
                float sumCat = cursor.getFloat(2);
                expenseStatisticses.add(new ExpenseStatistics(new ExpenseCat(imageId, categoryName),
                        sumCat, FormatUtils.formatFloat(sumCat / sumExpense * 100)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenseStatisticses;
    }

    public List<Expense> getPeriodExpense(Date start, Date end) {
        List<Expense> expenses = null;
        try {
            expenses = mDao.queryBuilder().where().between("date", start, end).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expenses;
    }
}
