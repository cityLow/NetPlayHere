package com.hl.netplayhere.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hl.netplayhere.R;
import com.hl.netplayhere.bean.HotSpot;
import java.util.List;


/**
 * Created by yjm on 2016/8/28.
 */
public class SpotAdapter extends BaseAdapter {
    List<HotSpot> list;
    Context context;
    LayoutInflater layoutInflater;

    public SpotAdapter(Context context, List<HotSpot> list) {
        this.list = list;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.layout_spot_item, null);
            viewHolder = new ViewHolder();
            viewHolder.addressTv = (TextView) convertView.findViewById(R.id.spot_address);
            viewHolder.nameTv = (TextView) convertView.findViewById(R.id.spot_name);
            viewHolder.descriptionTv = (TextView) convertView.findViewById(R.id.spot_description);
            viewHolder.spotIv = (ImageView) convertView.findViewById(R.id.spot_iv);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        HotSpot hotSpot = list.get(position);
        viewHolder.nameTv.setText(hotSpot.getName());
        viewHolder.addressTv.setText("地址:"+hotSpot.getAddress());
        viewHolder.descriptionTv.setText(hotSpot.getDescription());
        Glide.with(context).load(hotSpot.getPicture().getFileUrl(context))
                .centerCrop().placeholder(R.mipmap.ic_launcher)
                .crossFade().into(viewHolder.spotIv);
        Log.d("yjm", "picture url : " + hotSpot.getPicture().getFileUrl(context));
        return convertView;
    }

    class ViewHolder{
        TextView nameTv;
        TextView addressTv;
        TextView descriptionTv;
        ImageView spotIv;
    }
}
