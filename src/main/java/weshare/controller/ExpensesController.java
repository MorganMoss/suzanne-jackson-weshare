package weshare.controller;

import io.javalin.http.Handler;
import org.javamoney.moneta.function.MonetaryFunctions;
import org.jetbrains.annotations.NotNull;
import weshare.model.Expense;
import weshare.model.PaymentRequest;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import javax.money.NumberValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static weshare.model.MoneyHelper.ZERO_RANDS;
import static weshare.model.MoneyHelper.amountOf;

public class ExpensesController {

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);
        Collection<MonetaryAmount> netExpenseTotal = netExpensesList(expenses);
        MonetaryAmount netExpensesTotal = netExpensesTotal(netExpenseTotal);

        Map<String, Object> viewModel = new HashMap<>();
        viewModel.put("expenses", expenses);
        viewModel.put("netExpensesTotal", netExpensesTotal);

        context.render("expenses.html", viewModel);
    };

    private static Collection<MonetaryAmount> netExpensesList(Collection<Expense> expenses) {
        Collection<MonetaryAmount> netExpenses = new ArrayList<>();

        for(Expense expense: expenses) netExpenses.add(expense.amountLessPaymentsReceived());

        return netExpenses;
    }

    public static MonetaryAmount netExpensesTotal(Collection<MonetaryAmount> netExpensesList) {
        int netExpenseInt = amountOf(0).getNumber().intValue();

        for(MonetaryAmount netExpense: netExpensesList) netExpenseInt += netExpense.getNumber().intValue();

        return amountOf(netExpenseInt);
    }
}
