package android.application.cc98;

public interface SignInInterface {
	
	public void SignInPreProgress();
	public void SignInPostProgress(String[] status);
	public void SignInProgressUpdate();
}
