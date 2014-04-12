package android.application.cc98;

import android.app.Activity;
import android.application.cc98.network.HomePageTask;
import android.application.cc98.network.SignInTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class WelcomeActivity extends Activity implements SignInInterface, GetHomePageInterface{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome);
        
		final String username = UserInfoUtil.GetUserName(this);
		final String pwd = UserInfoUtil.GetPassword(this);
		final String url = UserInfoUtil.getSignURL(this);
		final SignInTask signInTask = new SignInTask(this);
		
        new Handler().postDelayed(new Runnable() {
        	
        	@Override
        	public void run() {
        		if (username != null && username.length() > 0 &&
        			pwd != null && pwd.length() > 0) {
        			signInTask.execute(username, pwd, url);
        		}
        		else {
        			jumpToLogin();
        		}
        	}
        }, 1000);
    }
    
    @Override
	public void SignInPreProgress() {
	}
	
	@Override
	public void SignInProgressUpdate() {
		
	}
	
	@Override
	public void SignInPostProgress(String[] status) {
		if (status[0] == "3" && status[1] == "9898") {
			String cookie = status[2];
			UserInfoUtil.SetCookieInfo(this, cookie);
			new HomePageTask(this).execute(cookie, UserInfoUtil.getHomePageURL(this));
		}
		else {
			jumpToLogin();
		}
	}
	
	@Override
	public void getHomePreProgress() {
		
	}
	
	@Override
	public void getHomeProgressUpdate() {
		
	}
	
	@Override
	public void getHomePostProgress(String[] status) {
		if (status[0] == "3") {
			Intent intent = new Intent(this, TestWebView.class);
			intent.putExtra("home page", status[1]);
			startActivity(intent);
		}
		else {
			// show error
		}
	}
	
	private void jumpToLogin() {
		Intent welcomeIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
		WelcomeActivity.this.startActivity(welcomeIntent);
		WelcomeActivity.this.finish();
	}
}
