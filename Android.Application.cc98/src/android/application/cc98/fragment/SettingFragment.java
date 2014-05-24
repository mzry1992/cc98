package android.application.cc98.fragment;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.application.cc98.AboutActivity;
import android.application.cc98.FragmentHomeActivity;
import android.application.cc98.LoginActivity;
import android.application.cc98.R;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class SettingFragment extends Fragment implements OnClickListener {

	private View settingLayout = null;

	private TextView userNameTv = null;
	
	int viewIds[] = { R.id.changUserLayout, R.id.feedbackLayout,
			R.id.aboutLayout, R.id.exitButton };

	public SettingFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		settingLayout = inflater.inflate(R.layout.setting, container, false);
		for (int i = 0; i < viewIds.length; ++i) {
			View currView = settingLayout.findViewById(viewIds[i]);
			currView.setOnClickListener(this);
		}
		userNameTv = (TextView)settingLayout.findViewById(R.id.userNameTV);
		updateUserInfo();
		return settingLayout;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.changUserLayout:
			jumpToLogin();
			break;
		case R.id.feedbackLayout:
			jumpToAbout();
			break;
		case R.id.aboutLayout:
			jumpToAbout();
			break;
		default:
			getActivity().finish();
		}
	}
	
	private void jumpToAbout() {
		Intent i = new Intent(getActivity(), AboutActivity.class);
		startActivity(i);
	}
	
	private void jumpToLogin() {
		Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
		this.startActivity(loginIntent);
	}
	
	public void updateUserInfo() {
		String userName = UserInfoUtil.GetUserName(getActivity());
		if (userName != null) {
			userNameTv.setText(userName);
		}
		else {
			userNameTv.setText("Î´µÇÂ¼");
		}
	}
}
