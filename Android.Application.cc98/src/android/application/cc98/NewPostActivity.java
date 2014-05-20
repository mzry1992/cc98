package android.application.cc98;

import android.app.Activity;
import android.application.cc98.network.NewPostTask;
import android.application.cc98.network.UserInfoUtil;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewPostActivity extends Activity implements OnClickListener, GetWebPageInterface {
	private EditText newPostSubjectET, newPostContentET;
	private Button submitButton;

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.new_post);
		
        newPostSubjectET = (EditText)this.findViewById(R.id.newPostSubjectEditText);
        newPostContentET = (EditText)this.findViewById(R.id.newPostContentEditText);
        submitButton = (Button)this.findViewById(R.id.newPostSubmitButton);
        submitButton.setOnClickListener(this);
	}
	
	public void onClick(View v){
    	switch (v.getId()) {
    	case R.id.newPostSubmitButton:
    		Intent intent = getIntent();
    		String homePage = UserInfoUtil.getHomePageURL(this);
    		
            String boardID = intent.getStringExtra(this.getString(R.string.newPostBoardID));
            // set content, include reference
            String subject = newPostSubjectET.getText().toString();
            String content = newPostContentET.getText().toString();

            // set cookie
            String cookie = UserInfoUtil.GetCookieInfo(this);
            String username = UserInfoUtil.GetUserName(this);
            int idx1 = cookie.indexOf("password=");
            int idx2 = cookie.indexOf(';', idx1);
            String pwd = cookie.substring(idx1 + 9, idx2);
            
            // set url
            StringBuilder postUrl = new StringBuilder();
            postUrl.append(homePage);
            postUrl.append("/SaveAnnounce.asp?boardID=");
            postUrl.append(boardID);
            
            // set referer url
            StringBuilder referer = new StringBuilder();
            referer.append(homePage);
            referer.append("announce.asp?boardid=");
            referer.append(boardID);
            
            // execute network request
            new NewPostTask(this).execute(postUrl.toString(), username, pwd,
            								subject, content, referer.toString(), cookie);
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
			Toast.makeText(this, "发帖成功！", Toast.LENGTH_LONG).show();
			//System.out.println("Entity: " + status[1]);
			
			Intent intent = new Intent(); 
			intent.setClass(this, LeafBoardActivity.class);
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
