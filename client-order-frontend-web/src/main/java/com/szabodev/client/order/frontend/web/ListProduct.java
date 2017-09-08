package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.WsService;
import com.szabodev.wsclient.ControlBean;
import com.szabodev.wsclient.Product;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.List;

@SuppressWarnings("serial")
public final class ListProduct extends Layout {

    TextField<String> clientid;
    private int selectedproductid;
    TextArea<String> description;

    public ListProduct(final PageParameters parameters) {

        final ControlBean controlBeanPort = WsService.getInstance().getServicePort();

        final WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);

        listContainer.add(new FeedbackPanel("feedback"));

        IModel listProductIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getProductIds();
            }
        };
        Form<Object> newOrder = new Form<Object>("newOrder");
        newOrder.add(clientid = new TextField<String>("userid", new Model<String>("")));
        DropDownChoice productid;
        newOrder.add(productid = new DropDownChoice<Integer>("productid", new PropertyModel(this, "selectedproductid"), listProductIds));
        newOrder.add(description = new TextArea<String>("description", new Model<String>("")));
        newOrder.add(new AjaxButton("newOrderSubmit") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String desc = description.getModelObject();
                Integer selectedclient = Integer.valueOf(clientid.getModelObject());
                if (controlBeanPort.getClient(selectedclient) != null) {
                    controlBeanPort.createRequestForClient(selectedclient, selectedproductid, desc);
                    info("Igény leadva!");
                } else {
                    info("A megadott ügyfélazonosító nem létezik");
                }

                target.add(listContainer);
            }
        });
        listContainer.add(newOrder);

        List<Product> listProducts = controlBeanPort.getAllProduct();
        final ListView<Product> clientListView = new ListView<Product>("products", listProducts) {

            @Override
            protected void populateItem(ListItem<Product> paramListItem) {
                final Product termekek = paramListItem.getModelObject();
                paramListItem.add(new Label("productid", termekek.getProductid()));
                paramListItem.add(new Label("productname", termekek.getProductname()));
                paramListItem.add(new Label("price", termekek.getPrice()));
                paramListItem.add(new Label("description", termekek.getDescription()));

            }
        };
        listContainer.add(clientListView);

    }
}
