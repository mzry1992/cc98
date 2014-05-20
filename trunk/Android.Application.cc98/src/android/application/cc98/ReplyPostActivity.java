package android.application.cc98;

import android.app.Activity;
import android.application.cc98.network.ReplyPostTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ReplyPostActivity extends Activity implements OnClickListener, GetWebPageInterface {

	private EditText replySubjectET, replyContentET;
	private Button submitButton;
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
        submitButton = (Button)this.findViewById(R.id.replySubmitButton);
        submitButton.setOnClickListener(this);
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
            
            // execute network request
            new ReplyPostTask(this).execute(postUrl.toString(), username, pwd,
            								subject, content, followup, rootID, referer.toString(), cookie);
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
