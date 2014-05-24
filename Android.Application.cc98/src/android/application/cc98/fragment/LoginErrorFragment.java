package android.application.cc98.fragment;

import android.support.v4.app.Fragment;
import android.application.cc98.FragmentHomeActivity;
import android.application.cc98.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class LoginErrorFragment extends Fragment {

	private View loginErrorLayout = null;
	private ImageButton retryButton = null;

	public LoginErrorFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		loginErrorLayout = inflater.inflate(R.layout.login_in_error,
				container, false);
		retryButton = (ImageButton)loginErrorLayout.findViewById(R.id.retry_image_button);
		if (retryButton != null) {
			retryButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					FragmentHomeActivity activity = (FragmentHomeActivity)getActivity();
					activity.reTry();
				}
			});
		}
		return loginErrorLayout;
	}

}
