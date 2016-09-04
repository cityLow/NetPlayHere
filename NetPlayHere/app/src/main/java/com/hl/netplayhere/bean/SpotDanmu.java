package com.hl.netplayhere.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by yjm on 2016/9/3.
 */
public class SpotDanmu extends BmobObject{

    private String text;
    private String time;
    private Spot spot;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }
}
