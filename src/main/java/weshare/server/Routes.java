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

    public static final String NEW_EXPENSE = "/newexpense";

    public static final String SAVE_EXPENSE = "/newexpense.action";

    public static final String PAYMENT_REQUEST_RECEIVED = "/paymentrequests_received";

    public static final String PAYMENT_REQUEST_PAID = "/paymentrequests_received.action";

    public static void configure(WeShareServer server) {
        server.routes(() -> {
            post(LOGIN_ACTION,          PersonController.login);
            get(LOGOUT,                 PersonController.logout);
            post(SEND_PAYMENT_REQUEST,  PaymentRequestController.send);
            get(PAYMENT_REQUEST_SENT,   PaymentRequestController.sent);
            get(PAYMENT_REQUEST_FORM,   PaymentRequestController.form);
            get(PAYMENT_REQUEST_RECEIVED, PaymentRequestController.received);
            post(PAYMENT_REQUEST_PAID,  PaymentRequestController.pay);
            get(EXPENSES,               ExpensesController.view);
            get(NEW_EXPENSE,            ExpensesController.viewNewExpensePage);
            post(SAVE_EXPENSE,          ExpensesController.saveExpense);
        });
    }
}
