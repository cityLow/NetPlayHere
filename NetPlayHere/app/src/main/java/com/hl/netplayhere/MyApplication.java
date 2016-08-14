package com.hl.netplayhere;

import android.app.Application;

import cn.bmob.v3.Bmob;

/**
 * Created by lining on 16/8/13.
 */
public class MyApplication extends Application {

    public static String APPID ="32f93faddd6fea80b6866f9c90c9def2";

    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this,APPID);
    }
}
