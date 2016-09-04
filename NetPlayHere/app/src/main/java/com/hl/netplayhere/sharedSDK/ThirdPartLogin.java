package com.hl.netplayhere.sharedSDK;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.hl.netplayhere.R;
import com.hl.netplayhere.activity.MainActivity;

import java.util.HashMap;

import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;

public class ThirdPartLogin extends AppCompatActivity implements View.OnClickListener {

    View tvWeixin = null;
    View tvWeibo = null;
    View tvQq = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tpl_login_page);

        tvWeixin = findViewById(R.id.tvWeixin);
        tvWeibo = (findViewById(R.id.tvWeibo));
        tvQq = (findViewById(R.id.tvQq));
        tvWeibo.setOnClickListener(this);
        tvQq.setOnClickListener(this);
        tvWeixin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvWeibo:
                login(SinaWeibo.NAME);

            break;
            case R.id.tvWeixin:

                login(Wechat.NAME);
            break;
            case R.id.tvQq:

                login(QQ.NAME);
            break;
            default:
                break;
        }
    }

    private void login(String platformName) {
        LoginApi api = new LoginApi();
        //设置登陆的平台后执行登陆的方法
        api.setPlatform(platformName);
        api.setOnLoginListener(new OnLoginListener() {
            public boolean onLogin(String platform, HashMap<String, Object> res) {
                // 在这个方法填写尝试的代码，返回true表示还不能登录，需要注册
                // 此处全部给回需要注册
                Toast.makeText(ThirdPartLogin.this, "login success!", Toast.LENGTH_SHORT).show();

                Intent intent=new Intent(ThirdPartLogin.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ThirdPartLogin.this.startActivity(intent);
                finish();
                return true;
            }

            public boolean onRegister(UserInfo info) {
                // 填写处理注册信息的代码，返回true表示数据合法，注册页面可以关闭
                return true;
            }
        });
        api.login(this);
    }
}
