package android.application.cc98;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.application.cc98.network.ReplyPostTask;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class ReplyPostActivity extends Activity implements OnClickListener, GetWebPageInterface {

	private EditText replySubjectET, replyContentET;
	private ImageView backButton, submitButton;
	private GridView faceView, gridView;
	private int faceLastPos = 6;
	SimpleAdapter faceAdapter;
	
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.reply_post);
		
        Intent intent = getIntent();
        String referContent = intent.getStringExtra(this.getString(R.string.replyReferContent));
        StringBuilder content = new StringBuilder();
        if (referContent != null && referContent.length() > 0) {
            content.append("[quotex][b]以下是引用[i]");
            String referAuthor = intent.getStringExtra(this.getString(R.string.replyReferAuthor));
            content.append(referAuthor);
            content.append("在");
            String refertime = intent.getStringExtra(this.getString(R.string.replyReferTimestamp));
            content.append(refertime);
            content.append("[/i]的发言：[/b]\n");
            content.append(referContent);
            content.append("\n\n[/quotex]\n");
        }
        replySubjectET = (EditText)this.findViewById(R.id.replySubjectEditText);
        replyContentET = (EditText)this.findViewById(R.id.replyContentEditText);
        replyContentET.setText(content.toString());
        backButton = (ImageView)this.findViewById(R.id.replyBackButton);
        submitButton = (ImageView)this.findViewById(R.id.replySubmitButton);
        backButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        
        gridView = (GridView)this.findViewById(R.id.replyGridView);
        gridView.setAdapter(new ExpressionAdapter(this));
        gridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Toast.makeText(NewPostActivity.this, "" + position, Toast.LENGTH_SHORT).show();
            	StringBuilder sb= new StringBuilder();
            	sb.append("[em");
            	if (position < 10) sb.append('0');
            	sb.append(position);
            	sb.append(']');
            	replyContentET.append(sb.toString());
            }
        });
        
        initFaceView();
	}
	
	private void initFaceView() {
		faceView = (GridView)this.findViewById(R.id.replyFaceView);
		ArrayList<HashMap<String, Object>> mList1 = new ArrayList<HashMap<String, Object>>();
		int expDrawable = R.drawable.face01 - 1;
		for (int i = 1; i <= 22; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			if (i != 7) map.put("radioIcon", R.drawable.radiobutton_off);
			else map.put("radioIcon", R.drawable.radiobutton_on);
			map.put("radioImage", expDrawable + i);
			mList1.add(map);
		}
		faceAdapter = new SimpleAdapter(getApplicationContext(), mList1, R.layout.face_item, 
				new String[]{"radioIcon","radioImage"}, new int[]{R.id.item_radioImage,R.id.item_faceImage});
		faceView.setAdapter(faceAdapter);
		//Utility.setGridViewHeightBasedOnChildren(faceView);
		faceView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				if (faceLastPos != position) {
					if (faceLastPos >= 0) {
						changeItemImg(faceAdapter, faceLastPos, false);
					}				
				}
				faceLastPos = position;
				changeItemImg(faceAdapter, position, true);	
			}	
		});
	}
	
	private void changeItemImg(SimpleAdapter sa, int selectedItem, boolean isOn) {
		HashMap<String, Object> map = (HashMap<String, Object>)sa.getItem(selectedItem);
		if (isOn) map.put("radioIcon", R.drawable.radiobutton_on);
		else map.put("radioIcon", R.drawable.radiobutton_off);
		sa.notifyDataSetChanged();
	}
	
	public void onClick(View v){
    	switch (v.getId()) {
    	case R.id.replySubmitButton:
    		Intent intent = getIntent();
    		String homePage = UserInfoUtil.getHomePageURL(this);
    		
            String boardID = intent.getStringExtra(this.getString(R.string.replyBoardID));
    		String followup = intent.getStringExtra(this.getString(R.string.replyFollowup));
            String rootID = intent.getStringExtra(this.getString(R.string.replyRootID));
            
            // set content, include reference
            String subject = replySubjectET.getText().toString();
            String content = replyContentET.getText().toString();
            
            // set cookie
            String cookie = UserInfoUtil.GetCookieInfo(this);
            String username = UserInfoUtil.GetUserName(this);
            int idx1 = cookie.indexOf("password=");
            int idx2 = cookie.indexOf(';', idx1);
            String pwd = cookie.substring(idx1 + 9, idx2);
            
            // set url
            StringBuilder postUrl = new StringBuilder();
            postUrl.append(homePage);
            postUrl.append("SaveReAnnounce.asp?method=Topic&boardID=");
            postUrl.append(boardID);
            postUrl.append("&bm=");

            // set referer url
            StringBuilder referer = new StringBuilder();
            referer.append(homePage);
            referer.append("reannounce.asp?BoardID=");
            referer.append(boardID);
            referer.append("&id=");
            referer.append(rootID);
            referer.append("&star=1");
            
            // set expression
            String expression = "face" + (faceLastPos + 1) + ".gif";
            // execute network request
            new ReplyPostTask(this).execute(postUrl.toString(), username, pwd, 
            								subject, content, followup, rootID, referer.toString(), cookie, expression);
            break;
            
    	case R.id.replyBackButton:
    		this.finish();
    		break;
    	}
	}
	
	@Override
	public void getWebPagePreProgress() {
	}
	
	@Override
	public void getWebPagePostProgress(Object outputs) {
		String[] status = (String[])outputs;
		int statusCode = Integer.parseInt(status[0]);
		switch (statusCode) {
		case 3:
			Toast.makeText(this, "回复成功！", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(); 
			intent.setClass(this, SinglePostActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			break;
		case 2:
			Toast.makeText(this, "服务器异常，错误码：" + status[1], Toast.LENGTH_LONG).show();
			break;
		case 4:
			Toast.makeText(this, "网络异常，请检查您的网络配置", Toast.LENGTH_LONG).show();
			break;
		case 0:
		default:
			Toast.makeText(this, "未知错误，请联系开发人员", Toast.LENGTH_LONG).show();
			break;
		}
	}
	
	@Override
	public void getWebPageProgressUpdate() {
		
	}
}
