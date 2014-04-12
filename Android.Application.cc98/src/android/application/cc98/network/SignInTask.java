package android.application.cc98.network;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import android.application.cc98.SignInInterface;
import android.os.AsyncTask;

public class SignInTask extends AsyncTask<String, Integer, String[]> {
	
	private SignInInterface activity = null;
	
	public SignInTask(SignInInterface activity) {
		if (null == activity)
			return;
		this.activity = activity;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.activity.SignInPreProgress();
		
	}
		
	////////////////////////////////////////
	/////sign in cc98 forum
	//return value is status code
	//"0" not handled error, please communication tech support
	//"1" Log first time
	//"2" Network communication fail
	//"3" Network communication success
	//"4" Network communication exception
	@Override
	protected String[] doInBackground(String... inputs) {
		String[] resultList = new String[3];
		resultList[0] = "0";
		String username = inputs[0];
		String pwd = inputs[1];
		//check whether store user name and pwd
		if (username == null || pwd == null || username.length() == 0 || pwd.length() == 0) {
			resultList[0] = "1";
			return resultList;
		}
		String signURL = inputs[2];
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("a", "i");
		params.put("u", username);
		params.put("p", Md5Util.getMD5Str(pwd));
		params.put("userhidden","2");
		try {
			HttpResult response =  SendHttpRequest.sendPost(signURL, null, params, "utf-8");
			if (response.getStatusCode() == 200) {
				resultList[0] = "3";
				resultList[1] = EntityUtils.toString(response.getHttpEntity());
				if (resultList[1].equals("9898")) {
					//set cookies
					String resCookie = response.getCookie();
					resultList[2] = resCookie;
				}
			}
			else {
				resultList[0] = "2";
				resultList[1] = String.valueOf(response.getStatusCode());
			}
		}
		catch (ClientProtocolException e) {  
			resultList[0] = "4";
	        e.printStackTrace(); 
	     } 
	     catch (IOException e) {  
	    	 resultList[0] = "4";
		     e.printStackTrace(); 
	     } 
	     catch (Exception e) {  
	    	 resultList[0] = "4";
		     e.printStackTrace(); 
	     }  
		return resultList;	
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		this.activity.SignInProgressUpdate();
	}
	
	@Override
	protected void onPostExecute(String[] results) {
		super.onPostExecute(results);
		this.activity.SignInPostProgress(results);
	}
}
