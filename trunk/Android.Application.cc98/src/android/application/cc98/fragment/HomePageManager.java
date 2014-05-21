package android.application.cc98.fragment;

import java.util.ArrayList;

import android.application.cc98.FragmentHomeActivity;
import android.application.cc98.GetWebPageInterface;
import android.application.cc98.R;
import android.application.cc98.network.HomePageTask;
import android.application.cc98.network.HotPostTask;
import android.application.cc98.network.UserInfoUtil;
import android.widget.Toast;

public class HomePageManager implements GetWebPageInterface {

	private CustomizationFragment customizationFragment = null;
	private BoardFragment boardFragment = null;

	private boolean isNetworkRequest = false;

	private boolean isLoadData = false;

	private boolean isCookieValid = false;

	private FragmentHomeActivity activity = null;

	// homePageURL
	private String homePageURL = null, cookies = null, serverName = null;

	public HomePageManager(FragmentHomeActivity activity) {
		// TODO Auto-generated constructor stub
		this.activity = activity;
		customizationFragment = new CustomizationFragment();
		boardFragment = new BoardFragment();
		homePageURL = UserInfoUtil.getHomePageURL(activity);
		serverName = activity.getString(R.string.serverName);
		
	}

	public CustomizationFragment getCustomizationFragment() {
		return customizationFragment;
	}

	public BoardFragment getBoardFragment() {
		return boardFragment;
	}

	public void getWebPagePreProgress() {

	}

	public void getWebPageProgressUpdate() {

	}

	public void loadData() {
		if (isNetworkRequest || isLoadData)
			return;
		cookies = UserInfoUtil.GetCookieInfo(activity);
		if (cookies == null) {
			handleLoginError();
			return;
		}
		isCookieValid = true;
		isNetworkRequest = true;
		new HomePageTask((GetWebPageInterface) this, serverName).execute(cookies,
				homePageURL);
	}

	public void getWebPagePostProgress(Object status) {

		isNetworkRequest = false;
		ArrayList<ArrayList<String>> statusStr = (ArrayList<ArrayList<String>>) status;
		int statusCode = Integer.parseInt(statusStr.get(0).get(0));
		boolean isLoadSucess = false;
		StringBuilder errorStrBuilder = new StringBuilder();

		switch (statusCode) {
		case 1:
			errorStrBuilder.append("用户登录信息无法认证，请重新登录");
			break;
		case 2:
			errorStrBuilder.append("cc98服务器异常, code:");
			errorStrBuilder.append(statusStr.get(0).get(1));
			break;
		case 3:
		case 5:
			loadPageSucess(status);
			isLoadSucess = true;
			break;
		case 4:
			errorStrBuilder.append("网络异常，请检查网络");
			break;
		default:
			errorStrBuilder.append("未知错误，请联系开发人员");
			break;
		}

		if (!isLoadSucess) {
			Toast.makeText(activity, errorStrBuilder.toString(),
					Toast.LENGTH_LONG).show();
			handleError(statusCode);
		}

		
	}

	private void loadPageSucess(Object status) {
		isLoadData = true;
		customizationFragment.fillContent(status);
		boardFragment.fillContent(status);
		activity.refresh();
	}

	private void handleError(int statusCode) {
		if (statusCode == 1) {
			handleLoginError();
			return;
		}
		activity.refresh();
	}

	private void handleLoginError() {
		isCookieValid = false;
		activity.jumpToLogin();
		activity.refresh();
	}

	public boolean isCookieValid() {
		if (cookies == null)
			return false;
		return isCookieValid;
	}

	public void setCookie(String cookies) {
		this.cookies = cookies;
		isCookieValid = true;
	}

	public boolean isDataLoad() {
		return isLoadData;
	}

	public void invalidData() {
		isLoadData = false;
	}

	// 0 represent loading data
	// 1 get normal data
	// 2 can't login
	// 3 network error
	public int getFragmentStatus() {
		if (isNetworkRequest)
			return 0;
		if (isLoadData)
			return 1;
		if (!isCookieValid)
			return 2;
		return 3;
	}

}
