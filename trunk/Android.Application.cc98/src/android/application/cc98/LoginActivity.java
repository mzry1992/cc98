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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {

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
        loginBT.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String username = "weihao1988";
				String pwd = "26071647";
				String httpUrl = "http://www.cc98.org/login.asp";
				
				// set up http post parameters
				HttpPost httpRequest = new HttpPost(httpUrl);
				
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("u", username));
				params.add(new BasicNameValuePair("p", pwd));
				params.add(new BasicNameValuePair("a", "i"));
				params.add(new BasicNameValuePair("userhidden", "2"));
				
				try {
					// set encodings
					HttpEntity httpEntity = new UrlEncodedFormEntity(params, "UTF-8");
					// set http request
					httpRequest.setEntity(httpEntity);
					// get default http client
					HttpClient httpClient = new DefaultHttpClient();
					// get http response
					HttpResponse httpResponse = httpClient.execute(httpRequest);
					
					// HttpStatus.SC_OK indicates post success
					if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
						// get return string from the site
						String strRes = EntityUtils.toString(httpResponse.getEntity());
						Intent switchIntent = new Intent(LoginActivity.this, TestWebView.class);
						switchIntent.putExtra("login page", strRes);
		    			startActivity(switchIntent);
					}
					else
					{
						;//
					}
				}
				catch (Exception e)
				{
					e.printStackTrace(); ;
				}
			}
		});
	}
}
