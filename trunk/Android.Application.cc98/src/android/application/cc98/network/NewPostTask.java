package android.application.cc98.network;

import java.io.IOException;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import android.application.cc98.GetWebPageInterface;
import android.os.AsyncTask;

public class NewPostTask extends AsyncTask<String, Integer, String[]>{

	private GetWebPageInterface activity = null;
	
	public NewPostTask(GetWebPageInterface activity) {
		if (null == activity)
			return;
		this.activity = activity;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.activity.getWebPagePreProgress();
		
	}

	////////////////////////////////////////
	// single post reply
	//return value is status code
	//"0" not handled error, please communication tech support
	//"1" 
	//"2" Network communication fail
	//"3" Network communication success
	//"4" Network communication exception
	@Override
	protected String[] doInBackground(String... inputs) {
		String[] resultList = new String[3];
		resultList[0] = "0";
		String postUrl = inputs[0];
		String username = inputs[1];
		String pwd = inputs[2];
		String subject = inputs[3];
		String content = inputs[4];
		String referer = inputs[5];
		String cookie = inputs[6];
		String expSrc = inputs[7];
		StringBuilder cookieSb = new StringBuilder();
		cookieSb.append("BoardList=BoardID=Show; owaenabled=True; autoplay=True; ");
		cookieSb.append(cookie);
		cookieSb.append("; upNum=0");
		cookie = cookieSb.toString();
		
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("Content", content);
		params.put("Expression", expSrc);
		params.put("passwd", pwd);
		params.put("signflag", "yes");
		params.put("subject", subject);
		params.put("upfilerename", "");
		params.put("UserName", username);
		
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Accept", "text/html, application/xhtml+xml, */*");
		header.put("Referer", referer);
		header.put("Accept-Language", "zh-CN, en-GB");
		header.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
		header.put("Content-Type","application/x-www-form-urlencoded");
		header.put("Accept-Encoding", "gzip, deflate");
		header.put("Host", "www.cc98.org");
		header.put("Cookie", cookie);
		
		/*System.out.println("PostURL:" + postUrl);
		System.out.println("Content:" + content);
		System.out.println("subject:" + subject);
		System.out.println("passwd:" + pwd);
		System.out.println("UserName:" + username);
		System.out.println("Cookie:" + cookie);
		System.out.println("Referer:" + referer);*/
		
		try {
			HttpResult response =  SendHttpRequest.sendPost(postUrl, header, params, "UTF-8");
			if (response.getStatusCode() == 200) {
				resultList[0] = "3";
				resultList[1] = EntityUtils.toString(response.getHttpEntity());
				resultList[2] = response.getCookie();
				/*if (resultList[1].contains("发表帖子成功"))
					resultList[0] = "4";*/
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
		this.activity.getWebPageProgressUpdate();
	}
	
	@Override
	protected void onPostExecute(String[] results) {
		super.onPostExecute(results);
		this.activity.getWebPagePostProgress(results);
	}	
}
