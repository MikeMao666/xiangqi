package edu.sustech.xiangqi.model.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserManager {
    private static final String USER_FILE = "resources/userdata/users.json";
    private List<User> users;
    ObjectMapper objectMapper;

    //constructor
    public UserManager(){
        this.objectMapper = new ObjectMapper();
        this.users = userLoad();
    }

    public boolean userRegister(String username, String password){
        for (User user : users){
            if (user.getUsername().equals(username))
                return false;
        }users.add(new User(username
        , password));
        userSave();
        return true;
    }
    public User userLogin(String username, String password){
        for (User user : users){
            if (user.getUsername().equals(username) && user.getPassword().equals(password)){
                return user;
            }
        }return null;
    }
    public User guestLogin() {
        return User.guest();
    }

    public List<User> userLoad(){
        File file = new File(USER_FILE);
        if (file.exists()) {
            try {
                return objectMapper.readValue(file, new TypeReference<List<User>>() {});
            } catch (IOException e) {
                System.err.println("加载用户数据失败: " + e.getMessage());
            }
        }return new ArrayList<>();
    }
    public void userSave(){
        try {
            objectMapper.writeValue(new File(USER_FILE), users);
        } catch (IOException e) {
            System.err.println("保存用户数据失败: " + e.getMessage());
        }
    }
}
