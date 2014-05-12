package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import android.application.cc98.network.SinglePostTask;
import android.application.cc98.view.Utility;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SinglePostActivity extends LoadWebPageActivity implements
		OnClickListener {

	// store data from HTML, updated when loading following posts
	private ArrayList<String> authors;
	private ArrayList<String> references;
	private ArrayList<String> contents;
	private ArrayList<String> timestamps;

	// global variables to store data of the topic list
	private ArrayList<String> postInfo;

	// variables to record post information
	private int postCount, pageCount, displayedPage = 0;
	private String cookie, serverName, postTitle, postUrl;

	// UI and data
	private Button postMoreBtn;
	private SinglePostAdapter listAdapter;
	
	//mark status
	private boolean firstPageLoadSucess = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		postUrl = intent.getStringExtra(this.getString(R.string.postUrl));
		StringBuilder sb = new StringBuilder();
		sb.append(postUrl);
		sb.append("&star=");
		postUrl = sb.toString();

		serverName = this.getString(R.string.serverName);
	}
	
	@Override
	public void loadPage() {
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

			// update UI for following posts
			updateSinglePostUI();
		} 
	}

	private void setSinglePostUI() {
		TextView postTitleTv = (TextView) this
				.findViewById(R.id.singlePostTitle);
		postTitleTv.setText(postTitle);

		TextView postInfoTv = (TextView) this.findViewById(R.id.singlePostInfo);
		postInfoTv.setText("»ØÌû×ÜÊý£º" + (postCount - 1));

		if (displayedPage < pageCount)
			postMoreBtn.setText(this.getString(R.string.morePostText));
		else
			postMoreBtn.setText(this.getString(R.string.noMorePostText));
		
		ListView postLv = (ListView) this.findViewById(R.id.singlePostList);
		listAdapter = new SinglePostAdapter(getApplicationContext());
		listAdapter.setData(authors, references, contents, timestamps);
		postLv.setAdapter(listAdapter);
		Utility.setListViewHeightBasedOnChildren(postLv);
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
		System.out.println(contentTv.getText().toString());
					
		return convertView;
	}
}