package com.hl.netplayhere;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hl.netplayhere.adapter.ViewPagerAdapter;
import com.hl.netplayhere.bean.Spot;
import com.hl.netplayhere.bean.SpotDanmu;
import com.hl.netplayhere.bean.SpotPhoto;
import com.hl.netplayhere.bean.User;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser;

public class FragmentPage2 extends Fragment implements View.OnClickListener {
    private IDanmakuView mDanmakuView;
    private BaseDanmakuParser mParser;
    private DanmakuContext mContext;
    ILoader mLoader;

    //    private ImageView mSpotBgIv;
    private Button mSendBtn;
    private EditText mEditText;
    private EditText mSearchEt;
    private Button mSearchBtn;
    private ImageView mDeleteIv;
    private TextView mSpotTv;
    private FloatingActionButton mFloatBtn;
    private static ViewPager viewPager;
    ViewPagerAdapter pagerAdapter;
    private Timer timer;

    private User mCurrentUser;

    private Spot mCurrentSpot;
    private static List<SpotPhoto> spotPhotoList;
    private static int mCurrentIndex = -1;

    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == -1) {
                if (mCurrentIndex == spotPhotoList.size() - 1) {
                    mCurrentIndex = -1;
                }
                mCurrentIndex++;
                viewPager.setCurrentItem(mCurrentIndex);
            }
        }
    };

    private BaseDanmakuParser createParser(InputStream stream) {
        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }
        try {
            mLoader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new BiliDanmukuParser();
        IDataSource<?> dataSource = mLoader.getDataSource();
        parser.load(dataSource);
        return parser;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2, null);
        findViews(view);
        return view;
    }


    private void findViews(View view) {
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);

        mDanmakuView = (IDanmakuView) view.findViewById(R.id.sv_danmaku);
        mContext = DanmakuContext.create();
        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);
