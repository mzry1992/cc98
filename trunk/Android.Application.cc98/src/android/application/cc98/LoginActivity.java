package android.application.cc98;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.application.cc98.network.SignInTask;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity  implements OnClickListener, SignInInterface {

	EditText usernameET, pwdET;
	Button loginBT;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        //get edittext control
        usernameET = (EditText)findViewById(R.id.usernameEditText);
        pwdET = (EditText)findViewById(R.id.pwdEditText);
        // set password to be hidden
        pwdET.setTransformationMethod(PasswordTransformationMethod.getInstance());  
        // setup login button 
        loginBT = (Button)findViewById(R.id.loginButton);
        loginBT.setOnClickListener(this);
	}
	
	public void onClick(View v){
    	switch (v.getId()) {
    	case R.id.loginButton:
			// set progress dialog to indicate login progress
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setTitle("登陆信息");
			progressDialog.setMessage("正在登陆，请稍后...");
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			//start new thread to login in
			new SignInTask(this).execute(usernameET.getText().toString(),
										 pwdET.getText().toString(),
										 getSignURL());
    	break;
    	// More buttons go here (if any) ...
    	}
    }
	
	private String getSignURL() {
		if (this == null)
			return null;
		StringBuilder signURLBuilder = new StringBuilder();
		signURLBuilder.append("http://");
		signURLBuilder.append(getString(R.string.serverName)); 
		signURLBuilder.append("/");
		signURLBuilder.append(getString(R.string.signSuffix));
		String signURL = signURLBuilder.toString();
		return signURL;
	}
	
	@Override
	public void SignInPreProgress() {
		this.progressDialog.show(); 
	}
	
	@Override
	public void SignInPostProgress(String[] status) {
		StringBuilder tmpStrBud = new StringBuilder();
		tmpStrBud.append("statusCode=");
		if (status[0] != null)
			tmpStrBud.append(status[0]);
		tmpStrBud.append("&statusMessage=");
		if (status[1] != null)
			tmpStrBud.append(status[1]);
		SharedPreferences userSettings = this.getSharedPreferences(this.getString(R.string.userInfoFileName), Activity.MODE_WORLD_READABLE);  
		String cookie = userSettings.getString(this.getString(R.string.cookieFile), null);
		tmpStrBud.append("&cookie=");
		if (cookie != null)
			tmpStrBud.append(cookie);
		usernameET.setText(tmpStrBud.toString());
		this.progressDialog.dismiss();
	}
	
	@Override
	public void SignInProgressUpdate() {
		
	}
}
