package com.mystory.application.views;

import com.mystory.application.security.AuthenticatedUser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@AnonymousAllowed
@PageTitle("My Story | Đăng ký")
@Route(value = "register")
public class RegisterView extends Composite {

    private final AuthenticatedUser authService;

    public RegisterView(AuthenticatedUser authService) {
        this.authService = authService;
    }

    @Override
    protected Component initContent() {
        TextField username = new TextField("Tài khoản");
        PasswordField password1 = new PasswordField("Mật khẩu");
        PasswordField password2 = new PasswordField("Nhập lại mật khẩu");
        return new VerticalLayout(
                new H2("Đăng ký"),
                username,
                password1,
                password2,
                new Button("Đăng ký", event -> register(
                        username.getValue(),
                        password1.getValue(),
                        password2.getValue()
                ))
        );
    }

    private void register(String username, String password1, String password2) {
        if (username.trim().isEmpty()) {
            Notification.show("Nhập tên tài khoản");
        } else if (password1.isEmpty()) {
            Notification.show("Nhập mật khẩu");
        } else if (!password1.equals(password2)) {
            Notification.show("Mật khẩu không kh");
        } else {
            authService.register(username, password1);
            Notification.show("Đăng ký thành công!.");
        }
    }
}
