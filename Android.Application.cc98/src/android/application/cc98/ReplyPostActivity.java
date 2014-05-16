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
		
        replySubjectET = (EditText)this.findViewById(R.id.replySubjectEditText);
        replyContentET = (EditText)this.findViewById(R.id.replyContentEditText);
        submitButton = (Button)this.findViewById(R.id.replySubmitButton);
        submitButton.setOnClickListener(this);
	}
	
	public void onClick(View v){
    	switch (v.getId()) {
    	case R.id.replySubmitButton:
    		Intent intent = getIntent();
            String boardID = intent.getStringExtra(this.getString(R.string.replyBoardID));
    		String followup = intent.getStringExtra(this.getString(R.string.replyFollowup));
            String rootID = intent.getStringExtra(this.getString(R.string.replyRootID));
            String subject = replySubjectET.getText().toString();
            String content = replyContentET.getText().toString();
            String cookie = UserInfoUtil.GetCookieInfo(this);
            String username = UserInfoUtil.GetUserName(this);
            int idx1 = cookie.indexOf("password=");
            int idx2 = cookie.indexOf(';', idx1);
            String pwd = cookie.substring(idx1 + 9, idx2);
            
            StringBuilder postUrl = new StringBuilder();
            postUrl.append(UserInfoUtil.getHomePageURL(this));
            postUrl.append("SaveReAnnounce.asp?method=fastreply&BoardID=");
            postUrl.append(boardID);

            new ReplyPostTask(this).execute(postUrl.toString(), username, pwd,
            								subject, content, followup, rootID, cookie);
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
			System.out.println("Entity:" +status[1]);
			System.out.println("Cookie:" +status[2]);
			this.finish();
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
