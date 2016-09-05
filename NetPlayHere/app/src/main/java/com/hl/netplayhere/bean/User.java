package com.hl.netplayhere.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by yongjiaming on 16-9-5.
 */
public class User extends BmobUser{

    private int score;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
