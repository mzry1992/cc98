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
import android.os.AsyncTask;

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
	// "1"
	//"2" Network communication fail
	//"3" Network communication success, load the first page
	//"5" Network communication success, load the following page
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
				if (boardUrl.charAt(boardUrl.length() - 1) == '1')
					res.set(0, "3");
				else
					res.set(0, "5");
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
	
	// boardInfo: 	0: boardName(nested),
	//				1: total posts number
	//				2: total pages number
	private void parseLeafBoardHtml(String homePageHtml,
									ArrayList<ArrayList<String>> outputs) {
		try {
			InputStream instr = new ByteArrayInputStream(homePageHtml.getBytes());
			Document httpDoc = Jsoup.parse(instr, "UTF-8", serverName);
			Element body = httpDoc.body();
			Elements children = body.children();
			
			ArrayList<String> boardInfo = new ArrayList<String>();
			boardInfo.add("");boardInfo.add("");boardInfo.add("");
			ArrayList<String> topicTitles = new ArrayList<String>();
			ArrayList<String> topicUrls = new ArrayList<String>();
			ArrayList<String> topicAdditions = new ArrayList<String>();
			
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
							
							// get article title
							String title = attrs[0].trim();
							assert(title.length() >= 2);
							title = title.substring(1);
							if (title.charAt(title.length() - 1) == '》')
								title = title.substring(0, title.length() - 1);
							
							// get additional information
							StringBuilder additionSb = new StringBuilder();
							for (int i = 1; i < attrs.length; i++) {
								if (attrs[i].startsWith("作者：")) {
									additionSb.append(attrs[i]);
									additionSb.append("\n");
								}
								else if (attrs[i].startsWith("发表于"))
									additionSb.append(attrs[i]);
							}
							
							topicTitles.add(title);
							topicUrls.add(url);
							topicAdditions.add(additionSb.toString());
						}
					}
					else if (formCnt == 2) {
						Element td = child.getElementsByTag("td").first();
						Elements bs = td.getElementsByTag("b");
						String pageNumberStr = bs.get(1).text();
						String topicNumberStr = bs.last().text().trim();
						boardInfo.set(1, topicNumberStr);
						boardInfo.set(2, pageNumberStr);
					}
					else continue;
				}
				else if (child.tagName().equals("table")) {
					tableCnt++;
					// get board name
					if (tableCnt == 3) {
						Elements links = child.select("a[href]");
						StringBuilder sb = new StringBuilder(" ");
						for (Element link : links) {
							String str = link.text();
							if (str.contains("帖子列表")) break;
							if (str.equals("www.cc98.org")) continue;
							sb.append(str + " → ");
						}
						//System.out.println("Test Board name!!!!!");
						String str = sb.toString();
						//System.out.println(str);
						boardInfo.set(0, str.substring(0, str.length() - 2));
					}
					else continue;
				}
			}
			
			outputs.add(boardInfo);
			outputs.add(topicTitles);
			outputs.add(topicUrls);
			outputs.add(topicAdditions);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
