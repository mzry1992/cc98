package android.application.cc98;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.application.cc98.network.SinglePostTask;
import android.application.cc98.view.Utility;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
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
		msgMenuItem.setIcon(this.getResources().getDrawable(R.drawable.ic_action_message));
		msgMenuItem.setVisible(true);
		msgMenuItem.setEnabled(false);
		
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
		listAdapter = new SinglePostAdapter(getApplicationContext(), SinglePostActivity.this);
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

	public void jumpToWebView(String url) {
        Intent intent = new Intent(SinglePostActivity.this, WebViewActivity.class);
        intent.putExtra(this.getString(R.string.webViewHyperlink), url);
        //intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        System.out.println("Open url:" + url);
        this.startActivity(intent);
	}
}

class SinglePostAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private Context context;
	private SinglePostActivity activity;
	private ArrayList<String> authors;
	private ArrayList<String> references;
	private ArrayList<String> contents;
	private ArrayList<String> timestamps;
	
	public SinglePostAdapter(Context context, SinglePostActivity activity) {
		this.context = context;
		this.activity = activity;
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
		LinearLayout layout = (LinearLayout)convertView.findViewById(R.id.singlePostContentItem);
		layout.removeAllViews();
		
		int i = position;
		floorTv.setText(" " + i + "F ");
		authorTv.setText(this.authors.get(i));
		timestampTv.setText(this.timestamps.get(i));
		
		// add reference	
		if (references.get(i).length() > 0) {
			String referText = references.get(i);
			setContentLayout(layout, referText, 5, 5, 5, 5, 
					13, R.color.darkgrey, R.color.thingrey, R.color.blue);
		}
		
		// add text content
		{
			String contentText = contents.get(i);
			setContentLayout(layout, contentText, 0, 5, 0, 12, 
					16, R.color.black, R.color.white, R.color.blue);
		}			
		return convertView;
	}

	private void setContentLayout(LinearLayout layout, String contentText,
									int left, int top, int right, int bottom,
									int size, int textColor, int bgColor, int linkColor) {
		LinearLayout contentLayout = new LinearLayout(context.getApplicationContext());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
		         LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(left, top, right, bottom);
		layoutParams.gravity = Gravity.LEFT;
		contentLayout.setLayoutParams(layoutParams);
		contentLayout.setGravity(Gravity.LEFT);
		contentLayout.setOrientation(LinearLayout.VERTICAL);
		
		// Pattern for recognizing a URL, based off RFC 3986
		Pattern urlPattern = Pattern.compile(
		        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
		                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
		                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
		        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher urlMatcher = urlPattern.matcher(contentText);
		/*while (urlMatcher.find()) {
			int matchStart = urlMatcher.start();
		    int matchEnd = urlMatcher.end();
		    System.out.println("Link:" + contentText.substring(matchStart, matchEnd));
		    // now you have the offsets of a URL match
		}*/
		
		Pattern expPattern = Pattern.compile("\\[em[0-1][0-9]\\]");
		Matcher expMatcher = expPattern.matcher(contentText);
		/*while (expMatcher.find()) {
			int matchStart = expMatcher.start();
		    int matchEnd = expMatcher.end();
		    System.out.println("Exp:" + contentText.substring(matchStart, matchEnd));
		}*/
		
		int startIndex = 0, endIndex = contentText.length();
		boolean hasUrl = urlMatcher.find();
		boolean hasExp = expMatcher.find();
		while (startIndex < endIndex) {
			if (!hasUrl && !hasExp) {
				TextView tv = makeTextView(contentText.substring(startIndex, endIndex), size, textColor, bgColor);
				contentLayout.addView(tv);
				break;
			}
			
			int urlStart = (hasUrl)? urlMatcher.start() : endIndex;
			int expStart = (hasExp)? expMatcher.start() : endIndex;
			if (urlStart < expStart) {
				// before textview
				TextView tv = makeTextView(contentText.substring(startIndex, urlStart), size, textColor, bgColor);
				contentLayout.addView(tv);
				// hyperlink
				int urlEnd = urlMatcher.end();
				TextView linkTv = makeTextView(contentText.substring(urlStart, urlEnd), size, linkColor, bgColor);
				contentLayout.addView(linkTv);
				System.out.println("link:" + contentText.substring(urlStart, urlEnd));
				linkTv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						TextView urlTv = (TextView)v;
						System.out.println("Jump to url:" + urlTv.getText().toString());
						activity.jumpToWebView(urlTv.getText().toString());
					}
				});
				// update index and matcher
				startIndex = urlEnd;
				hasUrl = urlMatcher.find();
			}
			else {
				// before textview
				TextView tv = makeTextView(contentText.substring(startIndex, expStart), size, textColor, bgColor);
				contentLayout.addView(tv);
				// expression
				int expEnd = expMatcher.end();
				ImageView imgView = makeExpression(contentText.substring(expStart + 1, expEnd - 1));
				contentLayout.addView(imgView);
				System.out.println("exp:" + contentText.substring(expStart, expEnd));
				// update index and matcher
				startIndex = expEnd;
				hasExp = expMatcher.find();
			}
		}
		/*
		while (true) {
			// get expression and link index
			int length = contentText.length();
			int expIdx = contentText.indexOf("[em");
			int expIdx1 = contentText.indexOf(']', expIdx);
			if (expIdx == -1 || expIdx1 - expIdx != 5) expIdx = length;
			if (expIdx1 - expIdx == 5) {
				String exp = contentText.substring(expIdx + 1, expIdx1);
				boolean isExp = (exp.charAt(2) == '0' && exp.charAt(3) >= '0' && exp.charAt(3) <= '9') ||
								(exp.charAt(2) == '1' && exp.charAt(3) >= '0' && exp.charAt(3) <= '1');
				if (!isExp) expIdx = length;
			}
			int linkIdx = contentText.indexOf("http://");
			if (linkIdx == -1) linkIdx = length;
			
			// set textview
			if (expIdx == length && linkIdx == length) {
				TextView tv = makeTextView(contentText, size, textColor, bgColor);
				contentLayout.addView(tv);
				break;
			}
			// set expression
			else if (expIdx < linkIdx) {
				// before text
				TextView tv = makeTextView(contentText.substring(0, expIdx), size, textColor, bgColor);
				contentLayout.addView(tv);
				// expression
				System.out.println("expIdx:" + (expIdx) + " expIdx1:" + (expIdx1));
				System.out.println("Exp:" + contentText.substring(expIdx + 1, expIdx1));
				String exp = contentText.substring(expIdx + 1, expIdx1);
				ImageView imgView = makeExpression(exp);
				contentLayout.addView(imgView);
				contentText = contentText.substring(expIdx1 + 1);
			}
			// set hyperlink
			else { // linkIdx < expIdx
				// before text
				TextView tv = makeTextView(contentText.substring(0, linkIdx), size, textColor, bgColor);
				contentLayout.addView(tv);
				// hyperlink
				int endIdx0 = contentText.indexOf(' ', linkIdx);
				int endIdx1 = contentText.indexOf('\t', linkIdx);
				int endIdx2 = contentText.indexOf('\n', linkIdx);
				int endIdx = Math.min(endIdx0, Math.min(endIdx1, endIdx2));
				if (endIdx <= linkIdx) endIdx = length;
				System.out.println("linkIdx:" + linkIdx + " endIdx:" + endIdx);
				System.out.println("Link:" + contentText.substring(linkIdx, endIdx));
				TextView linkTv = makeTextView(contentText.substring(linkIdx, endIdx), size, linkColor, bgColor);
				contentLayout.addView(linkTv);
				contentText = contentText.substring(endIdx);
			}
		}
		*/
		layout.addView(contentLayout);
	}
	
	private TextView makeTextView(String text, int size, int textColor, int bgColor) {
		TextView tv = new TextView(context.getApplicationContext());
		tv.setText(text);
		tv.setTextColor(context.getResources().getColor(textColor));
		tv.setBackgroundColor(context.getResources().getColor(bgColor));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		tv.setGravity(Gravity.LEFT);
		return tv;
	}
	
	private ImageView makeExpression(String exp) {
		int expDrawable = -1;
		int num = Integer.parseInt(exp.substring(2));
		switch (num) {
			case 0: expDrawable = R.drawable.em00; break;
			case 1: expDrawable = R.drawable.em01; break;
			case 2: expDrawable = R.drawable.em02; break;
			case 3: expDrawable = R.drawable.em03; break;
			case 4: expDrawable = R.drawable.em04; break;
			case 5: expDrawable = R.drawable.em05; break;
			case 6: expDrawable = R.drawable.em06; break;
			case 7: expDrawable = R.drawable.em07; break;
			case 8: expDrawable = R.drawable.em08; break;
			case 9: expDrawable = R.drawable.em09; break;
			case 10: expDrawable = R.drawable.em10; break;
			case 11: expDrawable = R.drawable.em11; break;
			default: expDrawable = -1; break;
		}
		ImageView imgView = new ImageView(context.getApplicationContext());
		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), expDrawable);
		imgView.setImageBitmap(bm);
		//imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		return imgView;
	}
}
