package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.application.cc98.R.color;
import android.application.cc98.network.BBSListTask;
import android.application.cc98.network.HomePageTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BBSListActivity extends Activity implements GetWebPageInterface {

	protected MenuItem refreshItem = null;
	private String serverName = null, boardUrlName = null;
	private String pageURL = null, cookie = null;

	private String pageTitle = null;
	private ListView lv = null;
	TextView headerTextView = null;
	private ArrayList<String> bordTitleArrayList = null;
	private ArrayList<String> infoArrayList = null;
	private ArrayList<String> linkArrayList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		serverName = this.getString(R.string.serverName);
		boardUrlName = this.getString(R.string.boardUrl);
		cookie = UserInfoUtil.GetCookieInfo(this);

		Intent intent = getIntent();
		pageURL = intent.getStringExtra(boardUrlName);
		
		if (cookie == null) 
			jumpToLogin();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.refresh_menu, menu);
		refreshItem = menu.findItem(R.id.refresh);
		loadPage();
		return true;
	}

	@Override
	public void getWebPagePreProgress() {
		// set layout invisible
		// LinearLayout layout =
		// (LinearLayout)this.findViewById(R.id.homePageLayout);
		// layout.setVisibility(View.INVISIBLE);
		showRefreshAnimation();
	}

	@Override
	public void getWebPageProgressUpdate() {

	}

	@Override
	public void getWebPagePostProgress(Object outputRes) {

		hideRefreshAnimation();

		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) outputRes;
		ArrayList<String> status = outputs.get(0);
		int statusCode = Integer.parseInt(status.get(0));

		boolean isLoadSucess = false;
		StringBuilder errorStrBuilder = new StringBuilder();

		switch (statusCode) {
		case 1:
			errorStrBuilder.append("用户登录信息无法认证，请重新登录");
			break;
		case 2:
			errorStrBuilder.append("cc98服务器异常, code:");
			errorStrBuilder.append(status.get(1));
			break;
		case 3:
			loadPageSucess(outputs);
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
			loadPageErrorHandle(statusCode);
		}

	}

	private void loadPage() {

		new BBSListTask(BBSListActivity.this).execute(pageURL, cookie);
	}

	private void loadPageSucess(ArrayList<ArrayList<String>> outputs) {

		setContentView(R.layout.bbslist);

		lv = (ListView) findViewById(R.id.bbslistView);
		headerTextView = new TextView(this);
		lv.addHeaderView(headerTextView);
		headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f);
		int lightPurple = this.getResources().getColor(R.color.lightPurple);
		headerTextView.setBackgroundColor(lightPurple);

		fillContent(outputs);
	}

	private void loadPageErrorHandle(int statusCode) {

		if (statusCode != 3 && statusCode != 1) {
			setContentView(R.layout.loading_error_net);
			ImageButton retryButton = (ImageButton) this
					.findViewById(R.id.retry_image_button);
			if (retryButton != null) {
				retryButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						BBSListActivity.this.loadPage();
					}
				});
			}
		} else {
			//jump to login page
			jumpToLogin();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			loadPage();
			return true;
		case R.id.exit_menu_item:
			exitProgram();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void fillContent(ArrayList<ArrayList<String>> outputs) {
		pageTitle = outputs.get(1).get(0);
		bordTitleArrayList = outputs.get(2);
		infoArrayList = outputs.get(3);
		linkArrayList = outputs.get(4);

		SimpleAdapter adapter = new SimpleAdapter(this, getData(),
				R.layout.bbs_list_item, new String[] { "title", "info" },
				new int[] { R.id.list_item_title, R.id.list_item_info });
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String boardUrl = "http://" + serverName + "/"
						+ linkArrayList.get(position);
				Toast.makeText(getApplicationContext(), "Url:" + boardUrl,
						Toast.LENGTH_SHORT).show();
				String titleName = bordTitleArrayList.get(position);
				Intent intent = null;
				if (titleName.contains("("))
					intent = new Intent(BBSListActivity.this,
							BBSListActivity.class);
				else
					intent = new Intent(BBSListActivity.this,
							LeafBoardActivity.class);
				intent.putExtra(boardUrlName, boardUrl);
				BBSListActivity.this.startActivity(intent);
			}
		});

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(pageTitle);
		strBuilder.append("->论坛列表(");
		strBuilder.append(bordTitleArrayList.size());
		strBuilder.append(")");
		headerTextView.setText(strBuilder.toString());
	}

	private ArrayList<HashMap<String, String>> getData() {
		ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < bordTitleArrayList.size(); ++i) {
			HashMap<String, String> tempHashMap = new HashMap<String, String>();
			tempHashMap.put("title", bordTitleArrayList.get(i));
			tempHashMap.put("info", infoArrayList.get(i));
			arrayList.add(tempHashMap);
		}
		return arrayList;
	}

	private void showRefreshAnimation() {
		hideRefreshAnimation();

		// 这里使用一个ImageView设置成MenuItem的ActionView，这样我们就可以使用这个ImageView显示旋转动画了
		ImageView refreshActionView = (ImageView) getLayoutInflater().inflate(
				R.layout.refresh_action_view, null);
		refreshActionView.setImageResource(R.drawable.ic_action_refresh);

		refreshItem.setActionView(refreshActionView);

		// 显示刷新动画
		Animation animation = AnimationUtils
				.loadAnimation(this, R.anim.refresh);
		animation.setRepeatMode(Animation.RESTART);
		animation.setRepeatCount(Animation.INFINITE);
		refreshActionView.startAnimation(animation);
	}

	private void hideRefreshAnimation() {
		if (refreshItem != null) {
			View view = refreshItem.getActionView();
			if (view != null) {
				view.clearAnimation();
				refreshItem.setActionView(null);
			}
		}
	}
	
	private void jumpToLogin() {
		Intent welcomeIntent = new Intent(BBSListActivity.this, LoginActivity.class);
		BBSListActivity.this.startActivity(welcomeIntent);
	}
	
	private void exitProgram() {
		Intent intent = new Intent(); 
		intent.setClass(BBSListActivity.this, HomePageActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("exit_code", "true");
		startActivity(intent);
		finish();
	}


}
