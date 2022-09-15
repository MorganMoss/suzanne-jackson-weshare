package weshare.controller;

import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import weshare.model.*;
import weshare.persistence.ExpenseDAO;
import weshare.persistence.PersonDAO;
import weshare.server.Routes;
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
                Map<String, Object> viewModel = new HashMap<>();

                viewModel.put("expense", e);
                viewModel.put("requests", e.listOfPaymentRequests());

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
            expense.get().requestPayment(
                person.get(),
                complexStringToMoney(Objects.requireNonNull(context.formParam("amount"))),
                stringToDate(context.formParam("due-date"))
            );
        }

        context.redirect("/payment-request?id="+ uuid);
    };


    public static final Handler sent = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<PaymentRequest> requests = expensesDAO.findPaymentRequestsSent(personLoggedIn);
        Map<String, Object> viewModel = new HashMap<>();

        requests.stream()
                .map(PaymentRequest::getAmountToPay)
                .reduce(MonetaryAmount::add)
                .ifPresent(monetaryAmount -> viewModel.put("total", monetaryAmount));

        viewModel.put("requestsSent", requests);

        context.render("payment-request-sent.html", viewModel);
    };

    public static final Handler received = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        Collection<PaymentRequest> paymentRequests = expensesDAO.findPaymentRequestsReceived(personLoggedIn);
        Map<String, Object> modelView = new HashMap<>(Map.of("requests", paymentRequests));

        modelView.put("total", MoneyHelper.ZERO_RANDS);

        paymentRequests.stream()
            .filter(paymentRequest -> !paymentRequest.isPaid())
            .map(PaymentRequest::getAmountToPay)
            .reduce(MonetaryAmount::add)
            .ifPresent(monetaryAmount -> modelView.put("total", monetaryAmount));

        context.render("paymentrequests_received.html", modelView);
    };

    public static final Handler pay = context -> {

        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);
        UUID requestID = UUID.fromString(context.formParam("request_id"));
        UUID expenseID = UUID.fromString(context.formParam("expense_id"));

        Optional<Expense> expense = expensesDAO.get(expenseID);

        if (expense.isPresent()) {
            Expense e = expense.get();
            Payment payment = e.payPaymentRequest(requestID, personLoggedIn, DateHelper.TODAY);
            expensesDAO.save(payment.getExpenseForPersonPaying());
        }

        context.redirect(Routes.PAYMENT_REQUEST_RECEIVED);
    };
}
