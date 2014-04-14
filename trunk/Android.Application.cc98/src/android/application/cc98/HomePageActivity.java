package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.application.cc98.network.HomePageTask;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.GrapeGridView;
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

public class HomePageActivity extends Activity implements GetWebPageInterface{

	private ArrayList<String> customBoardNames;
	private ArrayList<String> customBoardUrls;
	private ArrayList<String> customBoardDescripts;
	
	private ArrayList<String> defaultBoardNames;
	private ArrayList<String> defaultBoardUrls;
	
	private String homePage, serverName, boardUrlName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		homePage = UserInfoUtil.getHomePageURL(this);
		serverName = this.getString(R.string.serverName);
		boardUrlName = this.getString(R.string.boardUrl);
		String cookie = UserInfoUtil.GetCookieInfo(this);
		new HomePageTask(this, serverName).execute(cookie, homePage);
	}
	
	@Override
	public void getWebPagePreProgress() {
		// set layout invisible
		LinearLayout layout = (LinearLayout)this.findViewById(R.id.homePageLayout);
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
			TextView username = (TextView)this.findViewById(R.id.homePageUsername);
			username.setText("ÓÃ»§Ãû£º" + UserInfoUtil.GetUserName(this));

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
			LinearLayout layout = (LinearLayout)this.findViewById(R.id.homePageLayout);
			layout.setVisibility(View.VISIBLE);
		}
		else {
			// show error
		}
	}
	
	private void setCustomBoard() {
		// set data
		ArrayList<HashMap<String, String>> displist = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < customBoardNames.size(); i++)
        {
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put(this.getString(R.string.customItemTitle), customBoardNames.get(i));
        	map.put(this.getString(R.string.customItemText), customBoardDescripts.get(i));
        	displist.add(map);
        }
        //Toast.makeText(this, "Custom List count:" + displist.size(), Toast.LENGTH_LONG).show();
        
        SimpleAdapter mSchedule = new SimpleAdapter(this,
        		displist,
        		R.layout.home_custom_list_item,	//	ListItem XML implementation
                new String[] {this.getString(R.string.customItemTitle), 
        					  this.getString(R.string.customItemText)}, // dynamic array and ListItem correspondings        
                new int[] {R.id.customItemTitle,R.id.customItemText}); // ListItem XML's two TextView ID

        // set custom list view and listener
     	ListView customLv = (ListView)this.findViewById(R.id.homePageCustomList);
     	customLv.setAdapter(mSchedule);
     	Utility.setListViewHeightBasedOnChildren(customLv);
     	
     	// set view
		customLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String boardUrl = homePage + customBoardUrls.get(position);
				Toast.makeText(getApplicationContext(), "Url:" + boardUrl, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(HomePageActivity.this, LeafBoardActivity.class);
				intent.putExtra(boardUrlName, boardUrl);
				HomePageActivity.this.startActivity(intent);
        	}
        });
	}

	private void setDefaultBoard() {
		// set data
		ArrayList<HashMap<String, String>> displist = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < defaultBoardNames.size(); i++)
        {
        	HashMap<String, String> map = new HashMap<String, String>();
        	map.put(this.getString(R.string.defaultItemTitle), defaultBoardNames.get(i));
        	displist.add(map);
        }
        //Toast.makeText(this, "Custom List count:" + displist.size(), Toast.LENGTH_LONG).show();
        
        SimpleAdapter mSchedule = new SimpleAdapter(this,
        		displist,
        		R.layout.home_default_list_item,	//	ListItem XML implementation
                new String[] {this.getString(R.string.defaultItemTitle)}, // dynamic array and ListItem correspondings        
                new int[] {R.id.defaultItemTitle}); // ListItem XML's two TextView ID

        // set custom list view and listener
     	GrapeGridView defaultGv = (GrapeGridView)this.findViewById(R.id.homePageDefaultGrid);
     	defaultGv.setAdapter(mSchedule);
     	Utility.setGridViewHeightBasedOnChildren(defaultGv);
     	
		// set view
     	defaultGv.setOnItemClickListener(new OnItemClickListener() {
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        	String boardUrl = homePage + defaultBoardUrls.get(position);
				Toast.makeText(getApplicationContext(), "Url:" + boardUrl, Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(HomePageActivity.this, LeafBoardActivity.class);
				intent.putExtra(boardUrlName, boardUrl);
				HomePageActivity.this.startActivity(intent);
        	}
        });
	}
}

