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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SinglePostActivity extends LoadWebPageActivity implements
		OnClickListener {

	// store data from HTML, updated when loading following posts
	private ArrayList<String> authors;
	private ArrayList<String> faces;
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
		postUrl = sb.toString().toLowerCase();
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
			faces = outputs.get(3);
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
			faces.addAll(outputs.get(3));
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
		listAdapter.setData(authors, faces, contents, timestamps);
		postLv.setAdapter(listAdapter);
		Utility.setListViewHeightBasedOnChildren(postLv);
		
		// listen items
		postLv.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				//System.out.println("Long Click item:" + position);
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
				/*System.out.println("Loading follow post URL: "
						+ followingPostUrl);*/
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
		//System.out.println("Start reply, postUrl:" + postUrl);
		int idx1 = postUrl.indexOf("&id=");
		if (idx1 == -1) idx1 = postUrl.indexOf("?id=");
		int idx2 = postUrl.indexOf('&', idx1 + 1);
		String rootID = postUrl.substring(idx1 + 4, idx2);
		idx1 = postUrl.indexOf("boardid=");
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
        //System.out.println("Open url:" + url);
        this.startActivity(intent);
	}

	public void postLoadImage() {
		ListView postLv = (ListView) this.findViewById(R.id.singlePostList);
		Utility.setListViewHeightBasedOnChildren(postLv);
	}
}

class SinglePostAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	private Context context;
	private SinglePostActivity activity;
	private ArrayList<String> authors;
	private ArrayList<String> faces;
	private ArrayList<String> contents;
	private ArrayList<String> timestamps;
	private final int referenceLineThreshold = 6;
	
	public SinglePostAdapter(Context context, SinglePostActivity activity) {
		this.context = context;
		this.activity = activity;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setData(ArrayList<String> authors, ArrayList<String> faces,
						ArrayList<String> contents, ArrayList<String> timestamps) {
		this.authors = authors;
		this.faces = faces;
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
		
		// add face
		if (faces.get(i) != ""){
			ImageView imgView = makeExpression(faces.get(i), R.drawable.face01 - 1);
			if (imgView != null) {
				LinearLayout expLayout = new LinearLayout(context.getApplicationContext());
				LinearLayout.LayoutParams explayoutParams = new LinearLayout.LayoutParams(
				         LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				explayoutParams.gravity = Gravity.LEFT;
				expLayout.setLayoutParams(explayoutParams);
				expLayout.setGravity(Gravity.LEFT);
				expLayout.setOrientation(LinearLayout.HORIZONTAL);
				expLayout.addView(imgView);
				layout.addView(expLayout);
			}
		}
		// add text content
		{
			String contentText = contents.get(i);
			contentText = removeUrlContent(contentText);
			setContentLayout(layout, contentText, 0, 0, 0, 0, 
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
		//System.out.println("Content:" + contentText);
		
		Pattern urlPattern = Pattern.compile(
				"(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]",
				Pattern.DOTALL);
		/*Pattern.compile(
		        "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
		                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
		                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
		        Pattern.CASE_INSENSITIVE);*/
		Matcher urlMatcher = urlPattern.matcher(contentText);
		Pattern expPattern = Pattern.compile("\\[em[0-9][0-9]\\]");
		Matcher expMatcher = expPattern.matcher(contentText);
		Pattern referPattern = Pattern.compile("\\[(quotex|quote)\\]");
		Matcher refMatcher = referPattern.matcher(contentText);
		
		int startIndex = 0, endIndex = contentText.length();
		boolean hasUrl = urlMatcher.find();
		boolean hasExp = expMatcher.find();
		boolean hasRef = refMatcher.find();
		while (startIndex < endIndex) {
			if (!hasUrl && !hasExp && !hasRef) {
				TextView tv = makeTextView(contentText.substring(startIndex, endIndex), size, textColor, bgColor);
				if (tv != null) contentLayout.addView(tv);
				break;
			}
			
			int urlStart = (hasUrl)? urlMatcher.start() : endIndex;
			int expStart = (hasExp)? expMatcher.start() : endIndex;
			int refStart = (hasRef)? refMatcher.start() : endIndex;
			int minStart = Math.min(urlStart, Math.min(expStart, refStart));
			if (urlStart == minStart) {
				// before textview
				if (startIndex < urlStart) {
					TextView tv = makeTextView(contentText.substring(startIndex, urlStart), size, textColor, bgColor);
					if (tv != null) contentLayout.addView(tv);
				}
				// hyperlink
				int urlEnd = urlMatcher.end();
				if (urlStart < urlEnd) {
					String url = contentText.substring(urlStart, urlEnd);
					int httpIdx = url.indexOf("http"); httpIdx = (httpIdx == -1)? Integer.MAX_VALUE : httpIdx;
					int ftpIdx = url.indexOf("ftp"); ftpIdx = (ftpIdx == -1)? Integer.MAX_VALUE : ftpIdx;
					int fileIdx = url.indexOf("file"); fileIdx = (fileIdx == -1)? Integer.MAX_VALUE : fileIdx;
					int urlIdx = Math.min(httpIdx, Math.min(ftpIdx, fileIdx));
					if (urlIdx > 0 && urlIdx < Integer.MAX_VALUE)
						url = url.substring(urlIdx);

					TextView linkTv = makeTextView(url, size, linkColor, bgColor);
					if (linkTv != null) contentLayout.addView(linkTv);
	
					linkTv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							TextView urlTv = (TextView)v;
							urlTv.setTextColor(context.getResources().getColor(R.color.darkPurple));
							activity.jumpToWebView(urlTv.getText().toString());
						}
					});
				}
				// update index and matcher
				startIndex = urlEnd;
				hasUrl = urlMatcher.find();
			}
			else if (expStart == minStart){
				// before textview
				if (startIndex < expStart) {
					TextView tv = makeTextView(contentText.substring(startIndex, expStart), size, textColor, bgColor);
					if (tv != null) contentLayout.addView(tv);
				}
				// expression layout
				LinearLayout expLayout = new LinearLayout(context.getApplicationContext());
				LinearLayout.LayoutParams explayoutParams = new LinearLayout.LayoutParams(
				         LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				explayoutParams.gravity = Gravity.LEFT;
				expLayout.setLayoutParams(explayoutParams);
				expLayout.setGravity(Gravity.LEFT);
				expLayout.setOrientation(LinearLayout.HORIZONTAL);
				
				while (true) {
					// expression
					int expEnd = expMatcher.end();
					String expStr = contentText.substring(expStart + 1, expEnd - 1);
					ImageView imgView = makeExpression(expStr, R.drawable.em01);
					if (imgView != null)
						expLayout.addView(imgView);
					// update index and matcher
					startIndex = expEnd;
					hasExp = expMatcher.find();
					if (!hasExp) break;
					expStart = expMatcher.start();
					if (expStart != startIndex) break;
				}
				contentLayout.addView(expLayout);
			}
			else { // reference
				// before textview
				if (startIndex < refStart) {
					TextView tv = makeTextView(contentText.substring(startIndex, refStart), size, textColor, bgColor);
					if (tv != null) contentLayout.addView(tv);
				}
				
				// draw reference
				int refEnd = findMatchRefEnd(contentText, refStart);
				if (refStart + 8 < refEnd) {
					StringBuilder referStrSb = new StringBuilder();
					int idx1 = contentText.indexOf("[/b]", refStart);
					referStrSb.append(contentText.substring(refStart, idx1));
					referStrSb.append('\n');
					referStrSb.append(contentText.substring(idx1 + 4, refEnd));
					String referStr = getFirstRefLine(referStrSb.toString());
					setContentLayout(contentLayout, referStr, 5, 5, 5, 5, 
							13, R.color.darkgrey, R.color.thingrey, R.color.blue);
				}
				// update index and matcher
				startIndex = refEnd + 9;
				hasRef = refMatcher.find();
				// neglect exp and links in reference
				while (urlStart < startIndex || expStart < startIndex) {
					if (urlStart < startIndex) {
						hasUrl = urlMatcher.find();
						urlStart = (hasUrl)? urlMatcher.start() : endIndex;
					}
					else if (expStart < startIndex) {
						hasExp = expMatcher.find();
						expStart = (hasExp)? expMatcher.start() : endIndex;
					}
				}
			}
		}

		layout.addView(contentLayout);
	}
	
	private String removeUrlContent(String contentText) {
		Pattern urlPattern = Pattern.compile("\\[url=");
		Matcher urlMatcher = urlPattern.matcher(contentText);
		boolean hasUrl = urlMatcher.find();
		
		
		StringBuilder sb = new StringBuilder();
		int startIndex = 0, endIndex = contentText.length();
		while (true) {
			if (!hasUrl) {
				if (startIndex < endIndex)
					sb.append(contentText.substring(startIndex, endIndex));
				break;
			}
			else {
				int urlStart = urlMatcher.start();
				if (startIndex < urlStart)
					sb.append(contentText.substring(startIndex, urlStart));
				
				int end1 = contentText.indexOf(']', urlStart + 1);
				sb.append(contentText.substring(urlStart, end1 + 1));
				int start2 = contentText.indexOf("[/url]", end1);
				startIndex = start2 + 6;
				hasUrl = urlMatcher.find();
			}
		}
		return sb.toString();
	}
	
	private int findMatchRefEnd(String contentText, int refStart) {
		while (true) {
			int end0 = contentText.indexOf("[/quotex]", refStart);
			end0 = (end0 == -1)? Integer.MAX_VALUE : end0;
			int end1 = contentText.indexOf("[/quote]", refStart);
			end1 = (end1 == -1)? Integer.MAX_VALUE : end1;
			if (end1 > end0) return end0;
			else return end1;
		}
	}
	
	private String getFirstRefLine(String text) {
		String[] lines = removeBrackets(text).split("\n");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 5 && i < lines.length; i++) {
			sb.append(lines[i]);
			sb.append('\n');
		}
		if (lines.length > referenceLineThreshold)
			sb.append(".....");
		int len = sb.length();
		if (len > 0 && sb.charAt(len - 1) == '\n') sb.deleteCharAt(len - 1);
		return sb.toString();
	}
	
	private String removeBrackets(String text) {
		text = text + "]";
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < text.length()) {
			if (text.charAt(i) == '[') {
				if (   i + 1 < text.length() && text.charAt(i + 1) == 'e' 
					&& i + 2 < text.length() && text.charAt(i + 2) == 'm')
					sb.append(text.charAt(i++));
				else {
					int idx = text.indexOf(']', i);
					if (idx != -1 && idx > i + 1) {
						i = idx + 1;
						continue;
					}
				}
			}
			sb.append(text.charAt(i++));
		}
		while (true) {
			int len = sb.length();
			if (len > 0 && 
				(sb.charAt(0) == ']' || sb.charAt(0) == '[')) sb.deleteCharAt(0);
			else break;
		}
		while (true) {
			int len = sb.length() - 1;
			if (len >= 0 && (sb.charAt(len) == ']' || sb.charAt(len) == '['))
				sb.deleteCharAt(len);
			else break;
		}
		if (sb.length() == 0) return "";
		boolean hasReturn = sb.charAt(sb.length() - 1) == '\n';
		while (true) {
			int len = sb.length() - 1;
			if (len >= 0 && sb.charAt(len) == '\n') sb.deleteCharAt(len);
			else break;
		}
		if (hasReturn) sb.append('\n');
		return sb.toString();
	}
	
	private TextView makeTextView(String text, int size, int textColor, int bgColor) {
		text = removeBrackets(text);
		if (text.length() == 0) return null;
		TextView tv = new TextView(context.getApplicationContext());
		tv.setText(text);
		tv.setTextColor(context.getResources().getColor(textColor));
		tv.setBackgroundColor(context.getResources().getColor(bgColor));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
		tv.setGravity(Gravity.LEFT);
		return tv;
	}
	
	private ImageView makeExpression(String expSrc, int startDrawable) {
		int expDrawable = startDrawable;
		int num = 0;
		if (expSrc.startsWith("em")) {
			num = Integer.parseInt(expSrc.substring(2));
			if (num < 0 || num > 92) return null;
		}
		else if (expSrc.startsWith("face")) {
			num = Integer.parseInt(expSrc.substring(4));
			if (num < 1 || num > 22) return null;
		}
		expDrawable += num;
		ImageView imgView = new ImageView(context.getApplicationContext());
		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), expDrawable);
		imgView.setImageBitmap(bm);
		return imgView;
	}
}
