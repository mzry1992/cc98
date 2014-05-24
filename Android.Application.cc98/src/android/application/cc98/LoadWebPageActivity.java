package android.application.cc98;

import java.lang.reflect.Field;

import android.app.Dialog;
import android.application.cc98.network.UserInfoUtil;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public abstract class LoadWebPageActivity extends ActionBarActivity implements
		GetWebPageInterface {

	protected MenuItem msgMenuItem = null;
	protected MenuItem refreshItem = null;
	protected MenuItem moreItem = null;
	protected String cookie = null, serverName = null;
	private boolean isPageLoad = false;

	private Dialog popupDialog = null;
	
	private Boolean popupState = false;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		serverName = getString(R.string.serverName);
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
		preLoadPage();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			preLoadPage();
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

	protected void preLoadPage() {
		cookie = UserInfoUtil.GetCookieInfo(this);
		if (cookie == null)
			jumpToLogin(true);
		else
			loadPage();
	}

	abstract public void loadPage();

	abstract public void loadPageSucess(Object outputs);

	abstract public int getStatusCode(Object outputs);

	abstract public String getErrorMessage(Object outputs);

	private void loadPageErrorHandle(int statusCode) {

		if (statusCode != 3 && statusCode != 1) {
			setContentView(R.layout.loading_error_net);
			ImageButton retryButton = (ImageButton) this
					.findViewById(R.id.retry_image_button);
			if (retryButton != null) {
				retryButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						preLoadPage();
					}
				});
			}
		} else {
			// jump to login page
			jumpToLogin(true);
		}
	}

	public LoadWebPageActivity() {
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void getWebPagePreProgress() {
		// TODO Auto-generated method stub
		if (!isPageLoad) {
			showLoadingPage();
		}
	}

	@Override
	public void getWebPageProgressUpdate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getWebPagePostProgress(Object status) {
		// TODO Auto-generated method stub

		int statusCode = getStatusCode(status);
		// System.out.println("statusCode:" + statusCode);
		boolean isLoadSucess = false;
		StringBuilder errorStrBuilder = new StringBuilder();

		switch (statusCode) {
		case 1:
			errorStrBuilder.append("用户登录信息无法认证，请重新登录");
			break;
		case 2:
			errorStrBuilder.append("cc98服务器异常, code:");
			errorStrBuilder.append(getErrorMessage(status));
			break;
		case 3:
		case 5:
			loadPageSucess(status);
			isLoadSucess = true;
			isPageLoad = true;
			break;
		case 4:
			errorStrBuilder.append("网络异常，请检查网络");
			break;
		default:
			errorStrBuilder.append("未知错误，请联系开发人员");
			break;
		}

		if (!isLoadSucess) {
			Toast.makeText(this, errorStrBuilder.toString(), Toast.LENGTH_LONG)
					.show();
			loadPageErrorHandle(statusCode);
		}

	}

	private void jumpToLogin(boolean isSetView) {
		if (isSetView) {
			setContentView(R.layout.login_in_error);
			ImageButton retryButton = (ImageButton) this
					.findViewById(R.id.retry_image_button);
			if (retryButton != null) {
				retryButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						preLoadPage();
					}
				});
			}
		}
		Intent welcomeIntent = new Intent(this, LoginActivity.class);
		this.startActivity(welcomeIntent);
	}

	private void exitProgram() {
		Intent intent = new Intent();
		intent.setClass(this, FragmentHomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("exit_code", "true");
		startActivity(intent);
		finish();
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
					exitProgram();
				}
				else if (position == 0){
					jumpToLogin(false);
				}
				else if (position == 1) {
					Intent i = new Intent(LoadWebPageActivity.this, AboutActivity.class);
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
	
	private void showLoadingPage() {
		setContentView(R.layout.loading);
	}

}
