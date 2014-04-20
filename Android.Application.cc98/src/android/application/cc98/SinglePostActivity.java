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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SinglePostActivity extends Activity implements GetWebPageInterface{

	private ArrayList<String> postInfo;
	private ArrayList<String> authors;
	private ArrayList<String> contents;
	private ArrayList<String> timestamps;
	
	private int postCount, pageCount, displayedPage = 0;
	private String postTitle;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_post);
		
		Intent intent = getIntent();
        String postUrl = intent.getStringExtra(this.getString(R.string.postUrl));
		String cookie = UserInfoUtil.GetCookieInfo(this);
		new SinglePostTask(this, this.getString(R.string.serverName)).execute(cookie, postUrl);
	}
	
	@Override
	public void getWebPagePreProgress() {
		// set layout invisible
		LinearLayout layout = (LinearLayout)this.findViewById(R.id.singlePostLayout);
		layout.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void getWebPageProgressUpdate() {
		
	}
	
	@Override
	public void getWebPagePostProgress(Object outputRes) {
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>)outputRes;
		ArrayList<String> status = outputs.get(0);

		if (status.get(0).equals("3")) {
			
			postInfo = outputs.get(1);
			
			postCount = Integer.parseInt(postInfo.get(0));
			pageCount = Integer.parseInt(postInfo.get(1));
			postTitle = postInfo.get(2);
			displayedPage = 1;
			
			authors = outputs.get(2);
			contents = outputs.get(3);
			timestamps = outputs.get(4);
			
			// set single post UI
			setSinglePost();
			
			// set layout visible
			LinearLayout layout = (LinearLayout)this.findViewById(R.id.singlePostLayout);
			layout.setVisibility(View.VISIBLE);
		}
		else {
		}
	}
	
	private void setSinglePost() {
		TextView postTitleTv = (TextView)this.findViewById(R.id.singlePostTitle);
		postTitleTv.setText(postTitle);
		
		TextView postInfoTv = (TextView)this.findViewById(R.id.singlePostInfo);
		postInfoTv.setText("»ØÌû×ÜÊý£º" + (postCount - 1));
		
		// set data
		String postFloorText = this.getString(R.string.postFLoorText);
		String postAuthorText = this.getString(R.string.postAuthorText);
		String postContentText = this.getString(R.string.postContentText);
		String postTimestampText = this.getString(R.string.postTimestampText);
		ArrayList<HashMap<String, String>> displist = new ArrayList<HashMap<String, String>>();
		
		for (int i = 0; i < authors.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(postFloorText, " " + i + "F ");
			map.put(postAuthorText, authors.get(i));
			map.put(postContentText, contents.get(i));
			map.put(postTimestampText, timestamps.get(i));
			displist.add(map);
		}
		
        SimpleAdapter mSchedule = new SimpleAdapter(this,
        		displist,
        		R.layout.single_post_list_item,	//	ListItem XML implementation
                new String[] {	postFloorText, postAuthorText, 
        						postContentText, postTimestampText}, // dynamic array and ListItem correspondings        
                new int[] {	R.id.postFloorText, R.id.postAuthorText,
        					R.id.postContentText, R.id.postTimestampText}); // ListItem XML's two TextView ID
       
        // set custom list view and listener
     	ListView postLv = (ListView)this.findViewById(R.id.singlePostList);
     	postLv.setAdapter(mSchedule);
     	Utility.setListViewHeightBasedOnChildren(postLv);

	}
}
