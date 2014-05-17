package android.application.cc98;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class WelcomeActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome);
		
        new Handler().postDelayed(new Runnable() {
        	@Override
        	public void run() {
        		jumpToHomePage();
        	}
        }, 1000);
    }
	
	private void jumpToHomePage() {
		Intent welcomeIntent = new Intent(WelcomeActivity.this, HomePageActivity.class);
		WelcomeActivity.this.startActivity(welcomeIntent);
		WelcomeActivity.this.finish();
	}
	
}
