package weshare.controller;

import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import weshare.model.*;
import weshare.persistence.ExpenseDAO;
import weshare.persistence.PersonDAO;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.util.*;

public class PaymentRequestController {

    /**
     * This handler will create a web form
     * to fill out for a new payment request
     */
    public static final Handler form = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        String idString = context.queryParam("id");

        if (idString == null){
            throw new WeShareException("Please add ?id=<UUID> to this URL");
        }

        UUID id = UUID.fromString(idString);
        Optional<Expense> expense = expensesDAO.get(id);

        if (expense.isPresent()){
            Expense e =  expense.get();
            if (e.getPerson().equals(personLoggedIn)){
                Map<String, Object> viewModel = new HashMap<>() {{
                    put("expense_date", e.getDate());
                    put("expense_description", e.getDescription());
                    put("expense_amount", e.getAmount());
                    put("max_amount", e.totalAmountAvailableForPaymentRequests());
                    put("expense_UUID", e.getId());
                    put("requests", e.listOfPaymentRequests());
                    put("total", e.totalAmountOfPaymentsRequested());
                }};

                context.render("payment-request-form.html", viewModel);
                return;
            }
            throw new WeShareException("User is not the owner of this expense! The owner is " + e.getPerson().getEmail());
        }
        throw new WeShareException("No Expense under that UUID found!");
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

    /**
     * This handler will submit the data from the form
     * and add it to the DB
     */
    public static final Handler send = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);

        UUID uuid = UUID.fromString(Objects.requireNonNull(context.formParam("expense_UUID")));

        Optional<Expense> expense = expensesDAO.get(uuid);

        Optional<Person> person = personDAO.findPersonByEmail(context.formParam("email"));

        if (expense.isPresent() && person.isPresent()){
            PaymentRequest paymentRequest = expense.get().requestPayment(
                    person.get(),
                    complexStringToMoney(Objects.requireNonNull(context.formParam("amount"))),
                    stringToDate(context.formParam("due-date"))
            );
        }

        context.redirect("/payment-request?id="+ uuid);
    };


    public static final Handler sent = context -> {
    };

    public static final Handler received = context -> {
    };

    public static final Handler pay = context -> {
    };
}
