package com.szabodev.client.order.frontend.utils;

import com.szabodev.wsclient.ControlBean;
import com.szabodev.wsclient.ControlBeanService;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class WsService {

    private static WsService wsService;

    public static synchronized WsService getInstance() {
        if (wsService == null) {
            wsService = new WsService();
        }
        return wsService;
    }

    public ControlBean getServicePort() {

        String wsUrl = EnvironmentLoader.getInstance().getProperty("wsurl");
        String userName = EnvironmentLoader.getInstance().getProperty("username");
        String password = EnvironmentLoader.getInstance().getProperty("password");

        URL endpoint = null;
        try {
            endpoint = new URL(wsUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        ControlBeanService service = new ControlBeanService(endpoint, new QName("http://ejb.backend.order.client.szabodev.com/", "ControlBeanService"));
        final ControlBean controlBeanPort = service.getControlBeanPort();

        Map<String, Object> ctx = ((BindingProvider) controlBeanPort).getRequestContext();
        ctx.put(BindingProvider.USERNAME_PROPERTY, userName);
        ctx.put(BindingProvider.PASSWORD_PROPERTY, password);

        return controlBeanPort;
    }
}
