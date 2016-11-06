package com.hl.netplayhere.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hl.netplayhere.R;
import com.hl.netplayhere.bean.User;
import com.hl.netplayhere.util.Utils;

import java.io.File;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UploadFileListener;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;

public class EditAvatarActivity extends AppCompatActivity {

    private ImageView avatorIv;
    private User mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActionBar();

        setContentView(R.layout.activity_edit_avator);
        avatorIv = (ImageView) findViewById(R.id.avatar_iv);
        mCurrentUser = (User) getIntent().getSerializableExtra("currentUser");
        Glide.with(this).load(mCurrentUser.getAvatar() == null ? R.mipmap.ic_launcher : mCurrentUser.getAvatar().getFileUrl(this))
                .placeholder(R.mipmap.ic_launcher).into(avatorIv);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_avatar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action0){
            Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            innerIntent.setType("image/*"); // 查看类型
            startActivityForResult(innerIntent, 0);
        } else{
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Glide.with(this).load(picturePath)
                    .placeholder(R.mipmap.ic_launcher).into(avatorIv);

            Luban.get(this)                     // initialization of Luban
                    .load(new File(picturePath))                     // set the image file to compress
                    .putGear(Luban.THIRD_GEAR)      // set the compress mode, default is : THIRD_GEAR
                    .launch(new OnCompressListener() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess(File file) throws Exception {
                            Log.d("yjm", "after compress:" + Utils.getFileSize(file));

                            BmobFile bmobFile = new BmobFile(file);
                            mCurrentUser.setAvatar(bmobFile);
                            bmobFile.upload(EditAvatarActivity.this, new UploadFileListener() {
                                @Override
                                public void onSuccess() {
                                    mCurrentUser.update(EditAvatarActivity.this);
                                    Toast.makeText(EditAvatarActivity.this, "图像更新成功", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(int i, String s) {
                                    Toast.makeText(EditAvatarActivity.this, "图像更新失败：" + s, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    private void initActionBar(){
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


}
