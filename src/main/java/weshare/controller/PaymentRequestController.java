package weshare.controller;

import io.javalin.http.Handler;
import weshare.model.*;
import weshare.persistence.ExpenseDAO;
import weshare.persistence.PersonDAO;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

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
        UUID id = UUID.fromString(context.pathParam("Expense_ID"));
        Optional<Expense> expense = expensesDAO.get(id);

        if (expense.isPresent()){
            Expense e =  expense.get();
            if (e.getPerson().equals(personLoggedIn)){
                Map<String, Object> viewModel = new HashMap<>() {{
                    put("expense_date", e.getDate());
                    put("expense_description", e.getDescription());
                    put("expense_amount", e.getAmount());
                    put("expense_UUID", e.getId());
                }};
                context.render("payment-request-form.html", viewModel);
                return;
            }

            throw new WeShareException("User is not the owner of this expense! The owner is " + e.getPerson().getEmail());
        }

        throw new WeShareException("No Expense under that UUID found!");
    };

    /**
     * This handler will submit the data from the form
     * and add it to the DB
     */
    public static final Handler send = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        PersonDAO personDAO = ServiceRegistry.lookup(PersonDAO.class);

        System.out.println(context.formParamMap());

        Optional<Expense> expense = expensesDAO.get(
            UUID.fromString(
                Objects.requireNonNull(
                        context.formParam("expense_UUID")
            )));

        Optional<Person> person = personDAO.findPersonByEmail(context.formParam("email"));

        if (expense.isPresent() && person.isPresent()){
            PaymentRequest paymentRequest = new PaymentRequest(
                    expense.get(),
                    person.get(),
                    MoneyHelper.amountOf(
                            Math.round(Float.parseFloat(
                                    Objects.requireNonNull(
                                            context.formParam("amount")
                                    ).replaceAll("[^\\d.]", "")
                            ))
                    ),
                    LocalDate.parse(
                            Objects.requireNonNull(
                                    context.formParam("due-date")
                            )
                    )
            );

            System.out.println(paymentRequest);
        }
    };


    public static final Handler sent = context -> {
    };

    public static final Handler received = context -> {
    };

    public static final Handler pay = context -> {
    };
}
