package com.hl.netplayhere.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.trace.OnStartTraceListener;
import com.hl.netplayhere.FragmentPage1;
import com.hl.netplayhere.FragmentPage2;
import com.hl.netplayhere.FragmentPage3;
import com.hl.netplayhere.MonitorService;
import com.hl.netplayhere.MyApplication;
import com.hl.netplayhere.R;
import com.hl.netplayhere.sensitive.SimpleKWSeekerProcessor;
import com.hl.netplayhere.util.Constant;
import com.hl.netplayhere.util.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static Context mAppContext;
    private static MyApplication trackApp;

    /**
     * 开启轨迹服务监听器
     */
    protected static OnStartTraceListener startTraceListener = null;

    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == -1){
                //加载敏感词
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SimpleKWSeekerProcessor.newInstance(mAppContext);
                    }
                }).start();
                if (null == startTraceListener) {
                    initOnStartTraceListener();
                }
                trackApp.getClient().setInterval(Constant.gatherInterval,Constant.packInterval);
                trackApp.getClient().setProtocolType(0);
                //启动轨迹上传
                trackApp.getClient().startTrace(trackApp.getTrace(),startTraceListener);
                MonitorService.isCheck = true;
                trackApp.startService( new Intent(trackApp,
                        com.hl.netplayhere.MonitorService.class));
            }
        }
    };


    //定义FragmentTabHost对象
    private FragmentTabHost mTabHost;

    //定义一个布局
    private LayoutInflater layoutInflater;

    //定义数组来存放Fragment界面
    private Class fragmentArray[] = {FragmentPage1.class, FragmentPage2.class, FragmentPage3.class};

    //定义数组来存放按钮图片
    private int mImageViewArray[] = {R.drawable.tab_bg1, R.drawable.tab_bg2, R.drawable.tab_bg3};

    //Tab选项卡的文字
    private String mTextviewArray[] = {"景点导航","拍照弹幕", "积分商城"};

    private long time;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_tab_layout);
        initView();
        trackApp = (MyApplication) getApplication();
        mAppContext = getApplicationContext();
        handler.sendEmptyMessageDelayed(-1, 2000);
    }

    /**
     * 初始化组件
     */
    private void initView() {
        //实例化布局对象
        layoutInflater = LayoutInflater.from(this);

        //实例化TabHost对象，得到TabHost
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        //得到fragment的个数
        int count = fragmentArray.length;

        for (int i = 0; i < count; i++) {
            //为每一个Tab按钮设置图标、文字和内容
            TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
            //将Tab按钮添加进Tab选项卡中
            mTabHost.addTab(tabSpec, fragmentArray[i], null);
            //设置Tab按钮的背景
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.selector_tab_background);
        }
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Constant.isMapNeedReload = true;
                if(!tabId.equals(mTextviewArray[1])){
                    FragmentPage2 fragmentPage = (FragmentPage2) getSupportFragmentManager().findFragmentByTag(mTextviewArray[1]);
                    if(fragmentPage != null)
                        fragmentPage.onBackPressed();
                }
            }
        });
    }

    /**
     * 给Tab按钮设置图标和文字
     */
    private View getTabItemView(int index) {
        View view = layoutInflater.inflate(R.layout.tab_item_view, null);

        ImageView imageView = (ImageView) view.findViewById(R.id.imageview);
        imageView.setImageResource(mImageViewArray[index]);

        TextView textView = (TextView) view.findViewById(R.id.textview);
        textView.setText(mTextviewArray[index]);

        return view;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentPage1 fragmentPage1 = (FragmentPage1) getSupportFragmentManager().findFragmentByTag(mTextviewArray[0]);
        switch (item.getItemId()){
            case R.id.action1:
                //Fragment fragment = TrackUploadFragment.newInstance((MyApplication) getApplication());
//                getSupportFragmentManager().beginTransaction().replace(R.id.realtabcontent,fragment , mTextviewArray[0]).commit();
                fragmentPage1.startRefreshThread(true);
                fragmentPage1.queryHistoryTrack(0, null);
                return true;
            case R.id.action2:
                fragmentPage1.searchNearby();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 初始化OnStartTraceListener
     */
    private static void initOnStartTraceListener() {
        // 初始化startTraceListener
        startTraceListener = new OnStartTraceListener() {

            // 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            public void onTraceCallback(int arg0, String arg1) {
                // TODO Auto-generated method stub
                Log.d("yjm trace", "开启轨迹服务回调接口消息 [消息编码 : " + arg0 + "，消息内容 : " + arg1 + "]");
                //mHandler.obtainMessage(arg0, "开启轨迹服务回调接口消息 [消息编码 : " + arg0 + "，消息内容 : " + arg1 + "]").sendToTarget();
            }

            // 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            public void onTracePushCallback(byte arg0, String arg1) {
                // TODO Auto-generated method stub
                if (0x03 == arg0 || 0x04 == arg0) {
                    try {
                        JSONObject dataJson = new JSONObject(arg1);
                        if (null != dataJson) {
                            String mPerson = dataJson.getString("monitored_person");
                            String action = dataJson.getInt("action") == 1 ? "进入" : "离开";
                            String date = DateUtils.getDate(dataJson.getInt("time"));
                            long fenceId = dataJson.getLong("fence_id");
//                            mHandler.obtainMessage(-1,
//                                    "监控对象[" + mPerson + "]于" + date + " [" + action + "][" + fenceId + "号]围栏")
//                                    .sendToTarget();
                        }

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
//                        mHandler.obtainMessage(-1, "轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]")
//                                .sendToTarget();
                    }
                } else {
                    //mHandler.obtainMessage(-1, "轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]").sendToTarget();
                }
            }

        };
    }


    @Override
    public void onBackPressed() {
            if(System.currentTimeMillis() - time <= 2000){
                super.onBackPressed();
            } else{
                Toast.makeText(MainActivity.this, "再次返回退出应用！", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
        }
    }
}


