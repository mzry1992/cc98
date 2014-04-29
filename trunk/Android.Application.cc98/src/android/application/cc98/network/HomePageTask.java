package android.application.cc98.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.application.cc98.GetWebPageInterface;
import android.application.cc98.R;
import android.os.AsyncTask;
import android.util.Log;

public class HomePageTask extends AsyncTask<String, Integer, ArrayList<ArrayList<String>>> {

	private GetWebPageInterface activity = null;
	private String serverName = null;
	
	public HomePageTask(GetWebPageInterface activity, String serverName) {
		if (null == activity)
			return;
		this.activity = activity;
		this.serverName = serverName;
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
	protected ArrayList<ArrayList<String>> doInBackground(String... inputs) {
		ArrayList<ArrayList<String>> outputs = new ArrayList<ArrayList<String>>();
		ArrayList<String> res = new ArrayList<String>();
		outputs.add(res);
		
		String cookie = inputs[0];
		String url = inputs[1];
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Cookie", cookie);
		
		res.add("0");
		
		try {
			HttpResult response = SendHttpRequest.sendGet(url, header, null, "utf-8");
			if (response.getStatusCode() == 200) {
				res.set(0, "3");
				String htmlText = EntityUtils.toString(response.getHttpEntity());
				res.add(htmlText);
				parseHomePageHtml(htmlText, outputs);
			}
			else {
				res.set(0, "2");
			}
		}
		catch (ClientProtocolException e) {  
			res.set(0, "4");
	        e.printStackTrace(); 
	    } 
	    catch (IOException e) {  
	    	res.set(0, "4");
		     e.printStackTrace(); 
	    } 
	    catch (Exception e) {  
	    	res.set(0, "4");
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
	
	private void parseHomePageHtml(String homePageHtml, ArrayList<ArrayList<String>> outputs) {
		try {
			InputStream instr = new ByteArrayInputStream(homePageHtml.getBytes());
			Document httpDoc = Jsoup.parse(instr, "UTF-8", serverName);
			Element body = httpDoc.body();
			Elements children = body.children();
			
			ArrayList<String> customBoardNames = new ArrayList<String>();
			ArrayList<String> customBoardUrls = new ArrayList<String>();
			ArrayList<String> customBoardDescripts = new ArrayList<String>();
			
			ArrayList<String> defaultBoardNames = new ArrayList<String>();
			ArrayList<String> defaultBoardUrls = new ArrayList<String>();

			
			int tableCnt = 0;
			// get custom board data and default board data from html
			for (Element child : children) {
				if (child.tagName().equals("table")) {
					tableCnt++;
					switch (tableCnt) {
					case 4:
						getCustomBoardNames(child, customBoardNames, customBoardUrls, customBoardDescripts);
						break;
					case 5:
						getDefaultBoardNames(child, defaultBoardNames, defaultBoardUrls);
						break;				
					}	
				}
			}
			
			outputs.add(customBoardNames);
			outputs.add(customBoardUrls);
			outputs.add(customBoardDescripts);
			outputs.add(defaultBoardNames);
			outputs.add(defaultBoardUrls);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void getCustomBoardNames(Element table, 
									 ArrayList<String> boardNames,
									 ArrayList<String> boardUrls,
									 ArrayList<String> boardDescripts) {
		Element tbody = table.children().first();
		Elements children = tbody.children();
		
		boolean isTitle = true;
		for (Element child : children) {
			if (!isTitle && child.tagName().equals("tr")) {
				
				// get each custom board, with url and description
				Elements elems = child.select("a[href]");
				for (Element elem : elems) {
					String link = elem.attr("href");
					if (link.startsWith("list.asp?boardid=")) {
						boardNames.add(elem.parent().text());
						boardUrls.add(link);
					}		
				}
				
				// get each custom board description
				Elements tds = child.getElementsByTag("td");
				for (Element td : tds) {
					if (td.children().first() == null || !td.hasText()) continue;
					Element imgSrc = td.children().first();
					if (imgSrc.tagName().equals("img") &&
						imgSrc.attr("src").equals("pic/Forum_readme.gif")) {
						boardDescripts.add(td.text());
					}
				}
			}
			else
				isTitle = false;
		}
		
	}
	
	private void getDefaultBoardNames(Element table, ArrayList<String> boardNames, ArrayList<String> boardUrls) {
		Element tbody = table.children().first();
		Elements elems = tbody.select("a[href]");
		
		//int cnt = 0;
		for (Element elem : elems) {
			String link = elem.attr("href");
			if (link.startsWith("list.asp?boardid=")) {
				boardNames.add(elem.text());
				boardUrls.add(link);
				//cnt++;
			}
		}
		
		/*Toast.makeText(this, "Default Board Count:" + cnt, Toast.LENGTH_LONG).show();
		
		StringBuilder sb1 = new StringBuilder();
		for (String str : boardNames)
			sb1.append(str + "\n");
		Toast.makeText(this, sb1.toString(), Toast.LENGTH_LONG).show();
		
		
		StringBuilder sb2 = new StringBuilder();
		for (String str : boardUrls)
			sb2.append(str + "\n");
		Toast.makeText(this, sb2.toString(), Toast.LENGTH_LONG).show();*/
	}
}
