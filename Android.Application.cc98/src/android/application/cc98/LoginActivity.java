package android.application.cc98;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@SuppressLint("NewApi") public class LoginActivity extends Activity  implements OnClickListener {

	TextView usernameTV, pwdTV;
	EditText usernameET, pwdET;
	Button loginBT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        
        // set password to be hidden
        pwdET = (EditText)findViewById(R.id.pwdEditText);
        pwdET.setTransformationMethod(PasswordTransformationMethod.getInstance());
        
        // setup login button 
        loginBT = (Button)findViewById(R.id.loginButton);
        loginBT.setOnClickListener(this);
        
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
        		.detectDiskWrites().detectNetwork().penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
        		.detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
	}
	
	public void onClick(View v){
    	switch (v.getId()) {
    	case R.id.loginButton:
    		EditText userNameTextView = (EditText)findViewById(R.id.usernameEditText);
			EditText pswTextView = (EditText)findViewById(R.id.pwdEditText);
    		NetworkProxy networkProxy = new NetworkProxy(this);
    		String[] status = networkProxy.trySign(userNameTextView.getText().toString(), pswTextView.getText().toString());
    		StringBuilder tmpStrBud = new StringBuilder();
    		tmpStrBud.append("statusCode=");
    		if (status[1] != null)
    			tmpStrBud.append(status[0]);
    		tmpStrBud.append("&statusMessage=");
    		if (status[1] != null)
    			tmpStrBud.append(status[1]);
    		SharedPreferences userSettings = this.getSharedPreferences(this.getString(R.string.userInfoFileName), Activity.MODE_WORLD_READABLE);  
    		String cookie = userSettings.getString(this.getString(R.string.cookieFile), null);
    		tmpStrBud.append("&cookie=");
    		if (cookie != null)
    			tmpStrBud.append(cookie);
    		userNameTextView.setText(tmpStrBud.toString());
    	break;
    	// More buttons go here (if any) ...
    	}
    }
	
	
	
}
