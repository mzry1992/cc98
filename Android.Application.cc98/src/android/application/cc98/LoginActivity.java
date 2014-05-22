package android.application.cc98;


import android.app.Activity;
import android.app.ProgressDialog;
import android.application.cc98.network.SignInTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity  implements OnClickListener, GetWebPageInterface {

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
    		//determine whether input is empty
    		TextView errorTV = (TextView)this.findViewById(R.id.errorTextView);
    		if (usernameET.getText().toString().length() == 0) {
    			errorTV.setText("用户名不能为空");
    			Toast.makeText(this, "用户名不能为空", Toast.LENGTH_LONG).show();
    			return;
    		}
    		if (pwdET.getText().toString().length() == 0) {
    			errorTV.setText("密码不能为空");
    			Toast.makeText(this, "密码不能为空", Toast.LENGTH_LONG).show();
    			return;
    		}
    		
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
		StringBuilder signURLBuilder = new StringBuilder();
		signURLBuilder.append("http://");
		signURLBuilder.append(getString(R.string.serverName)); 
		signURLBuilder.append("/");
		signURLBuilder.append(getString(R.string.signSuffix));
		String signURL = signURLBuilder.toString();
		return signURL;
	}
	
	@Override
	public void getWebPagePreProgress() {
		this.progressDialog.show(); 
	}
	
	@Override
	public void getWebPagePostProgress(Object outputs) {
		String[] status = (String[])outputs;
		TextView errorTV = (TextView)this.findViewById(R.id.errorTextView);
		if (status[0] == null) {
			errorTV.setText("未知错误，请联系开发人员");
			return;
		}
		
		StringBuilder strBuilder = new StringBuilder();
		int statusCode = Integer.parseInt(status[0]);
		switch (statusCode) {
		case 1:
			strBuilder.append("用户名或密码不能为空");
			break;
		case 2:
			strBuilder.append("服务器异常，错误码：");
			strBuilder.append(status[1]);
			break;
		case 3: 
			handleSignInError(status[1],strBuilder,status[2]);
			break;
		case 4:
			strBuilder.append("网络异常，请检查您的网络配置");
			break;
		default:
			strBuilder.append("未知错误，请联系开发人员");
			break;
		}
		errorTV.setText(strBuilder.toString());
		Toast.makeText(this,strBuilder.toString(), Toast.LENGTH_LONG).show();
		this.progressDialog.dismiss();
	}
	
	@Override
	public void getWebPageProgressUpdate() {
		
	}
	
	public void handleSignInError(String errorStr, StringBuilder strBuilder, String cookie) {
		if (errorStr == null) {
			strBuilder.append("未知错误，请联系开发人员");
			return;
		}
		int errorCode = Integer.parseInt(errorStr);
		if (errorCode == 9898) {
			//show tips of login success
			strBuilder.append("登录成功");
			//set cookie
			if (cookie != null)
				UserInfoUtil.SetCookieInfo(this, cookie);
			//set user info
			UserInfoUtil.SetUserInfo(this, usernameET.getText().toString(), pwdET.getText().toString());
			//start home activity
			jumpToHomePage();
			return;
		}
		switch (errorCode) {
		case 101:
			strBuilder.append("用户名不能为空");
			break;
		case 102:
			strBuilder.append("密码不能为空");
			break;
		case 103: 
			strBuilder.append("xmlHttp请求失败");
			break;
		case 1001:
			strBuilder.append("该用户名不存在，请检查用户名是否正确");
			break;
		case 1002:
			strBuilder.append("该用户已被锁定，如果疑问请联系站务组");
			break;
		case 1003:
			strBuilder.append("密码错误,请找回密码");
			break;
		default:
			strBuilder.append("未知错误，请联系开发人员");
			break;
		}
	}
	
	private void jumpToHomePage() {
		Intent loginIntent = new Intent(LoginActivity.this, FragmentHomeActivity.class);
		loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		loginIntent.putExtra("exit_code", "false");
		LoginActivity.this.startActivity(loginIntent);
		LoginActivity.this.finish();
	}
}
