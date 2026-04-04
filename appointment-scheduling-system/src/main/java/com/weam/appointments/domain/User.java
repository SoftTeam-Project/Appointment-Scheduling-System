package com.weam.appointments.domain;

public class User {
   
	private final String username;
    private final String role;
    private final String email;

    public User(String username, String role, String email) {
       
    	this.username = username;
        this.role = role;
        this.email = email;
    }

    public String getUsername() {
    	return username; 
    	}
    public String getRole() {
    	
    	return role;
    	}
    public String getEmail() {
    	return email; 
    	}
}