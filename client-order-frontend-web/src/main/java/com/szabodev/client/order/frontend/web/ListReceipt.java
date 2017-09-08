package com.szabodev.client.order.frontend.web;

import com.szabodev.client.order.frontend.utils.UserSession;
import com.szabodev.client.order.frontend.utils.WsService;
import com.szabodev.wsclient.ControlBean;
import com.szabodev.wsclient.Receipt;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.resource.AbstractResourceStreamWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("serial")
public final class ListReceipt extends Layout {

    private DropDownChoice downloadid;

    private FileUploadField uploadFile;
    private DropDownChoice uploadOrderid;

    private int selectedorderid;
    private int selectedreceiptid;

    private DropDownChoice deleteid;
    private String deletereceiptid;

    public ListReceipt(final PageParameters parameters) {

        if (UserSession.getInstance().getCurrentUser().getMode() != 1
                && UserSession.getInstance().getCurrentUser().getMode() != 2) {
            throw new RestartResponseAtInterceptPageException(ErrorPage.class);
        }

        final ControlBean controlBeanPort = WsService.getInstance().getServicePort();

        final WebMarkupContainer listContainer = new WebMarkupContainer("listContainer");
        listContainer.setOutputMarkupId(true);
        add(listContainer);

        listContainer.add(new FeedbackPanel("feedback"));

        IModel listReceiptIds2 = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getReceiptIds();
            }
        };

        IModel listOrdersIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getOrdersIds();
            }
        };
        Form<Receipt> formupload = new Form<Receipt>("formupload") {
            @Override
            protected void onSubmit() {

                boolean done;

                final FileUpload uploadedFile = uploadFile.getFileUpload();
                if (uploadedFile != null) {

                    String filename = uploadedFile.getClientFileName();
                    // String filename = "proba.pdf";
                    byte[] content = uploadedFile.getBytes();
                    done = controlBeanPort.createReceiptForOrder(selectedorderid, content, filename);
                    if (done) {
                        info("Szamla feltoltve!");
                    } else {
                        info("Nem letezo rendeles id");
                    }
                }
            }
        };

        formupload.add(uploadFile = new FileUploadField("uploadFile"));
        formupload.add(uploadOrderid = new DropDownChoice<Integer>("orderid", new PropertyModel(this, "selectedorderid"), listOrdersIds));
        formupload.add(deleteid = new DropDownChoice<Integer>("deleteid", new PropertyModel(this, "deletereceiptid"), listReceiptIds2));
        formupload.add(new AjaxButton("delete") {
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (Integer.valueOf(deletereceiptid) != 0) {

                    try {
                        Integer id = Integer.valueOf(deletereceiptid);
                        Receipt receipt = controlBeanPort.getReceipt2(id);
                        controlBeanPort.removeReceipt(receipt);
                        success("Számla törölve");
                    } catch (Exception e) {
                        error("Nem törölhető");
                    }

                } else {
                    info("Adja meg a számla ID-t");
                }
                target.add(listContainer);
            }
        });
        listContainer.add(formupload);

        IModel listReceipt = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.listReceipt();
            }
        };
        listContainer.add(new ListView<Receipt>("receipts", listReceipt) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<Receipt> item) {
                final Receipt szamlak = item.getModelObject();
                item.add(new Label("receiptid", szamlak.getReceiptid()));
                item.add(new Label("orderid", szamlak.getOrderid()
                        .getOrdersid()));
                item.add(new Label("receipt", szamlak.getFilename()));
            }
        });

        IModel listReceiptIds = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controlBeanPort.getReceiptIds();
            }
        };
        Form<Object> dowloandform = new Form<Object>("dowloandform");
        dowloandform.add(downloadid = new DropDownChoice<Integer>("donwloadid",
                new PropertyModel(this, "selectedreceiptid"), listReceiptIds));
        dowloandform.add(new Button("download") {
            @Override
            public void onSubmit() {
                AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {
                    @Override
                    public void write(OutputStream output) throws IOException {
                        if (controlBeanPort.getReceipt(selectedreceiptid) != null) {
                            output.write(controlBeanPort.getReceipt(selectedreceiptid));
                        } else {
                            info("Nem letezo rendeles id");
                        }
                    }
                };

                String filename = controlBeanPort
                        .getReceipt2(selectedreceiptid).getFilename();
                ResourceStreamRequestHandler handler = new ResourceStreamRequestHandler(
                        rstream, filename);
                getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);

            }
        });
        listContainer.add(dowloandform);

        Link<Void> allDownloadLink = new Link<Void>("allDownload") {
            @Override
            public void onClick() {

                final List<Receipt> receiptList = controlBeanPort.listReceipt();

                AbstractResourceStreamWriter rstream = new AbstractResourceStreamWriter() {

                    @Override
                    public void write(OutputStream output) throws IOException {
                        ZipOutputStream zos = new ZipOutputStream(output);

                        try {
                            for (int i = 0; i < receiptList.size(); i++) {
                                try {
                                    zos.putNextEntry(new ZipEntry(i
                                            + receiptList.get(i).getFilename()));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    zos.write(receiptList.get(i)
                                            .getReceiptfile());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    zos.closeEntry();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } finally {
                            try {
                                zos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                ResourceStreamRequestHandler target = new ResourceStreamRequestHandler(rstream, "szamlak.zip");
                getRequestCycle().scheduleRequestHandlerAfterCurrent(target);

            }
        };
        listContainer.add(allDownloadLink);

    }

}
