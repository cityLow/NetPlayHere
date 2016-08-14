package com.hl.netplayhere.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by lining on 16/8/13.
 */
public class Person extends BmobObject {
    private String name;
    private String address;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}


