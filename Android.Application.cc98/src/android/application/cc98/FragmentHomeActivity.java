package android.application.cc98;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.application.cc98.fragment.BoardFragment;
import android.application.cc98.fragment.CustomizationFragment;
import android.application.cc98.fragment.HomePageManager;
import android.application.cc98.fragment.HotPostFragment;
import android.application.cc98.fragment.LoadingFragment;
import android.application.cc98.fragment.LoginErrorFragment;
import android.application.cc98.fragment.NetErrorFragment;
import android.application.cc98.fragment.SettingFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentHomeActivity extends Activity implements OnClickListener {

	private int currSelectedTab = 0;

	private boolean isHotPostInit = false, isHomePageInit = false;

	int layoutIds[] = { R.id.hotpost_layout, R.id.customization_layout,
			R.id.board_layout, R.id.setting_layout };
	int imageViewIds[] = { R.id.hotpost_image, R.id.customization_image,
			R.id.board_image, R.id.setting_image };
	int textViewIds[] = { R.id.hotpost_text, R.id.customization_text,
			R.id.board_text, R.id.setting_text };
	int imageResIds[] = { R.drawable.message_selected,
			R.drawable.contacts_selected, R.drawable.news_selected,
			R.drawable.setting_selected };
	int unSelImageResIds[] = { R.drawable.message_unselected,
			R.drawable.contacts_unselected, R.drawable.news_unselected,
			R.drawable.setting_unselected };

	private ArrayList<View> fragmentViews = null;

	private ArrayList<ImageView> fragmentImageViews = null;

	private ArrayList<TextView> fragmentTextViews = null;

	private ArrayList<Fragment> fragmentArray = null;

	private HotPostFragment hotPostFragment = null;
	private CustomizationFragment custimizationFragment = null;
	private BoardFragment boardFragment = null;
	private LoadingFragment loadingFragment = null;
	private LoginErrorFragment loginErrorFragment = null;
	private NetErrorFragment netErrorFragment = null;
	private SettingFragment settingFragment = null;

	private HomePageManager homePageManager = null;

	private FragmentManager fragmentManager;

	public FragmentHomeActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_home);
		initViews();
		fragmentManager = getFragmentManager();
		initFragment();
		setTabSelection(0);
	}

	private void initViews() {
		fragmentViews = new ArrayList<View>();
		fragmentImageViews = new ArrayList<ImageView>();
		fragmentTextViews = new ArrayList<TextView>();
		for (int i = 0; i < layoutIds.length; ++i) {
			View currView = findViewById(layoutIds[i]);
			currView.setOnClickListener(this);
			fragmentViews.add(currView);
			ImageView currImage = (ImageView) findViewById(imageViewIds[i]);
			fragmentImageViews.add(currImage);
			TextView currText = (TextView) findViewById(textViewIds[i]);
			fragmentTextViews.add(currText);
		}
	}

	private void initFragment() {
		fragmentArray = new ArrayList<Fragment>();
		hotPostFragment = new HotPostFragment(this);
		fragmentArray.add(hotPostFragment);
		homePageManager = new HomePageManager(this);
		custimizationFragment = homePageManager.getCustomizationFragment();
		fragmentArray.add(custimizationFragment);
		boardFragment = homePageManager.getBoardFragment();
		fragmentArray.add(boardFragment);
		loadingFragment = new LoadingFragment();
		fragmentArray.add(loadingFragment);
		loginErrorFragment = new LoginErrorFragment();
		fragmentArray.add(loginErrorFragment);
		netErrorFragment = new NetErrorFragment();
		fragmentArray.add(netErrorFragment);
		settingFragment = new SettingFragment();
		fragmentArray.add(settingFragment);
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		for (int i = 0; i < fragmentArray.size(); ++i) {
			transaction.add(R.id.content, fragmentArray.get(i));
		}
		transaction.commit();
	}
	
	private void reStartActivity() {
		hotPostFragment.invalidData();
		homePageManager.invalidData();
		isHotPostInit = false;
		isHomePageInit = false;
		currSelectedTab = 0;
		refresh();
	}

	@Override
	public void onClick(View v) {
		for (int i = 0; i < layoutIds.length; ++i)
			if (v.getId() == layoutIds[i]) {
				currSelectedTab = i;
				setTabSelection(i);
				// Toast.makeText(this, "gogo", Toast.LENGTH_LONG).show();
				break;
			}
	}

	private void setTabSelection(int index) {
		clearSelection();
		fragmentImageViews.get(index).setImageResource(imageResIds[index]);
		fragmentTextViews.get(index).setTextColor(Color.WHITE);

		Fragment currFragment = null;

		if (index == 0) {
			if (!isHotPostInit) {
				isHotPostInit = true;
				hotPostFragment.loadData();
			}
			currFragment = getFragment(hotPostFragment.getFragmentStatus(),
					index);
		} else if (index == 1 || index == 2) {
			if (!isHomePageInit) {
				isHomePageInit = true;
				homePageManager.loadData();
			}
			currFragment = getFragment(homePageManager.getFragmentStatus(),
					index);
		}
		else {
			currFragment = settingFragment;
			settingFragment.updateUserInfo();
		}

		FragmentTransaction transaction = fragmentManager.beginTransaction();
		hideFragments(transaction);
		if (currFragment != null) {
			transaction.show(currFragment);
		}
		transaction.commit();

	}

	private void clearSelection() {
		for (int i = 0; i < fragmentImageViews.size(); ++i)
			fragmentImageViews.get(i).setImageResource(unSelImageResIds[i]);
		for (int i = 0; i < fragmentTextViews.size(); ++i)
			fragmentTextViews.get(i).setTextColor(Color.parseColor("#82858b"));
	}

	private void hideFragments(FragmentTransaction transaction) {
		for (int i = 0; i < fragmentArray.size(); ++i)
			transaction.hide(fragmentArray.get(i));
	}

	public void refresh() {
		setTabSelection(currSelectedTab);
	}

	public void jumpToLogin() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		this.startActivity(loginIntent);
	}

	private Fragment getFragment(int code, int index) {
		switch (code) {
		case 0:
			return loadingFragment;
		case 1:
			return fragmentArray.get(index);
		case 2:
			return loginErrorFragment;
		default:
			return netErrorFragment;
		}
	}

	public void reTry() {
		if (currSelectedTab == 0) {
			isHotPostInit = false;
		} else if (currSelectedTab == 1 || currSelectedTab == 2) {
			isHomePageInit = false;
		}
		refresh();
	}

	// Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		// ÍË³ö
		if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
			String exitCode = intent.getStringExtra("exit_code");
			if (exitCode.equals("true"))
				finish();
			else
				reStartActivity();
		}
	}

}
