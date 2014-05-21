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

import android.application.cc98.GetMultiWebPageInterface;
import android.application.cc98.GetWebPageInterface;
import android.application.cc98.view.Utility;
import android.os.AsyncTask;

public class HotPostTask extends
		AsyncTask<String, Integer, ArrayList<ArrayList<String>>> {

	private GetWebPageInterface activity = null;


	public HotPostTask(GetWebPageInterface activity) {
		if (null == activity)
			return;
		HotPostTask.this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.activity.getWebPagePreProgress();

	}

	// //////////////////////////////////////
	// ///get bbs list
	// return value is status code
	// "0" not handled error, please communication tech support
	// "1" not login
	// "2" Network communication fail
	// "3" Network communication success
	// "4" Network communication exception
	@Override
	protected ArrayList<ArrayList<String>> doInBackground(String... inputs) {
		ArrayList<ArrayList<String>> outputs = new ArrayList<ArrayList<String>>();

		ArrayList<String> resultList = new ArrayList<String>();
		outputs.add(resultList);

		String hotPostURL = inputs[0];
		String cookie = inputs[1];
		HashMap<String, String> header = new HashMap<String, String>();
		header.put("Cookie", cookie);

		resultList.add("0");
		resultList.add("tmp");

		try {
			HttpResult response = SendHttpRequest.sendGet(hotPostURL, header,
					null, "utf-8");
			if (response.getStatusCode() == 200) {
				resultList.set(0, "3");
				String pageHtml = EntityUtils
						.toString(response.getHttpEntity());
				// parse html page content
				parseWebPage(pageHtml, outputs, resultList);
			} else {
				resultList.set(0, "2");
				resultList.set(1, String.valueOf(response.getStatusCode()));
			}
		} catch (ClientProtocolException e) {
			resultList.set(0, "4");
			e.printStackTrace();
		} catch (IOException e) {
			resultList.set(0, "4");
			e.printStackTrace();
		} catch (Exception e) {
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

	// page have two situations:1、not login, 2、login
	// if not login, jump to login page
	// if login in, analyze page, and get useful info
	private void parseWebPage(String pageHtml,
			ArrayList<ArrayList<String>> outputs, ArrayList<String> resultList) {
		Document httpDoc = Jsoup.parse(pageHtml);
		Element body = httpDoc.body();
		String loginName = Utility.parseLoginName(body);
		if (loginName.equals("未登录")) {
			resultList.set(0, "1");
			return;
		}
		parseHtml(body, outputs);
	}

	private void parseHtml(Element body, ArrayList<ArrayList<String>> outputs) {

		ArrayList<String> rank = new ArrayList<String>();
		ArrayList<String> postTitile = new ArrayList<String>();
		ArrayList<String> boardName = new ArrayList<String>();
		ArrayList<String> author = new ArrayList<String>();
		ArrayList<String> time = new ArrayList<String>();
		ArrayList<String> link = new ArrayList<String>();
		ArrayList<String> attentionCount = new ArrayList<String>();
		ArrayList<String> replyCount = new ArrayList<String>();
		ArrayList<String> clickCount = new ArrayList<String>();
	

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
		
		
		// get every row of root table
		Elements rows = rootTable.children();
		while (rows.size() < 2 && rows.size() > 0)
			rows = rows.get(0).children();
		if (rows.size() == 0)
			return;
		// get content
		for (int i = 1; i < rows.size(); ++i) {
			Element rootRow = rows.get(i);
			
			Elements columns = rootRow.children();
			if (columns.size() < 5)
				return;
			rank.add(columns.get(0).text());
			attentionCount.add("关注人数:" + columns.get(2).text());
			replyCount.add("回帖:" + columns.get(3).text());
			clickCount.add("点击:" + columns.get(4).text());
			
			Element contentColumn = columns.get(1);
			Elements subrows = contentColumn.children();
			while (subrows.size() == 1)
				subrows = subrows.get(0).children();
			if (subrows.size() == 0)
				return;
			Elements titleElements = subrows.get(0).getElementsByTag("a");
			if (titleElements.size() < 1)
				return;
			postTitile.add(titleElements.get(0).text());
			link.add(titleElements.get(0).attr("href"));
			Elements infoCols = subrows.get(1).children();
			if (infoCols.size() < 3)
				return;
			boardName.add(infoCols.get(0).text());
			author.add(infoCols.get(1).text());
			String trimSpaceTime = infoCols.get(2).text().replace(" ", "").replace("小时", "h").replace("分", "min");
			time.add(trimSpaceTime);
		}

		outputs.add(rank);
		outputs.add(postTitile);
		outputs.add(boardName);
		outputs.add(author);
		outputs.add(time);
		outputs.add(link);
		outputs.add(attentionCount);
		outputs.add(replyCount);
		outputs.add(clickCount);
	}

}
