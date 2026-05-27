package com.proyecto.capstone.models;

import java.util.Date;

public class User {
    private String uid;
    private String email;
    private String name;
    private String role;
    private Date createdAt;
    private String password;

    public User() {}

    public User(String uid, String email, String name, String role, Date createdAt, String password) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
        this.password = password;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}