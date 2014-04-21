package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.application.cc98.network.SinglePostTask;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SinglePostActivity extends Activity implements OnClickListener, GetWebPageInterface{

	// temporary variables to store data from HTML, updated when loading following posts
	private ArrayList<String> authors;
	private ArrayList<String> contents;
	private ArrayList<String> timestamps;
	
	// global variables to store data of the topic list
	private ArrayList<String> postInfo;
	
	// variables to record post information
	private int postCount, pageCount, displayedPage = 0;
	private String cookie, serverName, postTitle, postUrl;
	
	// UI and data
	private Button postMoreBtn;
	private SimpleAdapter listItemAdapter;
	private ArrayList<HashMap<String, String>> displist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_post);
		postMoreBtn = (Button)this.findViewById(R.id.singlePostMoreButton);
		postMoreBtn.setOnClickListener(this);
		
		Intent intent = getIntent();
        postUrl = intent.getStringExtra(this.getString(R.string.postUrl));
        StringBuilder sb = new StringBuilder();
        sb.append(postUrl); sb.append("&star=");
        postUrl = sb.toString();
        
		cookie = UserInfoUtil.GetCookieInfo(this);
		serverName = this.getString(R.string.serverName);
		new SinglePostTask(this, serverName).execute(cookie, postUrl + "1");
	}
	
	@Override
	public void getWebPagePreProgress() {
		if (displayedPage == 0 ) {
			// set layout invisible
			LinearLayout layout = (LinearLayout)this.findViewById(R.id.singlePostLayout);
			layout.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public void getWebPageProgressUpdate() {
		
	}
	
	@Override
	public void getWebPagePostProgress(Object outputRes) {
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>)outputRes;
		ArrayList<String> status = outputs.get(0);

		// first post page loading finish
		if (status.get(0).equals("1")) {
			
			postInfo = outputs.get(1);
			
			postCount = Integer.parseInt(postInfo.get(0));
			pageCount = Integer.parseInt(postInfo.get(1));
			postTitle = postInfo.get(2);
			displayedPage = 1;
			
			authors = outputs.get(2);
			contents = outputs.get(3);
			timestamps = outputs.get(4);
			
			// set single post UI
			setSinglePostUI();
			
			// set layout visible
			LinearLayout layout = (LinearLayout)this.findViewById(R.id.singlePostLayout);
			layout.setVisibility(View.VISIBLE);
		}
		// following post page loading finish
		else if (status.get(0).equals("3")) {
			
			authors = outputs.get(2);
			contents = outputs.get(3);
			timestamps = outputs.get(4);
			
			// update UI for following posts
			updateSinglePostUI();
		}
		else {
		}
	}
	
	private void setSinglePostUI() {
		TextView postTitleTv = (TextView)this.findViewById(R.id.singlePostTitle);
		postTitleTv.setText(postTitle);
		
		TextView postInfoTv = (TextView)this.findViewById(R.id.singlePostInfo);
		postInfoTv.setText("»ØÌû×ÜÊý£º" + (postCount - 1));
		
		if (displayedPage < pageCount)
			postMoreBtn.setText(this.getString(R.string.morePostText));
		else
			postMoreBtn.setText(this.getString(R.string.noMorePostText));

		// set data
		String postFloorText = this.getString(R.string.postFLoorText);
		String postAuthorText = this.getString(R.string.postAuthorText);
		String postContentText = this.getString(R.string.postContentText);
		String postTimestampText = this.getString(R.string.postTimestampText);
		displist = new ArrayList<HashMap<String, String>>();
		
		for (int i = 0; i < authors.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(postFloorText, " " + i + "F ");
			map.put(postAuthorText, authors.get(i));
			map.put(postTimestampText, timestamps.get(i));
			map.put(postContentText, contents.get(i));
			displist.add(map);
		}
		
		listItemAdapter = new SimpleAdapter(this,
        		displist,
        		R.layout.single_post_list_item,	//	ListItem XML implementation
                new String[] {	postFloorText, postAuthorText, 
        						postTimestampText, postContentText}, // dynamic array and ListItem correspondings        
                new int[] {	R.id.postFloorText, R.id.postAuthorText,
        					R.id.postTimestampText, R.id.postContentText}); // ListItem XML's two TextView ID
        
        // set custom list view
     	ListView postLv = (ListView)this.findViewById(R.id.singlePostList);
     	postLv.setAdapter(listItemAdapter);
     	Utility.setListViewHeightBasedOnChildren(postLv);
	}
	
	private void updateSinglePostUI() {
		// set data
		String postFloorText = this.getString(R.string.postFLoorText);
		String postAuthorText = this.getString(R.string.postAuthorText);
		String postContentText = this.getString(R.string.postContentText);
		String postTimestampText = this.getString(R.string.postTimestampText);
		
		int baseIndex = (displayedPage - 1) * 10;
		for (int i = 0; i < authors.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(postFloorText, " " + (baseIndex + i) + "F ");
			map.put(postAuthorText, authors.get(i));
			map.put(postTimestampText, timestamps.get(i));
			map.put(postContentText, contents.get(i));
			displist.add(map);
		}
		// inform adapter to update UI
		listItemAdapter.notifyDataSetChanged(); 
		
		if (displayedPage < pageCount)
			postMoreBtn.setText(this.getString(R.string.morePostText));
		else
			postMoreBtn.setText(this.getString(R.string.noMorePostText));
	}
	
	public void onClick(View v) {
    	switch (v.getId()) {
    	case R.id.singlePostMoreButton:
    		//System.out.println("Click singlePostMoreButton");
			if (displayedPage >= pageCount)
				postMoreBtn.setText(this.getString(R.string.noMorePostText));
			else {
				displayedPage++;
				String followingPostUrl = postUrl + Integer.toString(displayedPage);
				System.out.println("Loading follow post URL: " + followingPostUrl);
				postMoreBtn.setText(this.getString(R.string.loadingMorePostText));
				new SinglePostTask(this, serverName).execute(cookie, followingPostUrl);
			}
			break;
    	}
	}
}
