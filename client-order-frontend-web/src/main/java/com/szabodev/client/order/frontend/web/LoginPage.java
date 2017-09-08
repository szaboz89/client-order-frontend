package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.UserSession;
import com.szabodev.wsclient.Employee;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public final class LoginPage extends Layout {

    private TextField<String> username;
    private PasswordTextField password;

    public LoginPage(final PageParameters parameters) {

        add(new FeedbackPanel("feedback"));

        Form<Object> signInForm = new Form<Object>("signInForm");

        signInForm.add(username = new TextField<String>("username", new Model<String>("")));
        signInForm.add(password = new PasswordTextField("password", new Model<String>("")));
        signInForm.add(new Button("submit") {
            @Override
            public void onSubmit() {
                String user = username.getModelObject();
                String pass = password.getModelObject();
                Employee newEmployee = new Employee();
                newEmployee.setPassword(pass);
                newEmployee.setUsername(user);
                boolean success = UserSession.getInstance().login(newEmployee);
                if (success) {
                    setResponsePage(ListProduct.class);
                } else {
                    info("Rossz felhasználónév vagy jelszó!");
                }
            }
        });
        add(signInForm);
    }
}