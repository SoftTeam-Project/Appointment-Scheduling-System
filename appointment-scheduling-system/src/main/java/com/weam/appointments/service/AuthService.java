package com.weam.appointments.service;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private Map<String, String> users = new HashMap<>();

    public AuthService() {
      
        users.put("admin", "admin123");
        users.put("student", "stud123");
    }

    public boolean login(String username, String password) {
        if (!users.containsKey(username)) {
            return false;
        }
        return users.get(username).equals(password);
    }

    public void logout(String username) {
      
        System.out.println(username + " logged out");
    }
}