package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import android.application.cc98.network.LeafBoardTask;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class LeafBoardActivity extends LoadWebPageActivity implements
		OnClickListener {

	// temporary variables to store data from HTML, updated when following pages
	// call
	private ArrayList<String> topicTitles;
	private ArrayList<String> topicUrls;
	private ArrayList<String> topicAdditions;

	// global variables to store data of the topic list
	private ArrayList<String> boardInfo;
	private ArrayList<String> globalTopicUrls;

	// variables to record post information
	private String homePage,  boardUrl, postUrlName;
	private int postCount, pageCount, displayedPage = 0;

	// UI and data
	private Button pageMoreBtn;
	private SimpleAdapter listItemAdapter;
	private ArrayList<HashMap<String, String>> displist;
	
	// mark status
	private boolean firstPageLoadSucess = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		globalTopicUrls = new ArrayList<String>();

		Intent intent = getIntent();
		boardUrl = intent.getStringExtra(this.getString(R.string.boardUrl));
		boardUrl = boardUrl + "&page=";

		homePage = UserInfoUtil.getHomePageURL(this);
		postUrlName = this.getString(R.string.postUrl);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.refresh_menu, menu);
		refreshItem = menu.findItem(R.id.refresh);
		msgMenuItem = menu.findItem(R.id.message);
		msgMenuItem.setIcon(this.getResources().getDrawable(R.drawable.newmblog));
		msgMenuItem.setVisible(true);
		
		msgMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				
				return true;
			}
		});
		
		preLoadPage();
		return true;
	}
	
	@Override
	public void loadPage() {
		new LeafBoardTask(this, this.getString(R.string.serverName)).execute(
				cookie, boardUrl + "1");
	}

	@Override
	public void loadPageSucess(Object output) {
		
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) output;
		ArrayList<String> status = outputs.get(0);
		
		//System.out.println("LeafBoard status code:" + status.get(0));
		if (status.get(0).equals("3")) {
			setContentView(R.layout.leaf_board);
			pageMoreBtn = (Button) this.findViewById(R.id.leafBoardMoreButton);
			pageMoreBtn.setOnClickListener(this);
		}

		fillContent(output);
	}

	public int getStatusCode(Object outputRes) {
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) outputRes;
		ArrayList<String> status = outputs.get(0);
		int statusCode = Integer.parseInt(status.get(0));
		/*if (statusCode == 3 || firstPageLoadSucess)
			statusCode = 5;*/
		return statusCode;
	}

	public String getErrorMessage(Object outputs) {
		return "";
	}

	public void fillContent(Object outputRes) {
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) outputRes;
		ArrayList<String> status = outputs.get(0);
		// Toast.makeText(this, "Status: " + status.get(0),
		// Toast.LENGTH_LONG).show();

		if (status.get(0).equals("3")) {

			firstPageLoadSucess = true;
			
			boardInfo = outputs.get(1);
			topicTitles = outputs.get(2);
			topicUrls = outputs.get(3);
			topicAdditions = outputs.get(4);

			globalTopicUrls.addAll(topicUrls);

			postCount = Integer.parseInt(boardInfo.get(1));
			pageCount = Integer.parseInt(boardInfo.get(2));
			displayedPage = 1;

			// set leaf board UI
			setLeafBoardUI();

			// set layout visible
			LinearLayout layout = (LinearLayout) this
					.findViewById(R.id.leafBoardLayout);
			layout.setVisibility(View.VISIBLE);
		} else if (status.get(0).equals("5")) {

			topicTitles = outputs.get(2);
			topicUrls = outputs.get(3);
			topicAdditions = outputs.get(4);

			globalTopicUrls.addAll(topicUrls);

			// update UI for following posts
			updateLeafBoardUI();
		}
	}

	private void setLeafBoardUI() {
		/*
		 * TextView usernameTv =
		 * (TextView)this.findViewById(R.id.leafBoardUsername);
		 * usernameTv.setText("用户名：" + UserInfoUtil.GetUserName(this));
		 */

		TextView boardNameTv = (TextView) this
				.findViewById(R.id.leafBoardBoardname);
		boardNameTv.setText(boardInfo.get(0));

		TextView topicCntTv = (TextView) this
				.findViewById(R.id.leafBoardTopicCount);
		topicCntTv.setText("本版帖子总数：" + boardInfo.get(1));

		if (displayedPage < pageCount)
			pageMoreBtn.setText(this.getString(R.string.morePageText));
		else
			pageMoreBtn.setText(this.getString(R.string.noMorePageText));

		// set data
		String leafItemTitleStr = this.getString(R.string.leafItemTitle);
		String leafItemTextStr = this.getString(R.string.leafItemText);
		displist = new ArrayList<HashMap<String, String>>();

		for (int i = 0; i < topicTitles.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(leafItemTitleStr, topicTitles.get(i));
			map.put(leafItemTextStr, topicAdditions.get(i));
			displist.add(map);
		}

		listItemAdapter = new SimpleAdapter(this, displist,
				R.layout.leaf_board_list_item, // ListItem XML implementation
				new String[] { leafItemTitleStr, leafItemTextStr }, // dynamic
																	// array and
																	// ListItem
																	// correspondings
				new int[] { R.id.leafItemTitle, R.id.leafItemText }); // ListItem
																		// XML's
																		// two
																		// TextView
																		// ID

		// set custom list view and listener
		ListView topicLv = (ListView) this
				.findViewById(R.id.leafBoardTopicList);
		topicLv.setAdapter(listItemAdapter);
		Utility.setListViewHeightBasedOnChildren(topicLv);

		// set view
		topicLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String postUrl = homePage + globalTopicUrls.get(position);
				/*Toast.makeText(getApplicationContext(), "Url:" + postUrl,
						Toast.LENGTH_LONG).show();*/
				Intent intent = new Intent(LeafBoardActivity.this,
						SinglePostActivity.class);
				intent.putExtra(postUrlName, postUrl);
				LeafBoardActivity.this.startActivity(intent);
			}
		});
	}

	private void updateLeafBoardUI() {
		// set data
		String leafItemTitleStr = this.getString(R.string.leafItemTitle);
		String leafItemTextStr = this.getString(R.string.leafItemText);

		for (int i = 0; i < topicTitles.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(leafItemTitleStr, topicTitles.get(i));
			map.put(leafItemTextStr, topicAdditions.get(i));
			displist.add(map);
		}
		// inform adapter to update UI
		listItemAdapter.notifyDataSetChanged();

		if (displayedPage < pageCount)
			pageMoreBtn.setText(this.getString(R.string.morePageText));
		else
			pageMoreBtn.setText(this.getString(R.string.noMorePageText));

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.leafBoardMoreButton:
			if (displayedPage >= pageCount)
				pageMoreBtn.setText(this.getString(R.string.noMorePageText));
			else {
				displayedPage++;
				String followingPageUrl = boardUrl
						+ Integer.toString(displayedPage);
				System.out.println("Loading follow page URL: "
						+ followingPageUrl);
				pageMoreBtn.setText(this
						.getString(R.string.loadingMorePageText));
				new LeafBoardTask(this, serverName).execute(cookie,
						followingPageUrl);
			}
			break;
		}
	}
}
