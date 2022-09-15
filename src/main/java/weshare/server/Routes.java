package weshare.server;

import weshare.controller.*;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes {
    public static final String LOGIN_PAGE = "/";
    public static final String LOGIN_ACTION = "/login.action";
    public static final String LOGOUT = "/logout";
    public static final String EXPENSES = "/expenses";

    public static final String PAYMENT_REQUEST_SENT = "/paymentrequests_sent";
    public static final String PAYMENT_REQUEST_FORM = "/payment-request";
    public static final String SEND_PAYMENT_REQUEST = "/payment-request.action";

    public static final String NEW_EXPENSE = "/new-expense";

    public static final String SAVE_EXPENSE = "/new-expense.action";

    public static final String PAYMENT_REQUEST_RECEIVED = "/paymentrequests_received";

    public static final String PAYMENT_REQUEST_PAID = "/paymentrequests_received.action";

    public static void configure(WeShareServer server) {
        server.routes(() -> {
            post(LOGIN_ACTION,          PersonController.login);
            get(LOGOUT,                 PersonController.logout);
            get(EXPENSES,               ExpensesController.view);
            get(PAYMENT_REQUEST_SENT,   PaymentRequestSentController.view);
            get(PAYMENT_REQUEST_FORM,   PaymentRequestController.form);
            get(NEW_EXPENSE,            NewExpensesController.view);
            post(SEND_PAYMENT_REQUEST,  PaymentRequestController.send);
            post(SAVE_EXPENSE,          NewExpensesController.saveExpense);
            get(PAYMENT_REQUEST_RECEIVED, PaymentRequestController.received);
            post(PAYMENT_REQUEST_PAID,  PaymentRequestController.pay);
        });
    }
}
