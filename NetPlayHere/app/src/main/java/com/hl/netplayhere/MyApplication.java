package com.hl.netplayhere;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.Trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;

import cn.bmob.newim.BmobIM;


/**
 * Created by lining on 16/8/13.
 */
public class MyApplication extends Application {
    private Context mContext = null;
    /**
     * 轨迹服务
     */
    private Trace trace = null;

    /**
     * 轨迹服务客户端
     */
    private LBSTraceClient client = null;

    /**
     * 鹰眼服务ID，开发者创建的鹰眼服务对应的服务ID
     */
    private int serviceId = 128030;

    /**
     * entity标识
     */
    private String entityName = "myTrace";

    /**
     * 轨迹服务类型（0 : 不建立socket长连接， 1 : 建立socket长连接但不上传位置数据，2 : 建立socket长连接并上传位置数据）
     */
    private int traceType = 2;

    private MapView bmapView = null;

    private BaiduMap mBaiduMap = null;

    private TrackHandler mHandler = null;

    private static MyApplication INSTANCE;
    public static MyApplication INSTANCE(){
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        INSTANCE = this;
        SDKInitializer.initialize(this);
        // 初始化轨迹服务客户端
        client = new LBSTraceClient(mContext);
        // 初始化轨迹服务
        trace = new Trace(mContext, serviceId, entityName, traceType);
        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);
        mHandler = new TrackHandler(this);


        //只有主进程运行的时候才需要初始化
        if (getApplicationInfo().packageName.equals(getMyProcessName())){
            //im初始化
            BmobIM.init(this);
            //注册消息接收器
            BmobIM.registerDefaultMessageHandler(new DemoMessageHandler(this));
        }
    }

    public void initBmap(MapView bmapView) {
        this.bmapView = bmapView;
        this.mBaiduMap = bmapView.getMap();
        this.bmapView.showZoomControls(false);
    }

    static class TrackHandler extends Handler {
        WeakReference<MyApplication> trackApp;

        TrackHandler(MyApplication trackApplication) {
            trackApp = new WeakReference<>(trackApplication);
        }

        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(trackApp.get().mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
        }
    }

    public Context getmContext() {
        return mContext;
    }

    public Trace getTrace() {
        return trace;
    }

    public LBSTraceClient getClient() {
        return client;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getEntityName() {
        return entityName;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public MapView getBmapView() {
        return bmapView;
    }

    public BaiduMap getmBaiduMap() {
        return mBaiduMap;
    }


    /**
     * 获取当前运行的进程名
     * @return
     */
    public static String getMyProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
