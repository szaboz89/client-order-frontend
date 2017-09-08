package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.UserSession;
import com.szabodev.client.order.frontend.utils.WsService;
import com.szabodev.wsclient.ControlBean;
import com.szabodev.wsclient.Orders;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

@SuppressWarnings("serial")
public final class ListOrders extends Layout {

    private DropDownChoice clientid;
    private DropDownChoice productid;
    private int selectedclientid;
    private int selectedproductid;
    TextArea<String> description;

    private String updateidorderid;
    private DropDownChoice updateid;

    @SuppressWarnings("unchecked")
    public ListOrders(final PageParameters parameters) {

        if (UserSession.getInstance().getCurrentUser().getMode() != 1
                && UserSession.getInstance().getCurrentUser().getMode() != 2) {
            throw new RestartResponseAtInterceptPageException(ErrorPage.class);
        }

        final ControlBean controlBeanPort = WsService.getInstance().getServicePort();

        final WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);

        IModel listOrdersIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getOrdersIds();
            }
        };

        listContainer.add(new FeedbackPanel("feedback"));

        IModel listClientIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getClientIds();
            }
        };
        IModel listProductIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getProductIds();
            }
        };

        Form<Object> newOrder = new Form<Object>("newOrder");
        newOrder.add(clientid = new DropDownChoice<Integer>("userid", new PropertyModel(this, "selectedclientid"), listClientIds));
        newOrder.add(productid = new DropDownChoice<Integer>("productid", new PropertyModel(this, "selectedproductid"), listProductIds));
        newOrder.add(description = new TextArea<String>("description", new Model<String>("")));
        newOrder.add(updateid = new DropDownChoice<String>("updateid", new PropertyModel<String>(this, "updateidorderid"), listOrdersIds));
        newOrder.add(new AjaxButton("update_submit") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    String desc = (String) description.getModelObject();
                    Orders order = controlBeanPort.getOrder(Integer.valueOf(updateidorderid));
                    order.setComment(desc);
                    order.setClientid(controlBeanPort.getClient(selectedclientid));
                    order.setProductid(controlBeanPort.getProduct(selectedproductid));
                    controlBeanPort.updateOrder(order);
                    info("Rendelés frissítve!");
                } catch (NumberFormatException e) {
                    info("Hiba, próbálkozzon újra!");
                }
                target.add(listContainer);
            }
        });
        newOrder.add(new AjaxButton("newOrderSubmit") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String desc = (String) description.getModelObject();
                controlBeanPort.createOrderForClient(selectedclientid, selectedproductid, desc);
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
                        Orders order = controlBeanPort.getOrder(id);
                        controlBeanPort.removeOrder(order);
                        success("Rendelés törölve");
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

        IModel listOrders = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getAllOrders();
            }
        };

        listContainer.add(new ListView<Orders>("orders", listOrders) {

            @Override
            protected void populateItem(ListItem<Orders> item) {
                final Orders rendelesek = item.getModelObject();
                item.add(new Label("orderid", rendelesek.getOrdersid()));
                item.add(new Label("productname", rendelesek.getProductid().getProductname()));
                item.add(new Label("clientname", rendelesek.getClientid().getName()));
                item.add(new Label("clientaddress", rendelesek.getClientid().getAddress()));
                item.add(new Label("comment", rendelesek.getComment()));
            }
        });

    }
}
