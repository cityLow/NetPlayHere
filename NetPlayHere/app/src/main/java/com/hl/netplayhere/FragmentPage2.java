package com.hl.netplayhere;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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
import android.text.Spanned;
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
import com.hl.netplayhere.bean.Score;
import com.hl.netplayhere.bean.Spot;
import com.hl.netplayhere.bean.SpotDanmu;
import com.hl.netplayhere.bean.SpotPhoto;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
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
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser;
import master.flame.danmaku.danmaku.util.IOUtils;

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

    private BmobUser mCurrentUser;

    private Spot mCurrentSpot;
    static List<SpotPhoto> spotPhotoList;
    private static int mCurrentIndex = -1;

    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == -1){
                if(mCurrentIndex == spotPhotoList.size() -1){
                    mCurrentIndex = -1;
                }
                mCurrentIndex++;
                viewPager.setCurrentItem(mCurrentIndex);
            }
        }
    };


    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        private Drawable mDrawable;

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
            if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕
                // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
                new Thread() {

                    @Override
                    public void run() {
                        String url = "http://www.bilibili.com/favicon.ico";
                        InputStream inputStream = null;
                        Drawable drawable = mDrawable;
                        if (drawable == null) {
                            try {
                                URLConnection urlConnection = new URL(url).openConnection();
                                inputStream = urlConnection.getInputStream();
                                drawable = BitmapDrawable.createFromStream(inputStream, "bitmap");
                                mDrawable = drawable;
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                IOUtils.closeQuietly(inputStream);
                            }
                        }
                        if (drawable != null) {
                            drawable.setBounds(0, 0, 100, 100);
                            SpannableStringBuilder spannable = createSpannable(drawable);
                            danmaku.text = spannable;
                            if (mDanmakuView != null) {
                                mDanmakuView.invalidateDanmaku(danmaku, false);
                            }
                            return;
                        }
                    }
                }.start();
            }
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
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

        mCurrentUser = new BmobUser();
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("currentUser", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", "");
        mCurrentUser.setObjectId(userId);



        mCurrentSpot = new Spot();
        mCurrentSpot.setObjectId("QC4PZZZd");
        loadDanmu();


        BmobQuery<SpotPhoto> bmobQuery = new BmobQuery<>();
        bmobQuery.addWhereEqualTo("spot", mCurrentSpot);
        bmobQuery.findObjects(new FindListener<SpotPhoto>() {
            @Override
            public void done(List<SpotPhoto> list, BmobException e) {
                spotPhotoList = list;
                pagerAdapter = new ViewPagerAdapter(getContext(), list);
                viewPager.setAdapter(pagerAdapter);
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(-1);
                    }
                }, 4000, 4000);
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
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
        if(timer != null)
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
            Cursor cursor = contentResolver.query(data.getData(), new String[]{MediaStore.Images.Media.DATA}, null ,null, null);
            if(cursor != null){
                if(cursor.moveToFirst()){
                    path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                }
                cursor.close();
            }
            if(path != null){
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
                                Score score = new Score();
                                score.setUser(mCurrentUser);
                                score.setScore(score.getScore() + 2);
                                score.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if(e == null){
                                            Log.d("yjm", "update score success");
                                        } else{
                                            Log.d("yjm", "update score fail " + e.getMessage());
                                        }

                                    }
                                });
                            }
                        });
                    }
                });

                //更新滚动图片
                BmobQuery<SpotPhoto> bmobQuery = new BmobQuery<>();
                bmobQuery.addWhereEqualTo("spot", mCurrentSpot);
                bmobQuery.findObjects(new FindListener<SpotPhoto>() {
                    @Override
                    public void done(List<SpotPhoto> list, BmobException e) {
                        spotPhotoList = list;
                        pagerAdapter.setSpotPhotos(list);
                        pagerAdapter.notifyDataSetChanged();
                    }
                });



            }

        }
        super.onActivityResult(requestCode, resultCode, data);
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
                //spotDanmu.setTime("0");
                spotDanmu.setTime(mDanmakuView.getCurrentTime() + 1200 + "");
                addSpotDanmaku(spotDanmu);
                spotDanmu.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        Toast.makeText(getContext(), "save result: " + s, Toast.LENGTH_SHORT).show();
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
                        if (e == null) {
                            mCurrentSpot = list.get(0);
                            mSpotTv.setText(mCurrentSpot.getName());
//                            Glide.with(getContext()).load(mCurrentSpot.getPicture().getFileUrl()).placeholder(R.drawable.huaqinchi)
//                                    .crossFade().into(mSpotBgIv);
                            loadDanmu();
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
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
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
                for (SpotDanmu spotDanmu : list) {
                    addSpotDanmaku(spotDanmu);
                }
            }
        });
    }


    private void addSpotDanmaku(SpotDanmu spotDanmu) {
        BaseDanmaku danmaku = mContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        danmaku.text = spotDanmu.getText();
        danmaku.padding = 5;
        danmaku.priority = 1;
        danmaku.isLive = true;
        //danmaku.time = (long)(Float.parseFloat(spotDanmu.getTime()) * 1000.0F);
        danmaku.time = mDanmakuView.getCurrentTime() + 1200;
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
