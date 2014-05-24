package android.application.cc98;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.application.cc98.fragment.BoardFragment;
import android.application.cc98.fragment.CustomizationFragment;
import android.application.cc98.fragment.HomePageManager;
import android.application.cc98.fragment.HotPostFragment;
import android.application.cc98.fragment.LoadingFragment;
import android.application.cc98.fragment.LoginErrorFragment;
import android.application.cc98.fragment.NetErrorFragment;
import android.application.cc98.fragment.SettingFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class FragmentHomeActivity extends ActionBarActivity implements
		OnClickListener {

	private boolean isShowing = true;

	private int currSelectedTab = 0;

	private boolean isHotPostInit = false, isHomePageInit = false;

	protected MenuItem msgMenuItem = null;
	protected MenuItem refreshItem = null;
	protected MenuItem moreItem = null;

	private Dialog popupDialog = null;
	private Boolean popupState = false;

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

	private Fragment globalFragment = null;

	public FragmentHomeActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fragment_home);
		initViews();
		fragmentManager = getSupportFragmentManager();
		initFragment();
		setTabSelection(0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isShowing = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		isShowing = true;
		refresh();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.refresh_menu, menu);
		// mainMenu = menu;
		refreshItem = menu.findItem(R.id.refresh);
		msgMenuItem = menu.findItem(R.id.message);
		msgMenuItem.setVisible(false);
		moreItem = menu.findItem(R.id.moreoption);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			if (currSelectedTab == 0 && !hotPostFragment.isNetRequesting()) {
				hotPostFragment.invalidData();
				isHotPostInit = false;
				refresh();
			} else if ((currSelectedTab == 1 || (currSelectedTab == 2 && !boardFragment.isLoad()) )
					&& !homePageManager.isNetRequesting()) {
				homePageManager.invalidData();
				isHomePageInit = false;
				refresh();
			}
			return true;
		case R.id.moreoption:
			if (!popupState) {
				showPop();
			} else {
				popupDialog.dismiss();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
		} else if (index == 1) {
			if (!isHomePageInit) {
				isHomePageInit = true;
				homePageManager.loadData();
			}
			currFragment = getFragment(homePageManager.getFragmentStatus(),
					index);
		} else if (index == 2) {
			if (boardFragment.isLoad())
				currFragment = boardFragment;
			else {
				if (!isHomePageInit) {
					isHomePageInit = true;
					homePageManager.loadData();
				}
				currFragment = getFragment(homePageManager.getFragmentStatus(),
						index);
			}
		} else {
			currFragment = settingFragment;
			settingFragment.updateUserInfo();
		}

		FragmentTransaction transaction = fragmentManager.beginTransaction();
		hideFragments(transaction);
		if (currFragment != null) {
			transaction.show(currFragment);
			globalFragment = currFragment;
		}
		transaction.commit();
		if (globalFragment == hotPostFragment) {
			hotPostFragment.restoreScrollPosition();
		} else if (globalFragment == custimizationFragment)
			custimizationFragment.restoreScrollPosition();
		else if (globalFragment == boardFragment)
			boardFragment.restoreScrollPosition();
	}

	private void clearSelection() {
		for (int i = 0; i < fragmentImageViews.size(); ++i)
			fragmentImageViews.get(i).setImageResource(unSelImageResIds[i]);
		for (int i = 0; i < fragmentTextViews.size(); ++i)
			fragmentTextViews.get(i).setTextColor(Color.parseColor("#82858b"));
	}

	private void hideFragments(FragmentTransaction transaction) {
		if (globalFragment == hotPostFragment)
			hotPostFragment.recordScrollPosition();
		else if (globalFragment == custimizationFragment)
			custimizationFragment.recordScrollPosition();
		else if (globalFragment == boardFragment)
			boardFragment.recordScrollPosition();
		for (int i = 0; i < fragmentArray.size(); ++i)
			transaction.hide(fragmentArray.get(i));
	}

	public void refresh() {
		if (isShowing)
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
		// 退出
		if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
			String exitCode = intent.getStringExtra("exit_code");
			if (exitCode.equals("true"))
				finish();
			else
				reStartActivity();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			if (!popupState) {
				showPop();
			} else {
				popupDialog.dismiss();
			}
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showPop() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.more_menu, null);
		ListView listView = (ListView) view.findViewById(R.id.moreMenuListView);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.more_menu_list_item, R.id.menu_title);
		adapter.add("切换用户");
		adapter.add("关于我们");
		adapter.add("退出程序");
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				popupDialog.dismiss();
				if (position == 2) {
					FragmentHomeActivity.this.finish();
				} else if (position == 0) {
					jumpToLogin();
				} else if (position == 1) {
					Intent i = new Intent(FragmentHomeActivity.this,
							AboutActivity.class);
					startActivity(i);
				}
			}

		});
		popupDialog = new Dialog(this);
		popupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		popupDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.WHITE));
		popupDialog.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		popupDialog.setContentView(view);
		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		ActionBar maActionBar = getSupportActionBar();
		int actionBarHeight = maActionBar.getHeight();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
			actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
					getResources().getDisplayMetrics());
		}
		WindowManager.LayoutParams wmlp = popupDialog.getWindow()
				.getAttributes();
		wmlp.gravity = Gravity.TOP | Gravity.RIGHT;
		wmlp.x += 12;
		wmlp.y += actionBarHeight;
		popupDialog.getWindow().setAttributes(wmlp);
		popupDialog.setCanceledOnTouchOutside(true);
		popupDialog.show();
	}

}
