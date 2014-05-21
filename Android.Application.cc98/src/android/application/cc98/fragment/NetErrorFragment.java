package android.application.cc98.fragment;

import android.app.Fragment;
import android.application.cc98.FragmentHomeActivity;
import android.application.cc98.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class NetErrorFragment extends Fragment {

	View netErrorLayout = null;
	private ImageButton retryButton = null;

	public NetErrorFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		netErrorLayout = inflater.inflate(R.layout.loading_error_net,
				container, false);
		retryButton = (ImageButton)netErrorLayout.findViewById(R.id.retry_image_button);
		if (retryButton != null) {
			retryButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					FragmentHomeActivity activity = (FragmentHomeActivity)getActivity();
					activity.reTry();
				}
			});
		}
		return netErrorLayout;
	}

}
