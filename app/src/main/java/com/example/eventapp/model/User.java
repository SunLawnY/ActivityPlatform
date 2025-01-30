package com.example.eventapp.model;

public class User {
    private long id;
    private String username;
    private String password;
    private String email;
    private boolean isStaff;

    public User() {
    }

    public User(String username, String password, String email, boolean isStaff) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.isStaff = isStaff;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isStaff() {
        return isStaff;
    }

    public void setStaff(boolean staff) {
        isStaff = staff;
    }
} 