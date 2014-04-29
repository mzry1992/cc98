package android.application.cc98.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.application.cc98.GetWebPageInterface;
import android.application.cc98.view.Utility;
import android.os.AsyncTask;
import android.widget.Toast;

public class BBSListTask extends AsyncTask<String, Integer, ArrayList<ArrayList<String>>> {
	
	private GetWebPageInterface activity = null;
	
	public BBSListTask(GetWebPageInterface activity) {
		if (null == activity)
			return;
		BBSListTask.this.activity = activity;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.activity.getWebPagePreProgress();
		
	}
	
	
	////////////////////////////////////////
	/////get bbs list
	//return value is status code
	//"0" not handled error, please communication tech support
	//"1" not login
	//"2" Network communication fail
	//"3" Network communication success
	//"4" Network communication exception
	@Override
	protected ArrayList<ArrayList<String>> doInBackground(String... inputs) {
		ArrayList<ArrayList<String>> outputs = new ArrayList<ArrayList<String>>();
		
		ArrayList<String> resultList = new ArrayList<String>();
		outputs.add(resultList);
		
		String bbsListURL = inputs[0];
		String cookie = inputs[1];
		HashMap<String, String> header = new HashMap<String, String>(); 
        header.put("Cookie",cookie);
        
        resultList.add("0");
		resultList.add("tmp");
        
		try {
			HttpResult response =  SendHttpRequest.sendGet(bbsListURL, header, null, "utf-8");
			if (response.getStatusCode() == 200) {
				resultList.set(0, "3");
				String pageHtml = EntityUtils.toString(response.getHttpEntity());
				//parse html page content
				parseWebPage(pageHtml,outputs,resultList);
			}
			else {
				resultList.set(0, "2");
				resultList.set(1, String.valueOf(response.getStatusCode()));
			}
		}
		catch (ClientProtocolException e) {  
			resultList.set(0, "4");
	        e.printStackTrace(); 
	     } 
	     catch (IOException e) {  
	    	 resultList.set(0, "4");
		     e.printStackTrace(); 
	     } 
	     catch (Exception e) {  
	    	 resultList.set(0, "4");
		     e.printStackTrace(); 
	     }  
		return outputs;	
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		this.activity.getWebPageProgressUpdate();
	}
	
	@Override
	protected void onPostExecute(ArrayList<ArrayList<String>> outputs) {
		super.onPostExecute(outputs);
		this.activity.getWebPagePostProgress(outputs);
	}
	
	//page have two situations:1¡¢not login, 2¡¢login
	//if not login, jump to login page
	//if login in, analyze page, and get useful info 
	private void parseWebPage(String pageHtml, ArrayList<ArrayList<String>> outputs, ArrayList<String> resultList) {
		Document httpDoc = Jsoup.parse(pageHtml);
		Element body = httpDoc.body();
		String loginName = Utility.parseLoginName(body);
		if (loginName.equals("Î´µÇÂ¼")) {
			resultList.set(0, "1");
			return;
		}
		parseHtml(body,outputs);
	}
	
	
	private void parseHtml(Element body, ArrayList<ArrayList<String>> outputs) {
		
		ArrayList<String> pageTitle = new ArrayList<String>();
		ArrayList<String> bordTitleArrayList = new ArrayList<String>();
		ArrayList<String> infoArrayList = new ArrayList<String>();
		ArrayList<String> linkArrayList = new ArrayList<String>();

		Elements children = body.children();
		
		int tableCnt = 0;
		Element rootTable = null;
		// get root table
		for (Element child : children) {
			if (child.tagName().equals("table")) {
				tableCnt++;
				if (tableCnt == 4) {
					rootTable = child;
					break;
				}
			}
		}
		//get every row of root table
		Elements rows = rootTable.children();
		while (rows.size() < 2 && rows.size() > 0)
			rows = rows.get(0).children();
		//get page name
		Element firstRow = rows.get(0);
		Elements tdElems = firstRow.getElementsByTag("th");
		pageTitle.add(tdElems.get(0).text());
		//get content
		for (int i = 1; i < rows.size(); ++i) {
			Element rootRow = rows.get(i);
			Elements subrows = rootRow.children();
			while (subrows.size() < 2 && subrows.size() > 0)
				subrows = subrows.get(0).children();
			//find root column
			Element rootCol = subrows.get(2);
			subrows = rootCol.children();
			while (subrows.size() < 2 && subrows.size() > 0)
				subrows = subrows.get(0).children();
			Element firstSubRow = subrows.get(0);
			Element secondSubRow = subrows.get(1);
			Elements firstSubRowElems = firstSubRow.getElementsByTag("a");
			Elements titleRows = firstSubRow.getElementsByTag("td");
			if (titleRows.size() > 1)
				bordTitleArrayList.add(titleRows.get(0).text());
			infoArrayList.add(secondSubRow.text());
			linkArrayList.add(firstSubRowElems.get(0).attr("href"));
		}
		
		outputs.add(pageTitle);
		outputs.add(bordTitleArrayList);
		outputs.add(infoArrayList);
		outputs.add(linkArrayList);
	}

}
