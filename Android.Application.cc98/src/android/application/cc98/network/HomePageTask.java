package android.application.cc98.network;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import android.application.cc98.GetWebPageInterface;
import android.os.AsyncTask;
import android.util.Log;

public class HomePageTask extends AsyncTask<String, Integer, String[]> {

private GetWebPageInterface activity = null;
	
	public HomePageTask(GetWebPageInterface activity) {
		if (null == activity)
			return;
		this.activity = activity;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.activity.getWebPagePreProgress();
		
	}
	///////////////////////////////////////////////
	//// get home page of cc98 forum
	// return value is status code
	//"0" not handled error, please communication tech support
	//"2" Network communication fail
	//"3" Network communication success
	//"4" Network communication exception
	@Override
	protected String[] doInBackground(String... inputs) {
		String[] res = new String[2];
		String cookie = inputs[0];
		String url = inputs[1];
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Cookie", cookie);
		
		Log.i("HomePageTask -- Cookie", cookie);
		Log.i("HomePageTask -- url", url);
		// initial status
		res[0] = "0";
		
		try {
			HttpResult response = SendHttpRequest.sendGet(url, header, null, "utf-8");
			if (response.getStatusCode() == 200) {
				res[0] = "3";
				String htmlText = EntityUtils.toString(response.getHttpEntity());
				res[1] = htmlText;
			}
			else {
				res[0] = "2";
			}
		}
		catch (ClientProtocolException e) {  
			res[0] = "4";
	        e.printStackTrace(); 
	    } 
	    catch (IOException e) {  
	    	 res[0] = "4";
		     e.printStackTrace(); 
	    } 
	    catch (Exception e) {  
	    	 res[0] = "4";
		     e.printStackTrace(); 
	    } 
		
		return res;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		this.activity.getWebPageProgressUpdate();
	}
	
	@Override
	protected void onPostExecute(String[] results) {
		super.onPostExecute(results);
		this.activity.getWebPagePostProgress(results);
	}
}
