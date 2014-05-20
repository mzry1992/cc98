package android.application.cc98;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.application.cc98.network.SinglePostTask;
import android.application.cc98.view.Utility;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SinglePostActivity extends LoadWebPageActivity implements
		OnClickListener {

	// store data from HTML, updated when loading following posts
	private ArrayList<String> authors;
	private ArrayList<String> references;
	private ArrayList<String> contents;
	private ArrayList<String> timestamps;
	private ArrayList<String> replyIDs;
	private ArrayList<String> rawContents;
	
	// global variables to store data of the topic list
	private ArrayList<String> postInfo;

	// variables to record post information
	private int postCount, pageCount, displayedPage = 0;
	private String  postTitle, postUrl;

	// UI and data
	private Button postMoreBtn;
	private SinglePostAdapter listAdapter;
	
	//mark status
	private boolean firstPageLoadSucess = false;
	
	// reference
	private int referPos = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		postUrl = intent.getStringExtra(this.getString(R.string.postUrl));
		StringBuilder sb = new StringBuilder();
		sb.append(postUrl);
		sb.append("&star=");
		postUrl = sb.toString();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.refresh_menu, menu);
		refreshItem = menu.findItem(R.id.refresh);
		msgMenuItem = menu.findItem(R.id.message);
		msgMenuItem.setIcon(this.getResources().getDrawable(R.drawable.toolbar_comment_icon));
		msgMenuItem.setVisible(true);
		
		msgMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (replyIDs.size() > 0) {
					String followup = replyIDs.get(0);
					startReply(followup, null, null, null);
				}
				return true;
			}
		});
		
		preLoadPage();
		return true;
	}
	
	@Override
	public void loadPage() {
		msgMenuItem.setEnabled(false);
		new SinglePostTask(this, serverName).execute(cookie, postUrl + "1");
	}

	@Override
	public void loadPageSucess(Object output) {
		
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) output;
		ArrayList<String> status = outputs.get(0);
		
		if (status.get(0).equals("3")) {
			setContentView(R.layout.single_post);
			postMoreBtn = (Button) this.findViewById(R.id.singlePostMoreButton);
			postMoreBtn.setOnClickListener(this);
			msgMenuItem.setEnabled(true);
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

		// first post page loading finish
		if (status.get(0).equals("3")) {

			firstPageLoadSucess = true;
			
			postInfo = outputs.get(1);

			postCount = Integer.parseInt(postInfo.get(0));
			pageCount = Integer.parseInt(postInfo.get(1));
			postTitle = postInfo.get(2);
			displayedPage = 1;

			authors = outputs.get(2);
			references = outputs.get(3);
			contents = outputs.get(4);
			timestamps = outputs.get(5);
			replyIDs = outputs.get(6);
			rawContents = outputs.get(7);
			
			// set single post UI
			setSinglePostUI();

			// set layout visible
			LinearLayout layout = (LinearLayout) this
					.findViewById(R.id.singlePostLayout);
			layout.setVisibility(View.VISIBLE);
		}
		// following post page loading finish
		else if (status.get(0).equals("5")) {

			authors.addAll(outputs.get(2));
			references.addAll(outputs.get(3));
			contents.addAll(outputs.get(4));
			timestamps.addAll(outputs.get(5));
			replyIDs.addAll(outputs.get(6));
			rawContents.addAll(outputs.get(7));
			
			// update UI for following posts
			updateSinglePostUI();
		} 
	}

	private void setSinglePostUI() {
		TextView postTitleTv = (TextView) this
				.findViewById(R.id.singlePostTitle);
		postTitleTv.setText(postTitle);

		TextView postInfoTv = (TextView) this.findViewById(R.id.singlePostInfo);
		postInfoTv.setText("回帖总数：" + (postCount - 1));

		if (displayedPage < pageCount)
			postMoreBtn.setText(this.getString(R.string.morePostText));
		else
			postMoreBtn.setText(this.getString(R.string.noMorePostText));
		
		ListView postLv = (ListView) this.findViewById(R.id.singlePostList);
		listAdapter = new SinglePostAdapter(getApplicationContext());
		listAdapter.setData(authors, references, contents, timestamps);
		postLv.setAdapter(listAdapter);
		Utility.setListViewHeightBasedOnChildren(postLv);
		
		// listen items
		postLv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				System.out.println("Long Click item:" + position);
				referPos = position;
				referDialog();
				return true;
			}
		});
	}

	private void updateSinglePostUI() {
		listAdapter.notifyDataSetChanged();
		
		if (displayedPage < pageCount)
			postMoreBtn.setText(this.getString(R.string.morePostText));
		else
			postMoreBtn.setText(this.getString(R.string.noMorePostText));
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.singlePostMoreButton:
			// System.out.println("Click singlePostMoreButton");
			if (displayedPage >= pageCount)
				postMoreBtn.setText(this.getString(R.string.noMorePostText));
			else {
				displayedPage++;
				String followingPostUrl = postUrl
						+ Integer.toString(displayedPage);
				System.out.println("Loading follow post URL: "
						+ followingPostUrl);
				postMoreBtn.setText(this
						.getString(R.string.loadingMorePostText));
				new SinglePostTask(this, serverName).execute(cookie,
						followingPostUrl);
			}
			break;
		}
	}
	
	private void referDialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("确认引用此发言并回复？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				assert(referPos >= 0);
				String referAuthor = authors.get(referPos);
				String referTimestamp = timestamps.get(referPos);
				String referContent = rawContents.get(referPos);
				String followup = replyIDs.get(referPos);
				startReply(followup, referContent, referAuthor, referTimestamp);

				//this.finish();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
			}
		});
		builder.show();
	}
	
	private void startReply(String followup, String referContent, String referAuthor, String referTimestamp) {
		int idx1 = postUrl.indexOf("&ID=");
		int idx2 = postUrl.indexOf('&', idx1 + 1);
		String rootID = postUrl.substring(idx1 + 4, idx2);
		idx1 = postUrl.indexOf("boardID=");
		idx2 = postUrl.indexOf('&', idx1);
		String boardID = postUrl.substring(idx1 + 8, idx2);
		
		Intent intent = new Intent(SinglePostActivity.this, ReplyPostActivity.class);
		intent.putExtra(getResources().getString(R.string.replyReferer), postUrl);
		intent.putExtra(getResources().getString(R.string.replyFollowup), followup);
		intent.putExtra(getResources().getString(R.string.replyRootID), rootID);
		intent.putExtra(getResources().getString(R.string.replyBoardID), boardID);
		intent.putExtra(getResources().getString(R.string.replyReferContent), referContent);
		intent.putExtra(getResources().getString(R.string.replyReferAuthor), referAuthor);
		intent.putExtra(getResources().getString(R.string.replyReferTimestamp), referTimestamp);
		SinglePostActivity.this.startActivity(intent);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		// 退出
		if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
			setContentView(R.layout.loading);
			preLoadPage();
		}
	}
}

class SinglePostAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private Context context;
	private ArrayList<String> authors;
	private ArrayList<String> references;
	private ArrayList<String> contents;
	private ArrayList<String> timestamps;
	
	public SinglePostAdapter(Context context) {
		this.context = context;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setData(ArrayList<String> authors, ArrayList<String> references,
						ArrayList<String> contents, ArrayList<String> timestamps) {
		this.authors = authors;
		this.references = references;
		this.contents = contents;
		this.timestamps = timestamps;
	}
	
	@Override
	public int getCount() {
		return authors.size();
	}
	
	@Override
	public Object getItem(int position) {
		return authors.get(position);
	}
	
	@Override  
    public long getItemId(int position) {  
        return position;  
    } 
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = inflater.inflate(R.layout.single_post_list_item, null);
		
		TextView floorTv = (TextView) convertView.findViewById(R.id.postFloorText);
		TextView authorTv = (TextView) convertView.findViewById(R.id.postAuthorText);
		TextView timestampTv = (TextView) convertView.findViewById(R.id.postTimestampText);
		LinearLayout contentLayout = (LinearLayout)convertView.findViewById(R.id.singlePostContentItem);
		contentLayout.removeAllViews();
		
		int i = position;
		floorTv.setText(" " + i + "F ");
		authorTv.setText(this.authors.get(i));
		timestampTv.setText(this.timestamps.get(i));
		
		// add reference	
		if (references.get(i).length() > 0) {
			TextView referenceTv = new TextView(context.getApplicationContext());
			referenceTv.setText(references.get(i));
			referenceTv.setTextColor(context.getResources().getColor(R.color.darkgrey));
			referenceTv.setBackgroundColor(context.getResources().getColor(R.color.thingrey));
			referenceTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			         LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(5, 5, 5, 5);
			referenceTv.setLayoutParams(layoutParams);
			contentLayout.addView(referenceTv);
		}
		
		// add text content
		TextView contentTv = new TextView(context.getApplicationContext());
		contentTv.setText(contents.get(i));
		contentTv.setTextColor(context.getResources().getColor(R.color.black));
		contentTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
		         LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 5, 0, 12);
		contentTv.setLayoutParams(layoutParams);
		contentLayout.addView(contentTv);
		//System.out.println(contentTv.getText().toString());
					
		return convertView;
	}
}