package com.hl.netplayhere.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hl.netplayhere.R;
import com.hl.netplayhere.bean.User;

import cn.bmob.v3.listener.SaveListener;

public class RegisterActivity extends AppCompatActivity {
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    Button registerBtn;
    boolean cancel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registerBtn = (Button) findViewById(R.id.email_sign_in_button);
        registerBtn.setText(R.string.register);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Reset errors.
                mEmailView.setError(null);
                mPasswordView.setError(null);

                // Store values at the time of the login attempt.
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    mEmailView.setError(getString(R.string.error_field_required));
                    cancel = true;
                }

                if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                    mPasswordView.setError(getString(R.string.error_invalid_password));
                    cancel = true;
                }
                if(!cancel){
                    User bu = new User();
                    bu.setUsername(email);
                    bu.setPassword(password);
                    bu.setScore(0);
//                    BmobFile bmobFile = new BmobFile();
//                    bmobFile.setUrl("http://bmob-cdn-5589.b0.upaiyun.com/2016/11/05/19fbe37840490857808583266ff9212d.png");
//                    bu.setAvatar(bmobFile);
                    //注意：不能用save方法进行注册
                    bu.signUp(RegisterActivity.this, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(RegisterActivity.this,"注册成功", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Log.d("yjm",s);
                            Toast.makeText(RegisterActivity.this, "注册失败:" +s, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                cancel = false;
            }
        });

    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }
}
