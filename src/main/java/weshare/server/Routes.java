package weshare.server;

import weshare.controller.*;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class Routes {
    public static final String LOGIN_PAGE = "/";
    public static final String LOGIN_ACTION = "/login.action";
    public static final String LOGOUT = "/logout";
    public static final String EXPENSES = "/expenses";

    public static final String PAYMENT_REQUEST_FORM = "/payment-request/{Expense_ID}";
    public static final String SEND_PAYMENT_REQUEST = "/payment-request.action";

    public static void configure(WeShareServer server) {
        server.routes(() -> {
            post(LOGIN_ACTION,          PersonController.login);
            get(LOGOUT,                 PersonController.logout);
            get(EXPENSES,               ExpensesController.view);
            get(PAYMENT_REQUEST_FORM,   PaymentRequestController.form);
            post(SEND_PAYMENT_REQUEST,  PaymentRequestController.send);
        });
    }
}
