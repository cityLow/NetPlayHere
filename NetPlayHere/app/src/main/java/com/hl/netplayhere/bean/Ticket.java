package com.hl.netplayhere.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by yjm on 2016/9/5.
 */
public class Ticket extends BmobObject{

    private String name;
    private int score;
    private BmobFile ticketPic;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public BmobFile getTicketPic() {
        return ticketPic;
    }

    public void setTicketPic(BmobFile ticketPic) {
        this.ticketPic = ticketPic;
    }
}
