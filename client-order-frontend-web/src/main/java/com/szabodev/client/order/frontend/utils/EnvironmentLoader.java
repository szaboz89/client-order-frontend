package com.szabodev.client.order.frontend.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

public class EnvironmentLoader {

    private static final String PROPERTIES_FILEID = "proba.properties";

    private static Properties properties = new Properties();

    private static EnvironmentLoader singleton;

    public static synchronized EnvironmentLoader getInstance() {
        if (singleton == null) {
            singleton = new EnvironmentLoader();
        }
        return singleton;
    }

    private EnvironmentLoader() {
        loadProperties(properties, PROPERTIES_FILEID);
    }

    String getProperty(String key) {
        return properties.getProperty(key);
    }

    private void loadProperties(Properties prop, String key) {
        Properties sys = System.getProperties();
        for (Enumeration<?> keys = sys.propertyNames(); keys.hasMoreElements(); ) {
            String nk = (String) keys.nextElement();
            if (nk.startsWith(key)) {
                String value = sys.getProperty(nk);
                InputStream in = null;
                try {
                    in = new FileInputStream(value);
                    prop.load(in);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}