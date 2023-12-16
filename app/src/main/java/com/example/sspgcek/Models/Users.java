package com.example.sspgcek.Models;

public class Users {
    String username, mail,password,userId;

    public Users(String username, String mail, String password, String userId) {
        this.username = username;
        this.mail = mail;
        this.password = password;
        this.userId = userId;
    }
    public  Users(){}
    public Users(String username, String mail, String password) {
        this.username = username;
        this.mail = mail;
        this.password = password;
    }
    public Users(String username, String mail) {
        this.username = username;
        this.mail = mail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
