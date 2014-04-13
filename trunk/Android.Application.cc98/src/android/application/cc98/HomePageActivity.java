package android.application.cc98;

import android.app.Activity;
import android.application.cc98.network.HomePageTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Toast;

public class HomePageActivity extends Activity implements GetWebPageInterface{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.testwebview);
		
		String cookie = UserInfoUtil.GetCookieInfo(this);
		new HomePageTask(this).execute(cookie, UserInfoUtil.getHomePageURL(this));
	}
	
	@Override
	public void getWebPagePreProgress() {
		
	}
	
	@Override
	public void getWebPageProgressUpdate() {
		
	}
	
	@Override
	public void getWebPagePostProgress(String[] status) {
		Toast.makeText(this, "In getHomePagePostProgress" + "  status[0]:" + status[0], Toast.LENGTH_SHORT).show();
		if (status[0].equals("3")) {
			//Intent intent = new Intent(this, TestWebView.class);
			//intent.putExtra("home page", status[1]);
			//startActivity(intent);
			String homePageHtml = status[1];
			WebView wbView = (WebView)findViewById(R.id.webview_page);
	        wbView.getSettings().setDefaultTextEncodingName("UTF-8");
	        wbView.loadData(homePageHtml, "text/html; charset=UTF-8", null);
		}
		else {
			// show error
		}
	}
}

