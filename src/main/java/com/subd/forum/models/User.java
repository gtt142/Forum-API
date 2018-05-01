package com.subd.forum.models;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {
    private Integer id;
    private String  nickname;
    private String  email;
    private String  about;
    private String  fullname;

    public User() {
    }

    public User(Integer id, String nickname, String email, String fullname, String about) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.fullname = fullname;
        this.about = about;
    }

    public User(String nickname) {
        this.nickname = nickname;
    }

    public Boolean filled() {
        if (this.email != null && this.nickname !=null && this.fullname != null) {
            return true;
        }
        return false;
    }

    public Boolean valid() {
        Pattern pattern = Pattern.compile("^([a-zA-Z0-9_-]+\\.)*[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*\\.[a-zA-Z]{2,6}$");
        Matcher matcher = pattern.matcher(this.email);
        boolean isEmail = matcher.matches();

        if(isEmail) {
            return true;
        }
        return false;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

}
