package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.ProgressDialog;
import android.application.cc98.R.color;
import android.application.cc98.network.BBSListTask;
import android.application.cc98.network.HomePageTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BBSListActivity extends LoadWebPageActivity {

	private String boardUrlName = null;
	private String pageURL = null;

	private String pageTitle = null;
	private ListView lv = null;
	TextView headerTextView = null;
	private ArrayList<String> bordTitleArrayList = null;
	private ArrayList<String> infoArrayList = null;
	private ArrayList<String> linkArrayList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boardUrlName = this.getString(R.string.boardUrl);

		Intent intent = getIntent();
		pageURL = intent.getStringExtra(boardUrlName);
	}
	
	@Override
	public void loadPage() {
		new BBSListTask(BBSListActivity.this).execute(pageURL, cookie);
	}
	
	@Override
	public void loadPageSucess(Object output) {
		
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>)output;
		
		setContentView(R.layout.bbslist);

		lv = (ListView) findViewById(R.id.bbslistView);
		headerTextView = new TextView(this);
		lv.addHeaderView(headerTextView);
		headerTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 23f);
		int lightPurple = this.getResources().getColor(R.color.lightPurple);
		headerTextView.setBackgroundColor(lightPurple);
		fillContent(outputs);
	}
	
	public int getStatusCode(Object outputs) {
		ArrayList<ArrayList<String>> outputString = (ArrayList<ArrayList<String>>)outputs;
		ArrayList<String> status = outputString.get(0);
		int statusCode = Integer.parseInt(status.get(0));
		return statusCode;
	}
	
	public String getErrorMessage(Object outputs) {
		ArrayList<ArrayList<String>> outputString = (ArrayList<ArrayList<String>>)outputs;
		ArrayList<String> status = outputString.get(0);
		return status.get(1);
	}

	private void fillContent(ArrayList<ArrayList<String>> outputs) {
		pageTitle = outputs.get(1).get(0);
		bordTitleArrayList = outputs.get(2);
		infoArrayList = outputs.get(3);
		linkArrayList = outputs.get(4);

		SimpleAdapter adapter = new SimpleAdapter(this, getData(),
				R.layout.bbs_list_item, new String[] { "title", "info" },
				new int[] { R.id.list_item_title, R.id.list_item_info });
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position < 1)
					return;
				String boardUrl = "http://" + serverName + "/"
						+ linkArrayList.get(position-1);
				/*Toast.makeText(getApplicationContext(), "URL:" + boardUrl,
						Toast.LENGTH_SHORT).show();*/
				String titleName = bordTitleArrayList.get(position - 1);
				Intent intent = null;
				if (titleName.contains("("))
					intent = new Intent(BBSListActivity.this,
							BBSListActivity.class);
				else
					intent = new Intent(BBSListActivity.this,
							LeafBoardActivity.class);
				intent.putExtra(boardUrlName, boardUrl);
				BBSListActivity.this.startActivity(intent);
			}
		});

		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(pageTitle);
		strBuilder.append("->ÂÛÌ³ÁÐ±í(");
		strBuilder.append(bordTitleArrayList.size());
		strBuilder.append(")");
		headerTextView.setText(strBuilder.toString());
	}

	private ArrayList<HashMap<String, String>> getData() {
		ArrayList<HashMap<String, String>> arrayList = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < bordTitleArrayList.size(); ++i) {
			HashMap<String, String> tempHashMap = new HashMap<String, String>();
			tempHashMap.put("title", bordTitleArrayList.get(i));
			tempHashMap.put("info", infoArrayList.get(i));
			arrayList.add(tempHashMap);
		}
		return arrayList;
	}

}
