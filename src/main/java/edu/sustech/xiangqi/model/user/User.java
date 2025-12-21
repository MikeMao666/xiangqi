package edu.sustech.xiangqi.model.user;


import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    private String username;
    private String password;

    @JsonIgnore
    public boolean isGuest;

    //constructor
    public User(){

    }
    public User(String Username, String password){
        this.username = Username;
        this.password = password;
        isGuest = false;
    }

    public static User guest(){
        User Guest = new User();
        Guest.username = "Guest";
        Guest.isGuest = true;
        return Guest;
    }

    //TODO getter&setter   guest can't do!
    public String getUsername(){
        return username;//if
    }public String getPassword(){
        return password;//If null, swing! a window "Guest"
    }
    public void setUsername(String newUsername){
        username = newUsername;
    }
    public void setPassword(String newPassword){
        password = newPassword;
    }

}
