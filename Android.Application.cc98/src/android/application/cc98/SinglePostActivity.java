package android.application.cc98;

import java.util.ArrayList;

import android.app.Activity;
import android.application.cc98.network.SinglePostTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class SinglePostActivity extends Activity implements GetWebPageInterface{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_post);
		
		Intent intent = getIntent();
        String postUrl = intent.getStringExtra(this.getString(R.string.postUrl));
		String cookie = UserInfoUtil.GetCookieInfo(this);
		//homePage = UserInfoUtil.getHomePageURL(this);
		new SinglePostTask(this, this.getString(R.string.serverName)).execute(cookie, postUrl);
	}
	
	@Override
	public void getWebPagePreProgress() {
		// set layout invisible
		//LinearLayout layout = (LinearLayout)this.findViewById(R.id.leafBoardLayout);
		//layout.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void getWebPageProgressUpdate() {
		
	}
	
	@Override
	public void getWebPagePostProgress(Object outputRes) {
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>)outputRes;
		ArrayList<String> status = outputs.get(0);

		if (status.get(0).equals("3")) {
			
		}
		else {
		}
	}
}
