package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.UserSession;
import com.szabodev.client.order.frontend.utils.WsService;
import com.szabodev.wsclient.ControlBean;
import com.szabodev.wsclient.Request;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;


@SuppressWarnings("serial")
public final class ListRequest extends Layout {

    private DropDownChoice clientid;
    private DropDownChoice productid;
    private int selectedclientid;
    private int selectedproductid;
    TextArea<String> description;

    private String updateidorderid;
    private DropDownChoice updateid;

    public ListRequest(final PageParameters parameters) {

        if (UserSession.getInstance().getCurrentUser().getMode() != 1 && UserSession.getInstance().getCurrentUser().getMode() != 2) {
            throw new RestartResponseAtInterceptPageException(ErrorPage.class);
        }

        final ControlBean controlBeanPort = WsService.getInstance().getServicePort();

        final WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);

        IModel listRequestIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getRequestIds();
            }
        };

        listContainer.add(new FeedbackPanel("feedback"));

        Form<Object> newOrder = new Form<Object>("newOrder");
        newOrder.add(updateid = new DropDownChoice<Integer>("updateid", new PropertyModel(this, "updateidorderid"), listRequestIds));
        newOrder.add(new AjaxButton("update_submit") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Request request = controlBeanPort.getRequest(Integer.valueOf(updateidorderid));
                controlBeanPort.createOrderForClient(request.getClientid().getClientid(), request.getProductid().getProductid(), request.getComment());
                info("Rendelés felvéve!");
                target.add(listContainer);
            }
        });
        newOrder.add(new AjaxButton("delete") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (updateidorderid != null) {
                    try {
                        Integer id = Integer.valueOf(updateidorderid);
                        Request request = controlBeanPort.getRequest(id);
                        controlBeanPort.removeRequest(request);
                        success("Igény törölve");
                    } catch (Exception e) {
                        error("Nem törölhető");
                    }

                } else {
                    info("Adja meg a rendelés ID-t");
                }
                target.add(listContainer);
            }
        });
        listContainer.add(newOrder);

        IModel listRequest = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getAllRequest();
            }
        };

        listContainer.add(new ListView<Request>("orders", listRequest) {

            @Override
            protected void populateItem(ListItem<Request> item) {
                final Request rendelesek = item.getModelObject();
                item.add(new Label("orderid", rendelesek.getRequestid()));
                item.add(new Label("productname", rendelesek.getProductid().getProductname()));
                item.add(new Label("clientname", rendelesek.getClientid().getName()));
                item.add(new Label("clientaddress", rendelesek.getClientid().getAddress()));
                item.add(new Label("comment", rendelesek.getComment()));
            }
        });

    }
}
