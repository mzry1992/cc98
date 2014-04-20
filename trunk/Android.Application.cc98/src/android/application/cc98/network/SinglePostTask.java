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
	// 2. record the post contents
	// 3. record the post timestamps
	// Currently do not support images. Add this function later one
	private void parseSinglePostHtml(String singlePostHtml,
									 ArrayList<ArrayList<String>> outputs) {
		try {
			/*int idx = 0, brCnt = 0;
			while (true) {
				idx = singlePostHtml.indexOf("<br>", idx + 1);
				if (idx >= 0 && idx < singlePostHtml.length()) brCnt++;
				else break;
			}
			//System.out.println("Html has <br> count: " + brCnt);
			
			singlePostHtml = singlePostHtml.replace("<br>", "\n");
			singlePostHtml = singlePostHtml.replace("\n\n", "\n");*/
			InputStream instr = new ByteArrayInputStream(singlePostHtml.getBytes());
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
			ArrayList<String> timestamps = new ArrayList<String>();
			getPostsDetails(tables, authors, contents, timestamps);
			outputs.add(authors);
			outputs.add(contents);
			outputs.add(timestamps);
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
				Element bElem = elem.select("b").first();
				postCnt = Integer.parseInt(bElem.text());
				break;
			}
		}
		//System.out.println("Post count: " + postCnt);
		return postCnt;
	}
	// get post title
	private String getSinglePostTitle(Element table) {
		Element elem = table.select("th").first();
		String text = elem.text();
		int idx = text.indexOf("Ìû×ÓÖ÷Ìâ£º");
		String title = text.substring(idx + 5).trim();
		//System.out.println("Post title: " + title);
		return title;
	}
	// get posts details
	private void getPostsDetails(ArrayList<Element> tables,
								ArrayList<String> authors,
								ArrayList<String> contents,
								ArrayList<String> timestamps) {
		for (Element table : tables) {
			Element tbody = table.children().first();
			Element tr1 = tbody.child(0);
			Element tr2 = tbody.children().last();
			
			{ // author name
				Element td1 = tr1.child(0);
				Element name = td1.select("a[name]").first();
				Element author = name.select("b").first();
				authors.add(author.text());
				//System.out.println("Author:" + author.text());
			}
			
			{ // content
				Element td2 = tr1.child(1);
				Element bq = td2.select("blockquote").first();
				Element td = bq.select("td").first();
				String title = td.select("b").first().text().trim();
				StringBuilder sb = new StringBuilder();
				if (title.length() > 0) {
					sb.append(title);
					sb.append("\n\n");
				}
				Element span = td.select("span").first();
				while (true) {
					String content = span.text().trim();
					if (content.length() > 0) {
						sb.append(content);
						sb.append("\n");
					}
					if (span.children().size() != 0)
						span = span.child(0);
					else break;
				}
				String contentStr = removeBrackets(sb.toString());
				contents.add(contentStr);
				//System.out.println("Content:" + contentStr);
			}		
			
			{ // time post
				Element td = tr2.children().first();
				String timestamp = td.text().trim();
				timestamps.add(timestamp);
				//System.out.println("Timestamp:" + timestamp);
			}
		}
	}

	private String removeBrackets(String text) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		//int cnt = 0;
		while (i < text.length()) {
			if (text.charAt(i) == '[') {
				//cnt++;
				while ( i < text.length() &&
						text.charAt(i++) != ']');
				continue;
			}
			else
				sb.append(text.charAt(i++));
		}
		//System.out.println("Bracket Count: " + cnt);
		String str = sb.toString();
		//System.out.println("Remove bracket: " + str);
		
		return str;
	}
}