//        mContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3).setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
//                .setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
////        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
//                .setMaximumLines(maxLinesPair)
//                .preventOverlapping(overlappingEnablePair);
        if (mDanmakuView != null) {
            mLoader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);
            //mParser = createParser(this.getResources().openRawResource(R.raw.comments2));
            mParser = createParser(null);
            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
                }

                @Override
                public void prepared() {
                    mDanmakuView.start();
                }
            });
            mDanmakuView.prepare(mParser, mContext);
            mDanmakuView.enableDanmakuDrawingCache(true);
        }

        mSendBtn = (Button) view.findViewById(R.id.sendBtn);
        //mSpotBgIv = (ImageView) view.findViewById(R.id.spot_img);
        mEditText = (EditText) view.findViewById(R.id.danmuEditText);
        mSendBtn.setOnClickListener(this);

        mSearchEt = (EditText) view.findViewById(R.id.etSearch);
        mSearchBtn = (Button) view.findViewById(R.id.btnSearch);
        mDeleteIv = (ImageView) view.findViewById(R.id.ivDeleteText);
        mSpotTv = (TextView) view.findViewById(R.id.spotName);
        mSearchBtn.setOnClickListener(this);
        mDeleteIv.setOnClickListener(this);
        mSearchEt.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mDeleteIv.setVisibility(View.GONE);
                } else {
                    mDeleteIv.setVisibility(View.VISIBLE);
                }
            }
        });
        mFloatBtn = (FloatingActionButton) view.findViewById(R.id.floatingBtn);
        mFloatBtn.setOnClickListener(this);

        viewPager = (ViewPager) view.findViewById(R.id.viewpager);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("yjm", "fragment2 onViewCreated");

        mCurrentUser = new User();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("currentUser", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        mCurrentUser.setObjectId(userId);

        BmobQuery<User> bmobQuery1 = new BmobQuery<>();
        bmobQuery1.addWhereEqualTo("username", userId);
        bmobQuery1.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if(e == null){
                    mCurrentUser = list.get(0);
                    Log.d("yjm", mCurrentUser.getUsername());
                }
            }
        });

        mCurrentSpot = new Spot();
        mCurrentSpot.setObjectId("QC4PZZZd");
        loadDanmu();


        BmobQuery<SpotPhoto> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("spot", mCurrentSpot);
        bmobQuery.findObjects(new FindListener<SpotPhoto>() {
            @Override
            public void done(List<SpotPhoto> list, BmobException e) {
                spotPhotoList = list;
                pagerAdapter = new ViewPagerAdapter(getActivity(), list);
                viewPager.setAdapter(pagerAdapter);
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("timer", "send a message");
                        handler.sendEmptyMessage(-1);
                    }
                }, 2000, 6000);
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
        if (timer != null)
            timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            mDanmakuView.resume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
        if (timer != null)
            timer.cancel();
    }

    public void onBackPressed() {
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Bitmap cameraBitmap = (Bitmap) data.getExtras().get("data");
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("yjm", "photo uri: " + data.getData());
            String path = null;
            ContentResolver contentResolver = getContext().getContentResolver();
            Cursor cursor = contentResolver.query(data.getData(), new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                }
                cursor.close();
            }
            if (path != null) {
                BmobFile bmobFile = new BmobFile(new File(path));

                final SpotPhoto spotPhoto = new SpotPhoto();
                spotPhoto.setUser(mCurrentUser);
                spotPhoto.setSpot(mCurrentSpot);
                spotPhoto.setPhoto(bmobFile);
                bmobFile.upload(new UploadFileListener() {
                    @Override
                    public void done(BmobException e) {
                        spotPhoto.save(new SaveListener<String>() {
                            @Override
                            public void done(String s, BmobException e) {
                                Log.d("yjm", "发表图片成功，积分+2");
                                Toast.makeText(getContext(), "发表图片成功，积分+2", Toast.LENGTH_SHORT).show();
                                mCurrentUser.setScore(mCurrentUser.getScore() + 2);
                                mCurrentUser.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if (e == null) {
                                            Log.d("yjm", "update score success");
                                        } else {
                                            Log.d("yjm", "update score fail " + e.getMessage());
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

                notifyViewpager();

            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void notifyViewpager() {
        //更新滚动图片
        BmobQuery<SpotPhoto> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("spot", mCurrentSpot);
        bmobQuery.findObjects(new FindListener<SpotPhoto>() {
            @Override
            public void done(List<SpotPhoto> list, BmobException e) {
                if (list == null || list.size() <= 0) {
                    return;
                }
                spotPhotoList = list;
                pagerAdapter.setSpotPhotos(list);
                pagerAdapter.notifyDataSetChanged();
                mCurrentIndex = -1;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("timer", "send a message");
                        handler.sendEmptyMessage(-1);
                    }
                }, 2000, 4000);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (mDanmakuView == null || !mDanmakuView.isPrepared())
            return;
        switch (v.getId()) {
            case R.id.sendBtn:
                String text = mEditText.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(getContext(), "弹幕内容为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                SpotDanmu spotDanmu = new SpotDanmu();
                spotDanmu.setText(text);
                spotDanmu.setTime(mDanmakuView.getCurrentTime() + 1200 + "");
                spotDanmu.setSpot(mCurrentSpot);
                addSpotDanmaku(spotDanmu);
                spotDanmu.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            Toast.makeText(getContext(), "弹幕发送成功，积分+1", Toast.LENGTH_SHORT).show();
                            mCurrentUser.setScore(mCurrentUser.getScore() + 1);
                            mCurrentUser.update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if (e == null) {
                                        Log.d("yjm", "update score success +1");
                                    } else {
                                        Log.d("yjm", "update score fail +1" + e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                });
                mEditText.getText().clear();
                break;
            case R.id.btnSearch:
                BmobQuery<Spot> query = new BmobQuery<>();
                query.addWhereContains("name", mSearchEt.getText().toString());
                query.findObjects(new FindListener<Spot>() {
                    @Override
                    public void done(List<Spot> list, BmobException e) {
                        if (e == null && list != null && list.size() > 0) {
                            Toast.makeText(getContext(), "搜索成功", Toast.LENGTH_SHORT).show();
                            timer.cancel();
                            mCurrentSpot = list.get(0);
                            mSpotTv.setText(mCurrentSpot.getName());
                            notifyViewpager();
                            //清除当前的弹幕
                            mDanmakuView.clearDanmakusOnScreen();
                            loadDanmu();
                        } else{
                            Toast.makeText(getContext(), "抱歉，找不到您输入的景点信息！", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.ivDeleteText:
                mSearchEt.getText().clear();
                break;
            case R.id.floatingBtn:
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "请选择一张照片上传"), 0);
                break;
            default:
                break;
        }
    }

    private void loadDanmu() {
        BmobQuery<SpotDanmu> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("spot", mCurrentSpot);
        bmobQuery.findObjects(new FindListener<SpotDanmu>() {
            @Override
            public void done(List<SpotDanmu> list, BmobException e) {
                if (list == null) {
                    return;
                }

                for (SpotDanmu spotDanmu : list) {
                    addSpotDanmaku(spotDanmu);
                }
            }
        });
    }


    private void addSpotDanmaku(SpotDanmu spotDanmu) {
        //如果已切换显示景点，则剩余的弹幕不再添加
        if(!spotDanmu.getSpot().getObjectId().equals(mCurrentSpot.getObjectId()))
            return;
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        danmaku.text = spotDanmu.getText();
        danmaku.padding = 5;
        danmaku.priority = 1;
        danmaku.isLive = true;
        //danmaku.time = (long)(Float.parseFloat(spotDanmu.getTime()) * 1000.0F);
        danmaku.time = Long.valueOf(spotDanmu.getTime());
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = Color.WHITE;
        // danmaku.underlineColor = Color.GREEN;
        //danmaku.borderColor = Color.GREEN;
        mDanmakuView.addDanmaku(danmaku);
    }

    private void addDanmaku(boolean islive) {
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        // for(int i=0;i<100;i++){
        // }
        danmaku.text = "这是一条弹幕" + System.nanoTime();
        danmaku.padding = 5;
        danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = islive;
        danmaku.time = mDanmakuView.getCurrentTime() + 1200;
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = Color.WHITE;
        // danmaku.underlineColor = Color.GREEN;
        danmaku.borderColor = Color.GREEN;
        mDanmakuView.addDanmaku(danmaku);

    }

    private void addDanmaKuShowTextAndImage(boolean islive) {
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
        drawable.setBounds(0, 0, 100, 100);
        SpannableStringBuilder spannable = createSpannable(drawable);
        danmaku.text = spannable;
        danmaku.padding = 5;
        danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = islive;
        danmaku.time = mDanmakuView.getCurrentTime() + 1200;
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
        danmaku.underlineColor = Color.GREEN;
        mDanmakuView.addDanmaku(danmaku);
    }

    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        ImageSpan span = new ImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("图文混排");
        spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }
}
