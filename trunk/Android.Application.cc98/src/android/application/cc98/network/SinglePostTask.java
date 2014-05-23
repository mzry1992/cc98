package android.application.cc98.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.application.cc98.GetWebPageInterface;
import android.os.AsyncTask;

public class SinglePostTask extends AsyncTask<String, Integer, ArrayList<ArrayList<String>>> {

	private GetWebPageInterface activity = null;
	private String serverName = null;
	
	public SinglePostTask(GetWebPageInterface activity, String serverName) {
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
	//"3" Network communication success, first post page loading
	//"5" Network communication success, following post page loading	
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
				if (url.charAt(url.length() - 1) == '1')
					res.set(0, "3");
				else
					res.set(0, "5");
				String htmlText = EntityUtils.toString(response.getHttpEntity());
				res.add(htmlText);
				parseSinglePostHtml(htmlText, outputs);
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
	
	/// outputs:
	// 0. record the post count + page count + post title
	// 1. record the post authors
	// 2. record the post references
	// 3. record the post contents
	// 4. record the post timestamps
	// 5. record the replyID
	// 6. record the raw contents
	// Currently do not support images. Add this function later one
	private void parseSinglePostHtml(String singlePostHtml,
									 ArrayList<ArrayList<String>> outputs) {
		try {
			singlePostHtml = singlePostHtml.replace("\n\n", "\n");
			InputStream instr = new ByteArrayInputStream(singlePostHtml.getBytes());
			/*System.out.println("instr:" + instr);
			System.out.println("serverName:" + serverName);*/
			Document httpDoc = Jsoup.parse(instr, "UTF-8", serverName);
			Element body = httpDoc.body();
			Elements children = body.children();
			
			ArrayList<Element> tables = new ArrayList<Element>();
			for (Element child: children) {
				if (child.tagName().equals("table"))
					tables.add(child);
			}
			
			// remove the headers
			for (int i = 0; i < 4; i++) tables.remove(0);
			// remove the tails
			for (int i = 0; i < 4; i++) tables.remove(tables.size() - 1);
			
			assert(tables.size() >= 3);
			// get post/page count and post title
			int postCnt = getSinglePostNumber(tables.get(0));
			int pageCnt = postCnt / 10 + ((postCnt % 10 == 0)? 0 : 1);
			String postTitle = getSinglePostTitle(tables.get(1));
			ArrayList<String> postInfo = new ArrayList<String>();
			postInfo.add(Integer.toString(postCnt));
			postInfo.add(Integer.toString(pageCnt));
			postInfo.add(postTitle);
			outputs.add(postInfo);
			for (int i = 0; i < 2; i++) tables.remove(0);
			
			// get each post information
			ArrayList<String> authors = new ArrayList<String>();
			ArrayList<String> contents = new ArrayList<String>();
			//ArrayList<String> references = new ArrayList<String>();
			ArrayList<String> timestamps = new ArrayList<String>();
			ArrayList<String> replyIDs = new ArrayList<String>();
			ArrayList<String> rawContents = new ArrayList<String>();
			getPostsDetails(tables, authors, /*references,*/ contents, timestamps, replyIDs, rawContents);
			outputs.add(authors);
			//outputs.add(references);
			outputs.add(contents);
			outputs.add(timestamps);
			outputs.add(replyIDs);
			outputs.add(rawContents);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// get post count
	private int getSinglePostNumber(Element table) {
		int postCnt = 0;
		Elements elems = table.select("span[id]");
		for (Element elem : elems) {
			if (elem.attr("id").equals("topicPagesNavigation")) {
				Element bElem = elem.getElementsByTag("b").first();
				postCnt = Integer.parseInt(bElem.text());
				break;
			}
		}
		//System.out.println("Post count: " + postCnt);
		return postCnt;
	}
	// get post title
	private String getSinglePostTitle(Element table) {
		Element elem = table.getElementsByTag("th").first();
		String text = elem.text();
		int idx = text.indexOf("帖子主题：");
		String title = text.substring(idx + 5).trim();
		//System.out.println("Post title: " + title);
		return title;
	}
	// get posts details
	private void getPostsDetails(ArrayList<Element> tables,
								ArrayList<String> authors,
								//ArrayList<String> references,
								ArrayList<String> contents,
								ArrayList<String> timestamps,
								ArrayList<String> replyIDs,
								ArrayList<String> rawContents) {
		for (Element table : tables) {
			Element tbody = table.children().first();
			Element tr1 = tbody.child(0);
			Element tr2 = tbody.children().last();
			
			// author name
			Element td1 = tr1.child(0);
			{ 
				Element name = td1.select("a[name]").first();
				if (name != null) {
				Element author = name.getElementsByTag("b").first();
				authors.add(author.text());
				}
				else {
					Element span1 = td1.getElementsByTag("span").first();
					Element author = span1.getElementsByTag("b").first();
					authors.add(author.text());
				}
			}
			
			// contents
			Element td2 = tr1.child(1);
			{ 
				// replyID
				Elements urls = td2.select("a[href]"); 
				for (Element url : urls) {
					String link = url.attr("href");
					if (link.contains("replyID")) {
						int idx = link.indexOf("replyID");
						int idx1 = link.indexOf('=', idx);
						int idx2 = link.indexOf('&', idx1);
						String replyID = link.substring(idx1 + 1, idx2);
						//System.out.println("ReplyID : " + replyID);
						replyIDs.add(replyID);
						break;
					}
				}
				
				// content
				Element bq = td2.getElementsByTag("blockquote").first();
				Element td = bq.getElementsByTag("td").first();
				String title = td.getElementsByTag("b").first().text().trim();
				StringBuilder contentSb = new StringBuilder();
				if (title.length() > 0) {
					contentSb.append(title);
					contentSb.append("\n\n");
				}
				Element span = td.getElementsByTag("span").first();
				String text = span.html().trim();
				
				/*if (text.startsWith("[quotex]")) {
					//System.out.println("Reference:" + text);
					int idx = text.indexOf("[/quotex]");
					int idx1 = text.indexOf("[/b]");
					StringBuilder referStrSb = new StringBuilder();
					referStrSb.append(text.substring(0, idx1));
					referStrSb.append('\n');
					referStrSb.append(text.substring(idx1 + 4, idx));
					String referStr = referStrSb.toString();
					String contentStr = text.substring(idx + 9);
					
					rawContents.add(contentStr);
					referStr = adjustText(referStr);
					contentStr = adjustText(contentStr);
					
					contentSb.append(contentStr);
					contents.add(contentSb.toString().trim());
					//references.add(referStr.trim()/* + "\n");
				}
				else */
				{
					rawContents.add(text);
					text = adjustText(text);
					contentSb.append(text.trim());
					contents.add(contentSb.toString());
					//references.add("");
				}
			}		
			
			// time post
			{ 
				Element td = tr2.children().first();
				String timestamp = td.text().trim();
				timestamps.add(timestamp);
				//System.out.println("Timestamp:" + timestamp);
			}
		}
	}

	private String adjustText(String text) {
		text = removeBR(text);
		text = StringEscapeUtils.unescapeHtml4(text);
		text = removeBrackets(text);
		//text = removeBR(text);
		return text;
	}
	
	private String removeBrackets(String text) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		//int cnt = 0;
		while (i < text.length()) {
			/*if (text.charAt(i) == '[') {
				if (   i + 1 < text.length() && text.charAt(i + 1) == 'e' 
					&& i + 2 < text.length() && text.charAt(i + 2) == 'm')
					sb.append(text.charAt(i++));
				else {
					int idx = text.indexOf(']', i);
					if (idx != -1 && idx > i + 1) {
						i = idx + 1;
						continue;
					}
				}
			}*/
			if (text.charAt(i) == '<') {
				int idx = text.indexOf('>', i);
				if (idx != -1 && idx > i + 1) {
/*					CharSequence charset = text.subSequence(i + 1, idx - 1);
					boolean allChar = true;
					for (int k = 0; k < charset.length(); k++) {
						if (charset.charAt(k) > 128) {
							allChar = false;
							break;
						}
					}
					if (allChar) {*/
					i = idx + 1;
					continue;
				}
			}
			sb.append(text.charAt(i++));
		}
		String str = sb.toString();
		return str;
	}
	
	private String removeBR(String text) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		boolean beforeBR = false;
		while (i < text.length()) {
			if (text.charAt(i) == '<'
				&& i + 1 < text.length() && text.charAt(i + 1) == 'b'
				&& i + 2 < text.length() && text.charAt(i + 2) == 'r'
				&& i + 3 < text.length() && text.charAt(i + 3) == ' '
				&& i + 4 < text.length() && text.charAt(i + 4) == '/') {
				if (beforeBR == false)
					sb.append("<br />");
				beforeBR = true;
				i = i + 6;
			}
			else {
				beforeBR = false;
				sb.append(text.charAt(i++));
			}
		}
		String str = sb.toString();
		if (str.endsWith("<br />"))
			str = str.substring(0, str.length() - 6);
		str = str.replaceAll("<br />", "\n").trim();
		return str;
	}
	
}
