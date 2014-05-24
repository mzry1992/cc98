package android.application.cc98;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewActivity extends Activity{  
    private WebView webView;  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        // TODO Auto-generated method stub  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.webview);  
        
        Intent intent = this.getIntent();
        String url = intent.getStringExtra(this.getString(R.string.webViewHyperlink));
        webView = (WebView) findViewById(R.id.webView00);  
        webView.getSettings().setBuiltInZoomControls(true);
        
        //System.out.println("In Webview Open url:" + url);
        webView.loadUrl(url);  
        webView.setWebViewClient(new WebViewClient(){  
            @Override  
            public boolean shouldOverrideUrlLoading(WebView view, String url) {  
                // TODO Auto-generated method stub  
                view.loadUrl(url);
                return true;
            }  
            @Override
            public void onReceivedError(WebView view, int errorCode,  
                    String description, String failingUrl) {  
                // TODO Auto-generated method stub  
                Toast.makeText(WebViewActivity.this, "ÍøÒ³¼ÓÔØ³ö´í£¡ " + description, Toast.LENGTH_SHORT).show();  
            }  
        });  
    }  
    
    @Override 
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        // TODO Auto-generated method stub  
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {  
            webView.goBack();  
            return true;  
        }  
        return super.onKeyDown(keyCode, event);  
    }  
}  