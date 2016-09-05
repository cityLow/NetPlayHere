package com.hl.netplayhere.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hl.netplayhere.R;
import com.hl.netplayhere.bean.SpotPhoto;

import java.util.List;

/**
 * Created by yongjiaming on 16-9-5.
 */
public class ViewPagerAdapter extends PagerAdapter{

    Context context;
    List<SpotPhoto> spotPhotos;

    public ViewPagerAdapter(Context context, List<SpotPhoto> spotPhotos) {
        this.context = context;
        this.spotPhotos = spotPhotos;
    }

    public List<SpotPhoto> getSpotPhotos() {
        return spotPhotos;
    }

    public void setSpotPhotos(List<SpotPhoto> spotPhotos) {
        this.spotPhotos = spotPhotos;
    }

    @Override
    public int getCount() {
        return spotPhotos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        ImageView imageView = (ImageView) inflater.inflate(R.layout.layout_photo, null);
        container.addView(imageView);
        Glide.with(context).load(spotPhotos.get(position).getPhoto().getFileUrl()).placeholder(R.drawable.huaqinchi)
                                    .crossFade().into(imageView);
        return imageView;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


}
