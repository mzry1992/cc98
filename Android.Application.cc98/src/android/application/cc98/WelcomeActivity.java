package android.application.cc98;

import android.app.Activity;
import android.application.cc98.network.HomePageTask;
import android.application.cc98.network.SignInTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class WelcomeActivity extends Activity implements GetWebPageInterface{

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
		
        /*new Handler().postDelayed(new Runnable() {
        	
        	@Override
        	public void run() {
        		
        	}
        }, 1000);*/
        
        if (username != null && username.length() > 0 &&
    		pwd != null && pwd.length() > 0) {
    		signInTask.execute(username, pwd, url);
    	}
    	else {
    		jumpToLogin();
    	}
    }
    
    @Override
	public void getWebPagePreProgress() {
	}
	
	@Override
	public void getWebPageProgressUpdate() {
		
	}
	
	@Override
	public void getWebPagePostProgress(String[] status) {
		
		if (status[0].equals("3") && status[1].equals("9898")) {
			String cookie = status[2];
			UserInfoUtil.SetCookieInfo(this, cookie);
			
			jumpToHomePage();
		}
		else {
			jumpToLogin();
		}
	}
	
	private void jumpToHomePage() {
		Intent welcomeIntent = new Intent(WelcomeActivity.this, HomePageActivity.class);
		WelcomeActivity.this.startActivity(welcomeIntent);
		WelcomeActivity.this.finish();
	}
	
	private void jumpToLogin() {
		Intent welcomeIntent = new Intent(WelcomeActivity.this, LoginActivity.class);
		WelcomeActivity.this.startActivity(welcomeIntent);
		WelcomeActivity.this.finish();
	}
}
