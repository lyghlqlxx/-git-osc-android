package net.oschina.gitapp.ui;

import net.oschina.gitapp.AppContext;
import net.oschina.gitapp.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import net.oschina.gitapp.bean.URLs;
import net.oschina.gitapp.bean.User;
import net.oschina.gitapp.common.BroadcastController;
import net.oschina.gitapp.common.UIHelper;
import net.oschina.gitapp.interfaces.*;
import net.oschina.gitapp.widget.BadgeView;
import net.oschina.gitapp.widget.CircleImageView;

/**
 * 导航菜单栏
 * @created 2014-04-29
 * @author 火蚁（http://my.oschina.net/LittleDY）
 * 
 */
public class DrawerNavigation extends Fragment implements OnClickListener {

	public static DrawerNavigation newInstance() {
		return new DrawerNavigation();
	}
	
	private View mSavedView;// 当前操作的菜单项
	private RelativeLayout mMenu_user_layout;
	private LinearLayout mMenu_user_info_layout;
	private LinearLayout mMenu_user_login_tips;
	private CircleImageView mUser_info_userface;
	private TextView mUser_info_username;

	private LinearLayout mMenu_item_explore;
	private LinearLayout mMenu_item_myself;
	private LinearLayout mMenu_item_notice;
	private LinearLayout mMenu_item_setting;
	private LinearLayout mMenu_item_exit;
	
	private TextView mMenu_item_notice_text;

	private DrawerMenuCallBack mCallBack;
	private AppContext mApplication;
	
	public static BadgeView mNotification_bv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (AppContext) getActivity().getApplication();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof DrawerMenuCallBack) {
			mCallBack = (DrawerMenuCallBack) activity;
		}
		// 注册一个用户发生变化的广播
		BroadcastController.registerUserChangeReceiver(activity, mUserChangeReceiver);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallBack = null;
		// 注销接收用户信息变更的广播
		BroadcastController.unregisterReceiver(getActivity(), mUserChangeReceiver);
	}
	
	private BroadcastReceiver mUserChangeReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//接收到变化后，更新用户资料
			setupUserView(true);
		}
	};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initView(view);
		setupUserView(mApplication.isLogin());
	}
	
	private void setupUserView(final boolean reflash) {
		//判断是否已经登录，如果已登录则显示用户的头像与信息
		if(!mApplication.isLogin()) {
			mUser_info_userface.setImageResource(R.drawable.mini_avatar);
			mUser_info_username.setText("");
			mMenu_user_info_layout.setVisibility(View.GONE);
			mMenu_user_login_tips.setVisibility(View.VISIBLE);
			return;
		}
		
		mMenu_user_info_layout.setVisibility(View.VISIBLE);
		mMenu_user_login_tips.setVisibility(View.GONE);
		mUser_info_username.setText("");
		
		new AsyncTask<Void, Void, User>() {
			
			@Override
			protected User doInBackground(Void... params) {
				User user = mApplication.getLoginInfo();
				return user;
			}
			
			@Override
			protected void onPostExecute(User user) {
				if(user == null || isDetached()) {
					return;
				}
				// 加载用户头像
				String faceUrl = URLs.HTTP + URLs.HOST + URLs.URL_SPLITTER + user.getPortrait();
				UIHelper.showUserFace(mUser_info_userface, faceUrl);
				mUser_info_userface.setBorderWidth(2);
				mUser_info_userface.setBorderColor(getResources().getColor(R.color.white));
				// 其他资料
				mUser_info_username.setText(user.getName());
			}
			
		}.execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_drawer_menu, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	private void initBadgeView(View view) {
		mNotification_bv = (BadgeView) view.findViewById(R.id.menu_item_notice_badgeView);
		mNotification_bv.setVisibility(View.GONE);
	}

	// 初始化界面控件
	private void initView(View view) {
		mMenu_user_layout = (RelativeLayout) view.findViewById(R.id.menu_user_layout);
		mMenu_user_info_layout = (LinearLayout) view.findViewById(R.id.menu_user_info_layout);
		mUser_info_userface = (CircleImageView) view.findViewById(R.id.menu_user_info_userface);
		mUser_info_username = (TextView) view.findViewById(R.id.menu_user_info_username);
		mMenu_user_login_tips = (LinearLayout) view.findViewById(R.id.menu_user_info_login_tips_layout);

		mMenu_item_explore = (LinearLayout) view.findViewById(R.id.menu_item_explore);
		mMenu_item_myself = (LinearLayout) view.findViewById(R.id.menu_item_myself);
		mMenu_item_notice = (LinearLayout) view.findViewById(R.id.menu_item_notice);
		mMenu_item_setting = (LinearLayout) view.findViewById(R.id.menu_item_setting);
		mMenu_item_exit = (LinearLayout) view.findViewById(R.id.menu_item_exit);
		
		// 绑定点击事件
		mMenu_user_layout.setOnClickListener(this);
		mMenu_item_explore.setOnClickListener(this);
		mMenu_item_myself.setOnClickListener(this);
		mMenu_item_notice.setOnClickListener(this);
		mMenu_item_setting.setOnClickListener(this);
		mMenu_item_exit.setOnClickListener(this);

		// 高亮发现菜单栏
		highlightSelectedItem(mMenu_item_explore);
		initBadgeView(view);
	}

	private void highlightSelectedItem(View v) {
		setSelected(null, false);
		setSelected(v, true);
	}

	private void setSelected(View v, boolean selected) {
		View view;

		if (v == null && mSavedView == null) {
			return;
		}

		if (v != null) {
			mSavedView = v;
			view = mSavedView;

		} else {
			view = mSavedView;
		}

		if (selected) {
			ViewCompat.setHasTransientState(view, true);
			view.setBackgroundColor(getResources().getColor(
					R.color.accent_color));

		} else {
			ViewCompat.setHasTransientState(view, false);
			view.setBackgroundColor(getResources()
					.getColor(R.color.transparent));
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.menu_user_layout:
			showLoginActivity();
			break;
		case R.id.menu_item_explore:
			onClickExplore();
			highlightSelectedItem(v);
			break;
		case R.id.menu_item_myself:
			onClickMySelf();
			if (mApplication.isLogin()) {
				highlightSelectedItem(v);
			}
			break;
		case R.id.menu_item_notice:
			onClickNotice();
			highlightSelectedItem(v);
			break;
		case R.id.menu_item_setting:
			showSettingActivity();
			break;
		case R.id.menu_item_exit:
			onClickExit();
			break;
		}
	}

	private void showLoginActivity() {
		if (!mApplication.isLogin()) {
			Intent intent = new Intent(getActivity(), LoginActivity.class);
			getActivity().startActivity(intent);
		} else {
			UIHelper.showUserInfoDetail(getActivity());
		}
	}
	
	private void showSettingActivity() {
		Intent intent = new Intent(getActivity(), SettingActivity.class);
		getActivity().startActivity(intent);
	}

	private void onClickExplore() {
		if (mCallBack != null) {
			mCallBack.onClickExplore();
		}
	}

	private void onClickMySelf() {
		if (mCallBack != null) {
			mCallBack.onClickMySelf();
		}
	}

	private void onClickNotice() {
		if (mCallBack != null) {
			mCallBack.onClickNotice();
		}
	}

	private void onClickExit() {
		if (mCallBack != null) {
			mCallBack.onClickExit();
		}
	}
}
