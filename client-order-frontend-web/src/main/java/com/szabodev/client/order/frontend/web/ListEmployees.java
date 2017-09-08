package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.PasswordUtil;
import com.szabodev.client.order.frontend.utils.UserSession;
import com.szabodev.client.order.frontend.utils.WsService;
import com.szabodev.wsclient.ControlBean;
import com.szabodev.wsclient.Employee;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public final class ListEmployees extends Layout {

    TextField<String> username;
    PasswordTextField userpassword;
    PasswordTextField repeatpassword;
    TextField<String> mode;

    private DropDownChoice updateid;
    private String updateemployeeid;

    public ListEmployees(final PageParameters parameters) {

        if (UserSession.getInstance().getCurrentUser().getMode() != 1) {
            throw new RestartResponseAtInterceptPageException(ErrorPage.class);
        }

        final ControlBean controlBeanPort = WsService.getInstance().getServicePort();

        final WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);

        listContainer.add(new FeedbackPanel("feedback"));

        IModel listEmployeeIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getEmployeeIds();
            }
        };

        Form<Object> createEmployee = new Form<Object>("createEmployee");

        createEmployee.add(username = new TextField<String>("username", new Model<String>("")));
        createEmployee.add(userpassword = new PasswordTextField("userpassword", new Model<String>("")));
        createEmployee.add(mode = new TextField<String>("mode", new Model<String>("")));
        createEmployee.add(updateid = new DropDownChoice<String>("updateid", new PropertyModel<String>(this, "updateemployeeid"), listEmployeeIds));
        createEmployee.add(new AjaxButton("update_submit") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    Employee employee = controlBeanPort.getEmployee(Integer.valueOf(updateemployeeid));
                    String name = username.getModelObject();
                    String pass = userpassword.getModelObject();
                    int usermode = Integer.valueOf((String) mode.getModelObject());
                    employee.setMode(usermode);
                    employee.setUsername(name);
                    String salt = PasswordUtil.getSalt();
                    employee.setPassword(PasswordUtil.getSHA1SecurePassword(pass, salt));
                    employee.setSalt(salt);
                    controlBeanPort.updateEmployee(employee);
                    info("Munkatárs frissítve!");
                } catch (NumberFormatException e) {
                    info("Hiba, próbálkozzon újra!");
                }
                target.add(listContainer);
            }
        });
        createEmployee.add(new AjaxButton("create_submit") {

            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                String name = username.getModelObject();
                String pass = userpassword.getModelObject();
                int usermode = Integer.valueOf((String) mode.getModelObject());
                String salt = PasswordUtil.getSalt();
                String password = PasswordUtil.getSHA1SecurePassword(pass, salt);
                controlBeanPort.createEmployee(name, password, usermode, salt);
                info("Munkatárs hozzáadva!");
                target.add(listContainer);
            }
        });
        createEmployee.add(new AjaxButton("delete") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (updateemployeeid != null) {
                    try {
                        Integer id = Integer.valueOf(updateemployeeid);
                        Employee employee = controlBeanPort.getEmployee(id);
                        controlBeanPort.removeEmployee(employee);
                        success("Felhasználó törölve");
                    } catch (Exception e) {
                        error("Nem törölhető");
                    }

                } else {
                    info("Adja meg a user ID-t");
                }
                target.add(listContainer);
            }
        });
        listContainer.add(createEmployee);

        IModel listEmployees = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getAllEmployee();
            }
        };

        final ListView<Employee> clientListView = new ListView<Employee>("employees", listEmployees) {

            @Override
            protected void populateItem(ListItem<Employee> paramListItem) {
                final Employee munkatarsak = paramListItem.getModelObject();
                paramListItem.add(new Label("userid", munkatarsak.getEmployeeid()));
                paramListItem.add(new Label("username", munkatarsak.getUsername()));
                paramListItem.add(new Label("mode2", munkatarsak.getMode()));
            }
        };
        listContainer.add(clientListView);

    }
}
