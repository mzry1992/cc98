package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.application.cc98.network.LeafBoardTask;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class LeafBoardActivity extends Activity implements GetWebPageInterface{
	
	private ArrayList<String> topicTitles;
	private ArrayList<String> topicUrls;
	private ArrayList<String> topicAdditions;
	private ArrayList<String> boardInfo;
	private String homePage, postUrlName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaf_board);
		
		homePage = UserInfoUtil.getHomePageURL(this);
		postUrlName = this.getString(R.string.postUrl);
		Intent intent = getIntent();
        String boardUrl = intent.getStringExtra(this.getString(R.string.boardUrl));
		String cookie = UserInfoUtil.GetCookieInfo(this);
		new LeafBoardTask(this, this.getString(R.string.serverName)).execute(cookie, boardUrl);
	}
	
	@Override
	public void getWebPagePreProgress() {
		// set layout invisible
		LinearLayout layout = (LinearLayout)this.findViewById(R.id.leafBoardLayout);
		layout.setVisibility(View.INVISIBLE);
	}
	
	@Override
	public void getWebPageProgressUpdate() {
		
	}
	
	@Override
	public void getWebPagePostProgress(Object outputRes) {
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>)outputRes;
		ArrayList<String> status = outputs.get(0);
		//Toast.makeText(this, "Status: " + status.get(0), Toast.LENGTH_LONG).show();

		if (status.get(0).equals("3")) {
			
			topicTitles = outputs.get(1);
			topicUrls = outputs.get(2);
			topicAdditions = outputs.get(3);
			boardInfo = outputs.get(4);
			
			/*System.out.println(topicTitles.size() + ":" + topicUrls.size() + ":" + topicAdditions.size());
			for (int i = 0; i < topicTitles.size(); i++) {
				System.out.println("New pair");
				System.out.println(topicTitles.get(i));
				System.out.println(topicAdditions.get(i));
			}*/
			
			// set leaf board UI
			setLeafBoard();
			
			// set layout visible
			LinearLayout layout = (LinearLayout)this.findViewById(R.id.leafBoardLayout);
			layout.setVisibility(View.VISIBLE);
		}
		else {
		}
	}
	
	private void setLeafBoard() {
		TextView usernameTv = (TextView)this.findViewById(R.id.leafBoardUsername);
		usernameTv.setText("用户名：" + UserInfoUtil.GetUserName(this));
		
		TextView boardNameTv = (TextView)this.findViewById(R.id.leafBoardBoardname);
		boardNameTv.setText("版名：" + boardInfo.get(0));
		
		TextView topicCntTv = (TextView)this.findViewById(R.id.leafBoardTopicCount);
		topicCntTv.setText("本版帖子总数：" + boardInfo.get(1));
		
		// set data
		ArrayList<HashMap<String, String>> displist = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < topicTitles.size(); i++) {
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put(this.getString(R.string.leafItemTitle), topicTitles.get(i));
        	map.put(this.getString(R.string.leafItemText), topicAdditions.get(i));
        	displist.add(map);
        }
        //Toast.makeText(this, "Custom List count:" + displist.size(), Toast.LENGTH_LONG).show();
        
        SimpleAdapter mSchedule = new SimpleAdapter(this,
        		displist,
        		R.layout.leaf_board_list_item,	//	ListItem XML implementation
                new String[] {this.getString(R.string.leafItemTitle), 
        					  this.getString(R.string.leafItemText)}, // dynamic array and ListItem correspondings        
                new int[] {R.id.leafItemTitle, R.id.leafItemText}); // ListItem XML's two TextView ID

        // set custom list view and listener
     	ListView topicLv = (ListView)this.findViewById(R.id.leafBoardTopicList);
     	topicLv.setAdapter(mSchedule);
     	Utility.setListViewHeightBasedOnChildren(topicLv);
     	
     	// set view
     	topicLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String postUrl = homePage + topicUrls.get(position);
				Toast.makeText(getApplicationContext(), "Url:" + postUrl, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(LeafBoardActivity.this, SinglePostActivity.class);
				intent.putExtra(postUrlName, postUrl);
				LeafBoardActivity.this.startActivity(intent);
        	}
        });
	}
}
