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
    			errorTV.setText("�û�������Ϊ��");
    			Toast.makeText(this, "�û�������Ϊ��", Toast.LENGTH_LONG).show();
    			return;
    		}
    		if (pwdET.getText().toString().length() == 0) {
    			errorTV.setText("���벻��Ϊ��");
    			Toast.makeText(this, "���벻��Ϊ��", Toast.LENGTH_LONG).show();
    			return;
    		}
    		
			// set progress dialog to indicate login progress
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setTitle("��½��Ϣ");
			progressDialog.setMessage("���ڵ�½�����Ժ�...");
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
			errorTV.setText("δ֪��������ϵ������Ա");
			return;
		}
		
		StringBuilder strBuilder = new StringBuilder();
		int statusCode = Integer.parseInt(status[0]);
		switch (statusCode) {
		case 1:
			strBuilder.append("�û��������벻��Ϊ��");
			break;
		case 2:
			strBuilder.append("�������쳣�������룺");
			strBuilder.append(status[1]);
			break;
		case 3: 
			handleSignInError(status[1],strBuilder,status[2]);
			break;
		case 4:
			strBuilder.append("�����쳣������������������");
			break;
		default:
			strBuilder.append("δ֪��������ϵ������Ա");
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
			strBuilder.append("δ֪��������ϵ������Ա");
			return;
		}
		int errorCode = Integer.parseInt(errorStr);
		if (errorCode == 9898) {
			//show tips of login success
			strBuilder.append("��¼�ɹ�");
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
			strBuilder.append("�û�������Ϊ��");
			break;
		case 102:
			strBuilder.append("���벻��Ϊ��");
			break;
		case 103: 
			strBuilder.append("xmlHttp����ʧ��");
			break;
		case 1001:
			strBuilder.append("���û��������ڣ������û����Ƿ���ȷ");
			break;
		case 1002:
			strBuilder.append("���û��ѱ������������������ϵվ����");
			break;
		case 1003:
			strBuilder.append("�������,���һ�����");
			break;
		default:
			strBuilder.append("δ֪��������ϵ������Ա");
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
