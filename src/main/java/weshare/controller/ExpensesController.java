package weshare.controller;

import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import weshare.model.Expense;
import weshare.model.MoneyHelper;
import weshare.model.Person;
import weshare.persistence.ExpenseDAO;
import weshare.server.Routes;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static weshare.model.MoneyHelper.amountOf;

public class ExpensesController {

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);
        expenses = expenses.stream()
                .filter(expense -> !expense.isFullyPaidByOthers())
                .collect(Collectors.toUnmodifiableList());
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

    /**
     * This handler opens/shows a webpage
     */
    public static final Handler viewNewExpensePage = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);
        Map<String, Object> viewModel = Map.of("expenses", expenses);
        context.render("newexpense.html", viewModel);
    };

    /**
     * Saves the details in the form to a list and
     * redirects to the expenses page.
     */
    public static final Handler saveExpense = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        MonetaryAmount amount = complexStringToMoney(Objects.requireNonNull(context.formParam("amount")));
        LocalDate date = stringToDate(context.formParam("date"));
        expensesDAO.save(new Expense(personLoggedIn, context.formParam("description"), amount, date));

        context.redirect(Routes.EXPENSES);
    };

    /**
     * Takes a messy string
     * @param s messy string
     * @return the amount as a long
     */
    @NotNull
    private static MonetaryAmount complexStringToMoney(String s){
        float f = Float.parseFloat(s.replaceAll("[^\\d.]", ""));
        return MoneyHelper.amountOf(Math.round(f));
    }

    /**
     * quick date to string
     * @param s date as a string
     * @return the date
     */
    @NotNull
    private static LocalDate stringToDate(String s){
        return LocalDate.parse(s);
    }
}
