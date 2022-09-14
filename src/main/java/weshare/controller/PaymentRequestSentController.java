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

public class PaymentRequestSentController {

    public static final Handler view = context -> {
        ExpenseDAO expensesDAO = ServiceRegistry.lookup(ExpenseDAO.class);
        Person personLoggedIn = WeShareServer.getPersonLoggedIn(context);

        Collection<Expense> expenses = expensesDAO.findExpensesForPerson(personLoggedIn);
        Collection<PaymentRequest> requestsSent = new ArrayList<>();

        for(Expense expense: expenses) {
            for(PaymentRequest request: expense.listOfPaymentRequests()) {
                request.getPersonWhoShouldPayBack().getName();
                request.getDescription();
                request.daysLeftToPay();
                request.getAmountToPay();
                requestsSent.add(request);
            }
        }

        Map<String, Object> viewModel = new HashMap<>();
        viewModel.put("requestsSent", requestsSent);

        context.render("payment-request-sent.html", viewModel);
    };

    public static MonetaryAmount requestsSentTotalAmount(Collection<MonetaryAmount> requestsSentAmountList) {
        int requestSentInt = amountOf(0).getNumber().intValue();

        for(MonetaryAmount requestSentAmount: requestsSentAmountList) requestSentInt += requestSentAmount.getNumber().intValue();

        return amountOf(requestSentInt);
    }
}
