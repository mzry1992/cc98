package android.application.cc98;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

public class TestWebView extends Activity {
	
	 @Override
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.testwebview);
	        
	        Intent intent = getIntent();
	        String loginPageHtml = intent.getStringExtra("home page");
	        WebView wbView = (WebView)findViewById(R.id.webview_page);
	        wbView.getSettings().setDefaultTextEncodingName("UTF-8");
	        wbView.loadData(loginPageHtml, "text/html; charset=UTF-8", null);
	    }

}