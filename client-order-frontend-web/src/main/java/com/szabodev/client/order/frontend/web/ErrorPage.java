package com.szabodev.client.order.frontend.web;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import com.szabodev.client.order.frontend.utils.UserSession;

public final class ErrorPage extends Layout {

    public ErrorPage(final PageParameters parameters) {

        Link<Void> login = new Link<Void>("login2") {

            @Override
            public void onClick() {
                UserSession.getInstance().logout();
                setResponsePage(LoginPage.class);
            }
        };
        add(login);

    }

}
