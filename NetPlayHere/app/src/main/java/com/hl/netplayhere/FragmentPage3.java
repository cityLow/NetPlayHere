package com.hl.netplayhere;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hl.netplayhere.adapter.TicketAdapter;
import com.hl.netplayhere.bean.Ticket;
import com.hl.netplayhere.bean.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class FragmentPage3 extends Fragment{

	private ListView mListView;
	private TextView mScoreTv;
	private Button mSignBtn;
	private User mCurrentUser;

	private String mCurrentDate;
	SimpleDateFormat simpleDateFormat;
	private TicketAdapter ticketAdapter;
	private String userId;
	private boolean mFlag;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		mCurrentDate = simpleDateFormat.format(new Date());


		SharedPreferences sharedPreferences = getContext().getSharedPreferences("currentUser", Context.MODE_PRIVATE);
		userId = sharedPreferences.getString("userId", "");

		sharedPreferences = getContext().getSharedPreferences("signRecord", Context.MODE_PRIVATE);
		String signDate = sharedPreferences.getString("signDate", "");

		if(signDate.equals("") || !signDate.equals(mCurrentDate)){
			mFlag = true;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_3, null);
		mListView = (ListView) view.findViewById(R.id.ticketList);
		mScoreTv = (TextView) view.findViewById(R.id.userScoreTv);
		mSignBtn = (Button) view.findViewById(R.id.signBtn);



		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		BmobQuery<User> bmobQuery = new BmobQuery<>();
		bmobQuery.addWhereEqualTo("username", userId);
		bmobQuery.findObjects(new FindListener<User>() {
			@Override
			public void done(List<User> list, BmobException e) {
				if(e == null){
					mCurrentUser = list.get(0);
					mScoreTv.setText("当前积分:" + mCurrentUser.getScore() + "(" + mCurrentUser.getUsername() + ")");
				}
			}
		});




		mSignBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mFlag){
					if(mCurrentUser == null){
						mCurrentUser = new User();
						mCurrentUser.setUsername(userId);
					}

					mCurrentUser.setScore(mCurrentUser.getScore() + 3);
					mCurrentUser.update(new UpdateListener() {
						@Override
						public void done(BmobException e) {
							if(e == null){
								Toast.makeText(getContext(), "签到成功,积分+3", Toast.LENGTH_SHORT).show();
								mScoreTv.setText("当前积分:" + mCurrentUser.getScore() + "(" + mCurrentUser.getUsername() + ")");

								SharedPreferences sharedPreferences = getContext().getSharedPreferences("signRecord", Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = sharedPreferences.edit();
								editor.putString("signDate", mCurrentDate);
								editor.apply();

								mFlag = false;
							}
						}
					});

				} else{
					Toast.makeText(getContext(), "您今天已签到！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		final BmobQuery<Ticket> ticketBmobQuery = new BmobQuery<>();
		ticketBmobQuery.findObjects(new FindListener<Ticket>() {
			@Override
			public void done(List<Ticket> list, BmobException e) {
				if(e == null){
					ticketAdapter = new TicketAdapter(getContext(), list, mCurrentUser);
					mListView.setAdapter(ticketAdapter);
				}
			}
		});

	}
}