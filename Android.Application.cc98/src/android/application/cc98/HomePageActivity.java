package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import android.application.cc98.network.HomePageTask;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.GrapeGridView;
import android.application.cc98.view.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class HomePageActivity extends LoadWebPageActivity {

	private ArrayList<String> customBoardNames;
	private ArrayList<String> customBoardUrls;
	private ArrayList<String> customBoardDescripts;

	private ArrayList<String> defaultBoardNames;
	private ArrayList<String> defaultBoardUrls;

	private String homePage, boardUrlName;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		homePage = UserInfoUtil.getHomePageURL(this);
		boardUrlName = this.getString(R.string.boardUrl);

	}

	@Override
	public void loadPage() {
		new HomePageTask(this, serverName).execute(cookie, homePage);
	}

	@Override
	public void loadPageSucess(Object output) {

		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) output;

		setContentView(R.layout.home);

		Button existButton = (Button) this.findViewById(R.id.homeExitButton);
		existButton.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomePageActivity.this,
						LoginActivity.class);
				//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // clear
																	// activity
																	// stack
				HomePageActivity.this.startActivity(intent);
			}
		});

		fillContent(output);
	}

	public int getStatusCode(Object outputRes) {
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) outputRes;
		ArrayList<String> status = outputs.get(0);
		int statusCode = Integer.parseInt(status.get(0));
		return statusCode;
	}

	public String getErrorMessage(Object outputs) {
		return "";
	}

	public void fillContent(Object outputRes) {
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) outputRes;

		TextView username = (TextView) this.findViewById(R.id.homePageUsername);
		username.setText("用户名：" + UserInfoUtil.GetUserName(this));

		customBoardNames = outputs.get(1);
		customBoardUrls = outputs.get(2);
		customBoardDescripts = outputs.get(3);
		defaultBoardNames = outputs.get(4);
		defaultBoardUrls = outputs.get(5);
		// set custom board UI
		setCustomBoard();
		// set default board UI
		setDefaultBoard();

		// set layout visible
		LinearLayout layout = (LinearLayout) this
				.findViewById(R.id.homePageLayout);
		layout.setVisibility(View.VISIBLE);
	}

	private void setCustomBoard() {
		// set data
		ArrayList<HashMap<String, String>> displist = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < customBoardNames.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(this.getString(R.string.customItemTitle),
					customBoardNames.get(i));
			map.put(this.getString(R.string.customItemText),
					customBoardDescripts.get(i));
			displist.add(map);
		}
		// Toast.makeText(this, "Custom List count:" + displist.size(),
		// Toast.LENGTH_LONG).show();

		SimpleAdapter mSchedule = new SimpleAdapter(this, displist,
				R.layout.home_custom_list_item, // ListItem XML implementation
				new String[] { this.getString(R.string.customItemTitle),
						this.getString(R.string.customItemText) }, // dynamic
																	// array and
																	// ListItem
																	// correspondings
				new int[] { R.id.customItemTitle, R.id.customItemText }); // ListItem
																			// XML's
																			// two
																			// TextView
																			// ID

		// set custom list view and listener
		ListView customLv = (ListView) this
				.findViewById(R.id.homePageCustomList);
		customLv.setAdapter(mSchedule);
		Utility.setListViewHeightBasedOnChildren(customLv);

		// set view
		customLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String boardUrl = homePage + customBoardUrls.get(position);
				/*Toast.makeText(getApplicationContext(), "Url:" + boardUrl,
						Toast.LENGTH_SHORT).show();*/
				String titleName = customBoardNames.get(position);
				Intent intent = null;
				if (titleName.contains("("))
					intent = new Intent(HomePageActivity.this,
							BBSListActivity.class);
				else
					intent = new Intent(HomePageActivity.this,
							LeafBoardActivity.class);
				intent.putExtra(boardUrlName, boardUrl);
				HomePageActivity.this.startActivity(intent);
			}
		});
	}

	private void setDefaultBoard() {
		// set data
		ArrayList<HashMap<String, Object>> displist = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < defaultBoardNames.size(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			String boardName = defaultBoardNames.get(i).trim();
			int imageID = getBoardImageID(boardName);
			map.put(this.getString(R.string.defaultItemTitle), boardName);
			map.put(this.getString(R.string.defaultItemImage), imageID);
			displist.add(map);
		}
		// Toast.makeText(this, "Custom List count:" + displist.size(),
		// Toast.LENGTH_LONG).show();

		SimpleAdapter mSchedule = new SimpleAdapter(this, displist,
				R.layout.home_default_list_item, // ListItem XML implementation
				new String[] { 	this.getString(R.string.defaultItemTitle),
								this.getString(R.string.defaultItemImage)}, // dynamic
																			// array
																			// and
																			// ListItem
																			// correspondings
				new int[] { R.id.defaultItemTitle, R.id.defaultItemImage }); // ListItem XML's two
														// TextView ID

		// set custom list view and listener
		GrapeGridView defaultGv = (GrapeGridView) this
				.findViewById(R.id.homePageDefaultGrid);
		defaultGv.setAdapter(mSchedule);
		Utility.setGridViewHeightBasedOnChildren(defaultGv);

		// set view
		defaultGv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String boardUrl = homePage + defaultBoardUrls.get(position);
				/*Toast.makeText(getApplicationContext(), "Url:" + boardUrl,
						Toast.LENGTH_SHORT).show();*/
				Intent intent = new Intent(HomePageActivity.this,
						BBSListActivity.class);
				intent.putExtra(boardUrlName, boardUrl);
				HomePageActivity.this.startActivity(intent);
			}
		});
	}

	private int getBoardImageID(String boardName) {
		if (boardName.equals("教师答疑")) return R.drawable.teacher1;
		if (boardName.equals("学习天地")) return R.drawable.book3;
		if (boardName.equals("校园动态")) return R.drawable.news2;
		if (boardName.equals("信息资讯")) return R.drawable.network1;
		if (boardName.equals("个性生活")) return R.drawable.coffee;
		if (boardName.equals("休闲娱乐")) return R.drawable.ice_cream;
		if (boardName.equals("体育运动")) return R.drawable.sport;
		if (boardName.equals("影音无限")) return R.drawable.movie1;
		if (boardName.equals("电脑技术")) return R.drawable.computer1;
		if (boardName.equals("社科学术")) return R.drawable.science1;
		if (boardName.equals("游戏广场")) return R.drawable.game1;
		if (boardName.equals("动漫天地")) return R.drawable.mickey;
		if (boardName.equals("感性空间")) return R.drawable.love;
		if (boardName.equals("瞬间永恒")) return R.drawable.time1;
		if (boardName.equals("交易代理")) return R.drawable.sale;
		if (boardName.equals("论坛管理")) return R.drawable.management;
		if (boardName.equals("院系交流")) return R.drawable.communication;
		if (boardName.equals("社团风采")) return R.drawable.people;
		if (boardName.equals("天下一家")) return R.drawable.world;
		return R.drawable.cc98_smallest;
	}
	
	// Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		// 退出
		if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
			String exitCode = intent.getStringExtra("exit_code");
			if (exitCode.equals("true"))
				finish();
			else
				preLoadPage();
		}
	}

}
