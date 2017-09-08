package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.UserSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;

public class Layout extends WebPage {

    public Layout() {

        Link<Void> logout = new Link<Void>("logout") {

            @Override
            public void onClick() {
                UserSession.getInstance().logout();
                setResponsePage(LoginPage.class);
            }
        };
        add(logout);

        Link<Void> login = new Link<Void>("login") {

            @Override
            public void onClick() {
                UserSession.getInstance().logout();
                setResponsePage(LoginPage.class);
            }
        };
        add(login);

        if (UserSession.getInstance().isLoggenIn()) {
            if (UserSession.getInstance().getCurrentUser().getMode() == 1) {
                add(new Label("logged", "Bejelentkezve: " + UserSession.getInstance().getCurrentUser().getUsername() + " (admin)"));
            } else if (UserSession.getInstance().getCurrentUser().getMode() == 2) {
                add(new Label("logged", "Bejelentkezve: " + UserSession.getInstance().getCurrentUser().getUsername() + " (dolgozo)"));
            } else {
                add(new Label("logged", "Nincs bejelentkezve!"));
            }
        } else {
            add(new Label("logged", "Nincs bejelentkezve!"));
        }

    }

}
