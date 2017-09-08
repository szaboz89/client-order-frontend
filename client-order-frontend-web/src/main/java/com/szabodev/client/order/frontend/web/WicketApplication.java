package com.szabodev.client.order.frontend.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 *
 * @see org.clientorders.web.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return ListProduct.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();
        // add your configuration here
        // getWicketFilter().setFilterPath("");
        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this));
    }
}
