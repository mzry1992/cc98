package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.application.cc98.network.HotPostTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class HotPostActivity extends Activity implements
		GetWebPageInterface {

	private ListView lv = null;

	private String hotPostURL = null, cookie = null;

	private ArrayList<String> rankList = null, contentList = null,
			boardList = null;

	private ArrayList<String> authorList = null, timeList = null, 
			linkList = null, attentcountList = null, postcountList = null,
			clickcountList = null;

	public HotPostActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hotpost);
		lv = (ListView) findViewById(R.id.hotpostlistView);

		hotPostURL = "http://www.cc98.org/hottopic.asp";
		cookie = UserInfoUtil.GetCookieInfo(this);
		new HotPostTask(this).execute(hotPostURL, cookie);
	}

	private void fillContent() {
		SimpleAdapter adapter = new SimpleAdapter(this, getData(),
				R.layout.hotpost_list_item, new String[] { 
						"ranking", "content", 
						"board", "author", "time",
						"attentioncount", "postcount", "clickcount" }, 
			new int[] { 
						R.id.rankingText, R.id.contentText,
						R.id.boardText, R.id.authorText, R.id.timeText,
						R.id.attentionCountText, R.id.replyCountText, R.id.clickCountText });
		lv.setAdapter(adapter);
	}

	private ArrayList<HashMap<String, String>> getData() {
		ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < rankList.size(); ++i) {
			HashMap<String, String> tempHashMap = new HashMap<String, String>();
			//tempHashMap.put("rank", rankList.get(i));
			tempHashMap.put("ranking", " NO." + (i + 1) + " ");
			tempHashMap.put("content", contentList.get(i).trim());
			tempHashMap.put("board", boardList.get(i).trim());
			tempHashMap.put("author", authorList.get(i).trim());
			tempHashMap.put("time", timeList.get(i).trim());
			tempHashMap.put("attentioncount", attentcountList.get(i).trim());
			tempHashMap.put("postcount", postcountList.get(i).trim());
			tempHashMap.put("clickcount", clickcountList.get(i).trim());
			arrayList.add(tempHashMap);
		}
		return arrayList;
	}

	@Override
	public void getWebPagePreProgress() {

	}

	@Override
	public void getWebPageProgressUpdate() {

	}

	@Override
	public void getWebPagePostProgress(Object status) {

		ArrayList<ArrayList<String>> statusStr = (ArrayList<ArrayList<String>>) status;

		int statusCode = Integer.parseInt(statusStr.get(0).get(0));
		// System.out.println("statusCode:" + statusCode);
		boolean isLoadSucess = false;
		StringBuilder errorStrBuilder = new StringBuilder();

		switch (statusCode) {
		case 1:
			errorStrBuilder.append("用户登录信息无法认证，请重新登录");
			break;
		case 2:
			errorStrBuilder.append("cc98服务器异常, code:");
			errorStrBuilder.append(statusStr.get(0).get(1));
			break;
		case 3:
		case 5:
			loadPageSucess(statusStr);
			isLoadSucess = true;
			break;
		case 4:
			errorStrBuilder.append("网络异常，请检查网络");
			break;
		default:
			errorStrBuilder.append("未知错误，请联系开发人员");
			break;
		}

		if (!isLoadSucess) {
			Toast.makeText(this, errorStrBuilder.toString(), Toast.LENGTH_LONG)
					.show();
		}
	}

	private void loadPageSucess(ArrayList<ArrayList<String>> status) {
		rankList = status.get(1);
		contentList = status.get(2);
		boardList = status.get(3);
		authorList = status.get(4);
		timeList = status.get(5);
		linkList = status.get(6);
		attentcountList = status.get(7);
		postcountList = status.get(8);
		clickcountList = status.get(9);
		fillContent();
	}
}
