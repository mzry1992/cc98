package android.application.cc98.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.application.cc98.BBSListActivity;
import android.application.cc98.FragmentHomeActivity;
import android.application.cc98.GetWebPageInterface;
import android.application.cc98.LeafBoardActivity;
import android.application.cc98.R;
import android.application.cc98.SinglePostActivity;
import android.application.cc98.network.HotPostTask;
import android.application.cc98.network.LeafBoardTask;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class HotPostFragment extends Fragment implements GetWebPageInterface,
		OnClickListener {

	private int lastX = 0, lastY = 0;

	private ScrollView scrollView = null;
	
	private View linearLayout = null;

	private boolean isNetworkRequest = false;

	private boolean isLoadData = false;

	private boolean isCookieValid = false;

	private FragmentHomeActivity activity = null;

	// content
	private ListView lv = null;

	private View hotPostLayout = null;

	// hotPostURL
	private String hotPostURL = null, cookies = null, homePage = null;

	// content
	private ArrayList<String> rankList = null, contentList = null,
			boardList = null;

	private ArrayList<String> authorList = null, timeList = null,
			linkList = null, attentcountList = null, postcountList = null,
			clickcountList = null;

	private Button refreshButton = null;

	public HotPostFragment(FragmentHomeActivity activity) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		hotPostURL = activity.getString(R.string.hotpost_page_url);
		homePage = UserInfoUtil.getHomePageURL(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		hotPostLayout = inflater.inflate(R.layout.hotpost, container, false);
		scrollView = (ScrollView)hotPostLayout.findViewById(R.id.hotpostScrollView);
		linearLayout = hotPostLayout.findViewById(R.id.hotpostLinearLayout);
		lv = (ListView) hotPostLayout.findViewById(R.id.hotpostlistView);
		refreshButton = (Button) hotPostLayout
				.findViewById(R.id.hotPostFragmentRefreshButton);
		refreshButton.setOnClickListener(this);
		return hotPostLayout;
	}
	
	public void recordScrollPosition() {
		lastX = scrollView.getScrollX();
		lastY = scrollView.getScrollY();
	}
	
	public void restoreScrollPosition() {
		scrollView.smoothScrollTo(lastX, lastY);
	}

	public void loadData() {
		if (isNetworkRequest)
			return;
		cookies = UserInfoUtil.GetCookieInfo(activity);
		if (cookies == null) {
			handleLoginError();
			return;
		}
		isCookieValid = true;
		isNetworkRequest = true;
		if (isLoadData)
			refreshButton.setText("正在刷新中");
		new HotPostTask((GetWebPageInterface) this)
				.execute(hotPostURL, cookies);
	}

	private void fillContent() {
		SimpleAdapter adapter = new SimpleAdapter(activity, getData(),
				R.layout.hotpost_list_item, new String[] { "content", "board",
						"author", "time", "attentioncount", "postcount",
						"clickcount" }, new int[] { R.id.contentText,
						R.id.boardText, R.id.authorText, R.id.timeText,
						R.id.attentionCountText, R.id.replyCountText,
						R.id.clickCountText });
		lv.setAdapter(adapter);
		Utility.setListViewHeightBasedOnChildren(lv);
	}

	private ArrayList<HashMap<String, String>> getData() {
		ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < rankList.size(); ++i) {
			HashMap<String, String> tempHashMap = new HashMap<String, String>();
			// tempHashMap.put("rank", rankList.get(i));
			tempHashMap.put("content", contentList.get(i));
			tempHashMap.put("board", boardList.get(i));
			tempHashMap.put("author", authorList.get(i));
			tempHashMap.put("time", timeList.get(i));
			tempHashMap.put("attentioncount", attentcountList.get(i));
			tempHashMap.put("postcount", postcountList.get(i));
			tempHashMap.put("clickcount", clickcountList.get(i));
			arrayList.add(tempHashMap);
		}
		return arrayList;
	}

	public void getWebPagePreProgress() {

	}

	public void getWebPageProgressUpdate() {

	}

	public void getWebPagePostProgress(Object status) {
		isNetworkRequest = false;
		refreshButton.setText("点击刷新");
		ArrayList<ArrayList<String>> statusStr = (ArrayList<ArrayList<String>>) status;

		int statusCode = Integer.parseInt(statusStr.get(0).get(0));
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
			Toast.makeText(activity, errorStrBuilder.toString(),
					Toast.LENGTH_LONG).show();
			handleError(statusCode);
		}
	}

	private void loadPageSucess(ArrayList<ArrayList<String>> status) {
		isLoadData = true;
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
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String singlePostUrl = homePage + linkList.get(position);
				Intent intent = new Intent(getActivity(),
						SinglePostActivity.class);
				intent.putExtra(activity.getString(R.string.postUrl),
						singlePostUrl);
				getActivity().startActivity(intent);
				Toast.makeText(activity, singlePostUrl, Toast.LENGTH_LONG)
						.show();
			}
		});
		activity.refresh();
	}

	private void handleError(int statusCode) {
		if (statusCode == 1) {
			handleLoginError();
			return;
		}
		activity.refresh();
	}

	private void handleLoginError() {
		isCookieValid = false;
		activity.jumpToLogin();
		activity.refresh();
	}

	public boolean isCookieValid() {
		if (cookies == null)
			return false;
		return isCookieValid;
	}

	public void setCookie(String cookies) {
		this.cookies = cookies;
		isCookieValid = true;
	}

	public boolean isDataLoad() {
		return isLoadData;
	}

	public void invalidData() {
		isLoadData = false;
	}

	// 0 represent loading data
	// 1 get normal data
	// 2 can't login
	// 3 network error
	public int getFragmentStatus() {
		if (isLoadData)
			return 1;
		if (isNetworkRequest)
			return 0;
		if (!isCookieValid)
			return 2;
		return 3;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.hotPostFragmentRefreshButton:
			if (!isNetworkRequest)
				loadData();
			break;
		}
	}

}
