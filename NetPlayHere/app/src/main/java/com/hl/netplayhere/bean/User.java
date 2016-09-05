package com.hl.netplayhere.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by yongjiaming on 16-9-5.
 */
public class User extends BmobUser{

    private int score;
    private String username;
    private String password;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }
}
