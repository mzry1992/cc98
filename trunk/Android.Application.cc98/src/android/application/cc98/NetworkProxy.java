package android.application.cc98;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class NetworkProxy {
	
	private String userName;
	private String pwd;
	private String cookie;
	private Activity activity;
	
	public NetworkProxy(Activity activity) {
		this.activity = activity;
		this.userName = null;
		this.pwd = null;
		this.cookie = null;
		GetUserInfo();
		GetCookieInfo();
	}
	
	////////////////////////////////////////
	/////sign in cc98 forum
	//return value is status code
	//"0" not handled error, please communication tech surrport
	//"1" Log first time
	//"2" Network communication fail
	//"3" Network communication sucess
	//"4" Network communication exception
	public String[] trySign(String userName, String pwd) {
		String[] resultList = new String[2];
		if (userName != null && pwd != null)
		{
			this.userName = userName;
			this.pwd = pwd;
		}
		//check user invoke correctly
		if (this.activity == null)
		{
			resultList[0] = "0";
			return resultList;
		}
		//check whether store user name and pwd
		if (this.userName == null || this.pwd == null || this.userName.length() == 0 || this.pwd.length() == 0)
		{
			resultList[0] = "1";
			return resultList;
		}
		int statusCode = 0;
		String signURL = getSignURL();
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("a", "i");
		params.put("u", this.userName);
		params.put("p", Md5Util.getMD5Str(this.pwd));
		params.put("userhidden","2");
		try {
			HttpResult response =  SendHttpRequest.sendPost(signURL, null, params, "utf-8");
			if (response.getStatusCode() == 200)
			{
				resultList[0] = "3";
				resultList[1] = EntityUtils.toString(response.getHttpEntity());
				//set cookies
				String resCookie = response.getCookie();
				SetCookieInfo(resCookie);
				//set user info
				if (userName != null && pwd != null)
					SetUserInfo(userName,pwd);
			}
			else
			{
				resultList[0] = "2";
				resultList[1] = String.valueOf(response.getStatusCode());
			}
		}
		catch (ClientProtocolException e) 
	     {  
			resultList[0] = "4";
	        e.printStackTrace(); 
	     } 
	     catch (IOException e) 
	     {  
	    	 resultList[0] = "4";
		     e.printStackTrace(); 
	     } 
	     catch (Exception e) 
	     {  
	    	 resultList[0] = "4";
		     e.printStackTrace(); 
	     }  
		return resultList;
	}
	
	private String getSignURL() {
		if (this.activity == null)
			return null;
		StringBuilder signURLBuilder = new StringBuilder();
		signURLBuilder.append("http://");
		signURLBuilder.append(this.activity.getString(R.string.serverName)); 
		signURLBuilder.append("/");
		signURLBuilder.append(this.activity.getString(R.string.signSuffix));
		String signURL = signURLBuilder.toString();
		return signURL;
	}
	
	private void GetUserInfo() {
		if (this.activity == null)
			return;
		SharedPreferences userSettings = this.activity.getSharedPreferences(this.activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_READABLE);  
		this.userName = userSettings.getString(this.activity.getString(R.string.userNameFile), null);
		this.pwd = userSettings.getString(this.activity.getString(R.string.passwordFile), null);
	}
	
	private void GetCookieInfo() {
		if (this.activity == null)
			return;
		SharedPreferences userSettings = this.activity.getSharedPreferences(this.activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_READABLE);  
		this.cookie = userSettings.getString(this.activity.getString(R.string.cookieFile), null);
	}
	
	private void SetUserInfo(String userName, String pwd) {
		if (this.activity == null)
			return;
		Editor userSettingsEditor = this.activity.getSharedPreferences(this.activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_WRITEABLE).edit();  
		userSettingsEditor.putString(this.activity.getString(R.string.userNameFile), userName);
		userSettingsEditor.putString(this.activity.getString(R.string.passwordFile), pwd);
		userSettingsEditor.commit();
	}
	
	private void SetCookieInfo(String cookie) {
		if (this.activity == null)
			return;
		Editor userSettingsEditor = this.activity.getSharedPreferences(this.activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_WRITEABLE).edit();  
		userSettingsEditor.putString(this.activity.getString(R.string.cookieFile), cookie);
		userSettingsEditor.commit();
	}

}
