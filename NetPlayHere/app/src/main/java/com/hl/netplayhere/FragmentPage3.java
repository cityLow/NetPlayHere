package com.hl.netplayhere;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hl.netplayhere.activity.EditAvatarActivity;
import com.hl.netplayhere.activity.MainActivity;
import com.hl.netplayhere.adapter.TicketAdapter;
import com.hl.netplayhere.bean.Ticket;
import com.hl.netplayhere.bean.User;
import com.hl.netplayhere.util.GlideCircleTransform;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class FragmentPage3 extends Fragment{

	private ListView mListView;
	private TextView mScoreTv;
	private TextView mUsernameTv;
	private Button mSignBtn;
	private User mCurrentUser;
	private ImageView avatorIv;

	private String mCurrentDate;
	SimpleDateFormat simpleDateFormat;
	private TicketAdapter ticketAdapter;
	private String userId;
	private boolean mFlag;

	private View rootView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		mCurrentDate = simpleDateFormat.format(new Date());


		SharedPreferences sharedPreferences = getContext().getSharedPreferences("currentUser", Context.MODE_PRIVATE);
		userId = sharedPreferences.getString("userId", "");

		sharedPreferences = getContext().getSharedPreferences("signRecord", Context.MODE_PRIVATE);
		String signDate = sharedPreferences.getString("signDate" + userId, "");

		if(signDate.equals("") || !signDate.equals(mCurrentDate)){
			mFlag = true;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		if (rootView == null)
		{
			rootView = inflater.inflate(R.layout.fragment_3, null);
		}
		// 缓存的rootView需要判断是否已经被加过parent，如果有parent需要从parent删除，要不然会发生这个rootview已经有parent的错误。
		ViewGroup parent = (ViewGroup) rootView.getParent();
		if (parent != null)
		{
			parent.removeView(rootView);
		}
		mListView = (ListView) rootView.findViewById(R.id.ticketList);
		mScoreTv = (TextView) rootView.findViewById(R.id.scoreTv);
		mUsernameTv = (TextView) rootView.findViewById(R.id.userNameTv);
		mSignBtn = (Button) rootView.findViewById(R.id.signBtn);
		avatorIv = (ImageView) rootView.findViewById(R.id.avatar_iv);
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		mCurrentUser = ((MainActivity)getActivity()).getCurrentUser();
		mScoreTv.setText("当前积分："  + mCurrentUser.getScore());
		mUsernameTv.setText(mCurrentUser.getUsername());
		Glide.with(getContext()).load(mCurrentUser.getAvatar() == null ? R.mipmap.ic_launcher : mCurrentUser.getAvatar().getFileUrl(getContext()))
				.transform(new GlideCircleTransform(getContext())).into(avatorIv);

		mSignBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mFlag){
					if(mCurrentUser == null){
						mCurrentUser = new User();
						mCurrentUser.setUsername(userId);
					}
					mCurrentUser.increment("score", 3);
//					mCurrentUser.setScore(mCurrentUser.getScore() + 3);
					mCurrentUser.update(getContext(), new UpdateListener() {
						@Override
						public void onSuccess() {
							Toast.makeText(getContext(), "签到成功,积分+3", Toast.LENGTH_SHORT).show();
							mScoreTv.setText("当前积分:" + (mCurrentUser.getScore() + 3));

							SharedPreferences sharedPreferences = getContext().getSharedPreferences("signRecord", Context.MODE_PRIVATE);
							SharedPreferences.Editor editor = sharedPreferences.edit();
							editor.putString("signDate" + userId, mCurrentDate);
							editor.apply();

							mSignBtn.setText("已签到");
							mFlag = false;
						}

						@Override
						public void onFailure(int i, String s) {

						}
					});

				} else{
					Toast.makeText(getContext(), "您今天已签到！", Toast.LENGTH_SHORT).show();
				}

			}
		});

		final BmobQuery<Ticket> ticketBmobQuery = new BmobQuery<>();
		ticketBmobQuery.findObjects(getContext(), new FindListener<Ticket>() {
			@Override
			public void onSuccess(List<Ticket> list) {
				ticketAdapter = new TicketAdapter(getContext(), list, mCurrentUser);
				mListView.setAdapter(ticketAdapter);
			}

			@Override
			public void onError(int i, String s) {

			}
		});

		avatorIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getContext(), EditAvatarActivity.class);
				intent.putExtra("currentUser", mCurrentUser);
				startActivity(intent);
			}
		});

	}


	@Override
	public void onResume() {
		super.onResume();
		mCurrentUser = BmobUser.getCurrentUser(getContext(), User.class);
		mScoreTv.setText("当前积分:" + mCurrentUser.getScore());
	}
}