package com.ibs.interview_scheduler.context;

public class UserContext {
    private static final ThreadLocal<String> userName = new ThreadLocal<>();
    private static final ThreadLocal<String> userRole = new ThreadLocal<>();

    public static void setUserName(String name) {
        userName.set(name);
    }
    public static void setUserRole(String role) {
        userRole.set(role);
    }
    public static String getUserName() {
        return userName.get();
    }
    public static String getUserRole() {
        return userRole.get();
    }
    public static void clear() {
        userName.remove();
        userRole.remove();
    }
}
