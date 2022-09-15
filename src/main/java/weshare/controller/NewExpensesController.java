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

public class NewExpensesController {
    /**
     * This handler opens/shows a webpage
     */
    public static final Handler view = context -> {
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
        Expense expense = expensesDAO.save(new Expense(personLoggedIn, context.formParam("description"), amount, date));

        context.redirect(Routes.EXPENSES);
    };

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
