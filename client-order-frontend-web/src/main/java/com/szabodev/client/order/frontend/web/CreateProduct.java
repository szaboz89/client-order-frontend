package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.UserSession;
import com.szabodev.client.order.frontend.utils.WsService;
import com.szabodev.wsclient.ControlBean;
import com.szabodev.wsclient.Product;
import org.apache.wicket.RestartResponseAtInterceptPageException;
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

public final class CreateProduct extends Layout {

    private TextField<String> productname;
    private TextField<String> price;
    private TextArea<String> description;

    private DropDownChoice deleteid;
    private int deleteproductid;

    private String updateproductid;
    private DropDownChoice updateid;

    public CreateProduct(final PageParameters parameters) {

        if (UserSession.getInstance().getCurrentUser().getMode() != 1 && UserSession.getInstance().getCurrentUser().getMode() != 2) {
            throw new RestartResponseAtInterceptPageException(ErrorPage.class);
        }

        final ControlBean controlBeanPort = WsService.getInstance().getServicePort();

        final WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);

        IModel listProductIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getProductIds();
            }
        };

        listContainer.add(new FeedbackPanel("feedback"));

        Form<Object> createProduct = new Form<Object>("createProduct");

        createProduct.add(productname = new TextField<String>("productname", new Model<String>("")));
        createProduct.add(price = new TextField<String>("price", new Model<String>("")));
        createProduct.add(description = new TextArea<String>("description", new Model<String>("")));
        createProduct.add(updateid = new DropDownChoice<String>("updateid", new PropertyModel<String>(this, "updateproductid"), listProductIds));
        createProduct.add(new AjaxButton("update_submit") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    String value1 = productname.getModelObject();
                    Double value2 = Double.valueOf(price.getModelObject());
                    String value3 = description.getModelObject();
                    Product product = controlBeanPort.getProduct(Integer.valueOf(updateproductid));
                    product.setProductname(value1);
                    product.setPrice(value2);
                    product.setDescription(value3);
                    controlBeanPort.updateProduct(product);
                    info("Termék frissítve!");
                } catch (Exception e) {
                    info("Hiba, próbálkozzon újra!");
                }
                target.add(listContainer);
            }
        });
        createProduct.add(new AjaxButton("create_submit") {

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    String value1 = productname.getModelObject();
                    Double value2 = Double.valueOf(price.getModelObject());
                    String value3 = description.getModelObject();
                    controlBeanPort.createProduct(value1, value2, value3);
                    info("Termék hozzáadva!");
                } catch (NumberFormatException e) {
                    info("Hiba, próbálkozzon újra!");
                }
                target.add(listContainer);
            }
        });
        createProduct.add(new AjaxButton("delete") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (updateid != null) {
                    Integer id = Integer.valueOf(updateproductid);

                    try {
                        Product product = controlBeanPort.getProduct(id);
                        controlBeanPort.removeProduct(product);
                        success("Termék törölve");
                    } catch (Exception e) {
                        error("Nem törölhető");
                    }

                } else {
                    info("Adja meg a termék ID-t");
                }
                target.add(listContainer);
            }
        });
        listContainer.add(createProduct);

        IModel listProducts = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getAllProduct();
            }
        };

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
