package android.application.cc98.network;

import java.io.ByteArrayInputStream;
import java.io.Console;
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

import android.app.Activity;
import android.application.cc98.GetWebPageInterface;
import android.os.AsyncTask;
import android.widget.Toast;

public class LeafBoardTask extends AsyncTask<String, Integer, ArrayList<ArrayList<String>>> {
	private GetWebPageInterface activity = null;
	private String serverName = null;
	
	public LeafBoardTask(GetWebPageInterface activity, String serverName) {
		if (null == activity)
			return;
		this.activity = activity;
		this.serverName = serverName;
	}
	
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
		String boardUrl = inputs[1];
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Cookie", cookie);
		res.add("0");
		
		try {
			HttpResult response = SendHttpRequest.sendGet(boardUrl, header, null, "utf-8");
			if (response.getStatusCode() == 200) {
				res.set(0, "3");
				String htmlText = EntityUtils.toString(response.getHttpEntity());
				res.add(htmlText);
				parseLeafBoardHtml(htmlText, outputs);
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
	
	private void parseLeafBoardHtml(String homePageHtml,
									ArrayList<ArrayList<String>> outputs) {
		try {
			InputStream instr = new ByteArrayInputStream(homePageHtml.getBytes());
			Document httpDoc = Jsoup.parse(instr, "UTF-8", serverName);
			Element body = httpDoc.body();
			Elements children = body.children();
			
			ArrayList<String> topicTitles = new ArrayList<String>();
			ArrayList<String> topicUrls = new ArrayList<String>();
			ArrayList<String> topicAdditions = new ArrayList<String>();
			
			ArrayList<String> boardInfo = new ArrayList<String>();
			
			// get custom board data and default board data from html
			int tableCnt = 0, formCnt = 0;
			for (Element child : children) {
				if (child.tagName().equals("form")) {
					formCnt++;
					if (formCnt == 1) {
						Elements topics = child.select("a[id]");
						
						for (Element topic : topics) {
							String content = topic.attr("title");
							String url = topic.attr("href");
							String[] attrs = content.split("\n");
							String title = attrs[0].trim();
							String addtion = attrs[1] + "\n" + attrs[2];
							
							assert(title.length() > 2);
							title = title.substring(1, title.length() - 1);
							topicTitles.add(title);
							topicUrls.add(url);
							topicAdditions.add(addtion);
						}
					}
					else if (formCnt == 2) {
						Element td = child.getElementsByTag("td").first();
						String topicNumberStr = td.getElementsByTag("b").last().text().trim();
						boardInfo.add(topicNumberStr);
					}
					else continue;
				}
				else if (child.tagName().equals("table")) {
					tableCnt++;
					// get board name
					if (tableCnt == 3) {
						Elements links = child.select("a[href]");
						StringBuilder sb = new StringBuilder();
						for (Element link : links) {
							String str = link.text();
							if (str.contains("Ìû×ÓÁÐ±í")) break;
							sb.append(str + "->");
						}
						//System.out.println("Test Board name!!!!!");
						String str = sb.toString();
						//System.out.println(str);
						boardInfo.add(str.substring(0, str.length() - 2));
					}
					else continue;
				}
			}
			
			outputs.add(topicTitles);
			outputs.add(topicUrls);
			outputs.add(topicAdditions);
			outputs.add(boardInfo);
			//System.out.println("BoardInfo Size:" + boardInfo.size() + boardInfo.get(0));
			/*System.out.println("Finish parsing... Topic count:" + topicTitles.size()
					 + ":" + topicUrls.size() + ":" + topicAdditions.size());*/
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
