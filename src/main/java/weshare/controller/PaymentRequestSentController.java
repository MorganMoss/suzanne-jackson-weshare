package weshare.controller;

import io.javalin.http.Handler;
import org.eclipse.jetty.server.Request;
import org.javamoney.moneta.function.MonetaryFunctions;
import org.jetbrains.annotations.NotNull;
import weshare.model.Expense;
import weshare.model.PaymentRequest;
import weshare.model.Person;
import weshare.model.WeShareException;
import weshare.persistence.ExpenseDAO;
import weshare.server.ServiceRegistry;
import weshare.server.WeShareServer;

import javax.money.MonetaryAmount;
import javax.money.NumberValue;
import java.util.*;
import java.util.stream.Collectors;

import static weshare.model.MoneyHelper.ZERO_RANDS;
import static weshare.model.MoneyHelper.amountOf;

public class PaymentRequestSentController {

    public static final Handler view = context -> {
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

    public static MonetaryAmount requestsSentTotalAmount(Collection<MonetaryAmount> requestsSentAmountList) {
        int requestSentInt = amountOf(0).getNumber().intValue();

        for(MonetaryAmount requestSentAmount: requestsSentAmountList) requestSentInt += requestSentAmount.getNumber().intValue();

        return amountOf(requestSentInt);
    }
}
