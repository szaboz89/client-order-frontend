package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.UserSession;
import com.szabodev.client.order.frontend.utils.WsService;
import com.szabodev.wsclient.Client;
import com.szabodev.wsclient.ControlBean;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public final class ListClients extends Layout {

    private TextField<String> username;
    private TextField<String> address;

    private String updateclientid;
    private DropDownChoice updateid;

    public ListClients(final PageParameters parameters) {

        if (UserSession.getInstance().getCurrentUser().getMode() != 1 && UserSession.getInstance().getCurrentUser().getMode() != 2) {
            throw new RestartResponseAtInterceptPageException(ErrorPage.class);
        }

        final ControlBean controlBeanPort = WsService.getInstance().getServicePort();

        final WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);

        listContainer.add(new FeedbackPanel("feedback"));

        IModel listClientIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getClientIds();
            }
        };

        IModel listClient = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getAllClient();
            }
        };
        final ListView<Client> clientListView = new ListView<Client>("clients", listClient) {

            @Override
            protected void populateItem(ListItem<Client> item) {
                final Client kliensek = item.getModelObject();
                item.add(new Label("clientid", kliensek.getClientid()));
                item.add(new Label("name", kliensek.getName()));
                item.add(new Label("address", kliensek.getAddress()));
            }
        };
        listContainer.add(clientListView);

        Form<Object> createClient = new Form<Object>("createClient");
        createClient.add(username = new TextField<String>("username2", new Model<String>("")));
        createClient.add(address = new TextField<String>("address2", new Model<String>("")));
        createClient.add(updateid = new DropDownChoice<String>("updateid", new PropertyModel<String>(this, "updateclientid"), listClientIds));
        createClient.add(new AjaxButton("update_submit") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    String value1 = (String) username.getModelObject();
                    String value2 = (String) address.getModelObject();
                    Client client = controlBeanPort.getClient(Integer
                            .valueOf(updateclientid));
                    client.setAddress(value2);
                    client.setName(value1);
                    controlBeanPort.updateClient(client);
                    info("Kliens frissítve!");
                } catch (NumberFormatException e) {
                    info("Hiba, próbálkozzon újra!");
                }
                target.add(listContainer);
            }
        });
        createClient.add(new AjaxButton("create_submit") {

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String value1 = (String) username.getModelObject();
                try {
                    String value2 = (String) address.getModelObject();
                    controlBeanPort.createClient(value1, value2);
                    username.setModelObject("");
                    address.setModelObject("");
                    info("Kliens hozzáadva!");
                } catch (Exception e) {
                    info("Hiba, próbálkozzon újra!");
                }
                target.add(listContainer);
            }
        });
        createClient.add(new AjaxButton("delete") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (updateclientid != null) {
                    try {
                        Integer id = Integer.valueOf(updateclientid);
                        Client client = controlBeanPort.getClient(id);
                        controlBeanPort.removeClient(client);
                        success("Kliens törölve");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        error("Nem törölhető");
                    }

                } else {
                    info("Adja meg az ügyfél ID-t");
                }
                target.add(listContainer);
            }
        });
        listContainer.add(createClient);

    }

}
