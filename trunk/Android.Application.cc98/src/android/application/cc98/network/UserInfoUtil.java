package android.application.cc98.network;

import android.app.Activity;
import android.application.cc98.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserInfoUtil {

	public static String GetUserName(Activity activity) {
		if (activity == null)
			return null;
		SharedPreferences userSettings = activity.getSharedPreferences(activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_READABLE);  
		return userSettings.getString(activity.getString(R.string.userNameFile), null);
	}
	
	public static String GetPassword(Activity activity) {
		if (activity == null)
			return null;
		SharedPreferences userSettings = activity.getSharedPreferences(activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_READABLE);  
		return userSettings.getString(activity.getString(R.string.passwordFile), null);
	}
	
	public static String GetCookieInfo(Activity activity) {
		if (activity == null)
			return null;
		SharedPreferences userSettings = activity.getSharedPreferences(activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_READABLE);  
		return userSettings.getString(activity.getString(R.string.cookieFile), null);
	}
	
	public static void SetUserInfo(Activity activity, String userName, String pwd) {
		if (activity == null)
			return;
		Editor userSettingsEditor = activity.getSharedPreferences(activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_WRITEABLE).edit();  
		userSettingsEditor.putString(activity.getString(R.string.userNameFile), userName);
		userSettingsEditor.putString(activity.getString(R.string.passwordFile), pwd);
		userSettingsEditor.commit();
	}
	
	public static void SetCookieInfo(Activity activity, String cookie) {
		if (activity == null)
			return;
		Editor userSettingsEditor = activity.getSharedPreferences(activity.getString(R.string.userInfoFileName), Activity.MODE_WORLD_WRITEABLE).edit();  
		userSettingsEditor.putString(activity.getString(R.string.cookieFile), cookie);
		userSettingsEditor.commit();
	}
	
	public static String getSignURL(Activity activity) {
		if (activity == null)
			return null;
		StringBuilder signURLBuilder = new StringBuilder();
		signURLBuilder.append("http://");
		signURLBuilder.append(activity.getString(R.string.serverName)); 
		signURLBuilder.append("/");
		signURLBuilder.append(activity.getString(R.string.signSuffix));
		String signURL = signURLBuilder.toString();
		return signURL;
	}
	
	public static String getHomePageURL(Activity activity) {
		if (activity == null)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append("http://");
		sb.append(activity.getString(R.string.serverName)); 
		sb.append("/");
		String homeUrl = sb.toString();
		return homeUrl;
	}
}
