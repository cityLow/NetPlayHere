package com.hl.netplayhere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hl.netplayhere.R;
import com.hl.netplayhere.bean.Ticket;
import com.hl.netplayhere.bean.User;

import java.util.List;

/**
 * Created by yjm on 2016/9/5.
 */
public class TicketAdapter extends BaseAdapter{

    List<Ticket> list;
    Context context;
    LayoutInflater layoutInflater;
    private User currentUser;

    public TicketAdapter(Context context, List<Ticket> list, User currentUser) {
        this.list = list;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.currentUser = currentUser;
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
            convertView = layoutInflater.inflate(R.layout.layout_ticket_item, null);
            viewHolder = new ViewHolder();
            viewHolder.nameTv = (TextView) convertView.findViewById(R.id.ticket_name);
            viewHolder.scoreTv = (TextView) convertView.findViewById(R.id.ticket_score);
            viewHolder.picIv = (ImageView) convertView.findViewById(R.id.ticket_pic);
            viewHolder.purchaseBtn = (Button) convertView.findViewById(R.id.purchase_btn);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Ticket ticket = list.get(position);

        viewHolder.purchaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) {
                    return;
                }
                int yourCurrentScore = currentUser.getScore();
                int ticketScore = ticket.getScore();
                if (yourCurrentScore >= ticketScore) {
                    Toast.makeText(context, "兑换成功", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context, "当前积分不足", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewHolder.nameTv.setText(ticket.getName());
        viewHolder.scoreTv.setText(ticket.getScore()+"积分");
        Glide.with(context).load(ticket.getTicketPic().getFileUrl(context))
                .centerCrop().placeholder(R.mipmap.ic_launcher)
                .crossFade().into(viewHolder.picIv);
        return convertView;
    }

    class ViewHolder{
        TextView nameTv;
        TextView scoreTv;
        ImageView picIv;
        Button purchaseBtn;
    }
}
