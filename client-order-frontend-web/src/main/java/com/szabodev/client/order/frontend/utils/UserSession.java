package com.szabodev.client.order.frontend.utils;

import com.szabodev.wsclient.ControlBean;
import com.szabodev.wsclient.Employee;

public class UserSession {

    private final ControlBean controlBeanPort;

    private Employee currentUser;
    private boolean isLoggenIn;

    public UserSession() {
        controlBeanPort = WsService.getInstance().getServicePort();
        reset();
    }

    private static UserSession singleton;

    public static synchronized UserSession getInstance() {
        if (singleton == null) {
            singleton = new UserSession();
        }
        return singleton;
    }

    private void reset() {
        currentUser = new Employee();
        currentUser.setMode(0);
        isLoggenIn = false;

    }

    public boolean login(Employee newEmployee) {
        Employee employee = controlBeanPort.getEmployeeByName(newEmployee.getUsername());
        String password = newEmployee.getPassword();
        if (employee != null && PasswordUtil.getSHA1SecurePassword(password, employee.getSalt()).equals(employee.getPassword())) {
            currentUser = employee;
            isLoggenIn = true;
            return true;
        } else {
            reset();
            return false;
        }

    }

//    public boolean loginSimple(Employee newEmployee) {
//        Employee employee = controlBeanPort.getEmployeeByName(newEmployee.getUsername());
//        String password = newEmployee.getPassword();
//        if (employee != null && password.equals(employee.getPassword())) {
//            currentUser = employee;
//            isLoggenIn = true;
//            return true;
//        } else {
//            reset();
//            return false;
//        }
//
//    }

    public void logout() {
        reset();
    }

    public void setCurrentUser(Employee currentUser) {
        this.currentUser = currentUser;
    }

    public Employee getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggenIn() {
        return isLoggenIn;
    }

    public void setIsLoggenIn(boolean isLoggenIn) {
        this.isLoggenIn = isLoggenIn;
    }

}
