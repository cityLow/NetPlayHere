package com.hl.netplayhere;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.hl.netplayhere.util.Constant;

import cn.bmob.v3.Bmob;

/**
 * Created by lining on 16/8/13.
 */
public class MyApplication extends Application {



    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, Constant.APPID);
        SDKInitializer.initialize(getApplicationContext());
    }
}
