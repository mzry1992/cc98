package android.application.cc98;

import android.app.Activity;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public abstract class LoadWebPageActivity extends Activity implements GetWebPageInterface {
	
	protected MenuItem msgMenuItem = null;
	protected MenuItem refreshItem = null;
	protected String cookie = null, serverName = null;
	private boolean isPageLoad = false; 
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.refresh_menu, menu);
		refreshItem = menu.findItem(R.id.refresh);
		msgMenuItem = menu.findItem(R.id.message);
		msgMenuItem.setVisible(false);
		preLoadPage();
		serverName = getString(R.string.serverName);;
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			preLoadPage();
			return true;
		case R.id.exit_menu_item:
			exitProgram();
			return true;
		case R.id.change_user_menu_item:
			jumpToLogin(false);
			return true;
		case R.id.about_us_menu_item:
			Intent i = new Intent(this, AboutActivity.class);
			startActivity(i);
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
	
	abstract public void loadPage() ;
	
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
			//jump to login page
			jumpToLogin(true);
		}
	}

	public LoadWebPageActivity() {
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void getWebPagePreProgress() {
		// TODO Auto-generated method stub
		//showRefreshAnimation();
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
		hideRefreshAnimation();
		
		int statusCode = getStatusCode(status);
		//System.out.println("statusCode:" + statusCode);
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
		intent.setClass(this, HomePageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("exit_code", "true");
		startActivity(intent);
		finish();
	}
	
	private void showRefreshAnimation() {
		hideRefreshAnimation();

		// 这里使用一个ImageView设置成MenuItem的ActionView，这样我们就可以使用这个ImageView显示旋转动画了
		ImageView refreshActionView = (ImageView) getLayoutInflater().inflate(
				R.layout.refresh_action_view, null);
		refreshActionView.setImageResource(R.drawable.ic_action_refresh);

		refreshItem.setActionView(refreshActionView);

		// 显示刷新动画
		Animation animation = AnimationUtils
				.loadAnimation(this, R.anim.refresh);
		animation.setRepeatMode(Animation.RESTART);
		animation.setRepeatCount(Animation.INFINITE);
		refreshActionView.startAnimation(animation);
	}

	private void hideRefreshAnimation() {
		if (refreshItem != null) {
			View view = refreshItem.getActionView();
			if (view != null) {
				view.clearAnimation();
				refreshItem.setActionView(null);
			}
		}
	}
	
	private void showLoadingPage() {
		setContentView(R.layout.loading);
	}

}
