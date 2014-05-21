package android.application.cc98.fragment;

import android.app.Fragment;
import android.application.cc98.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LoadingFragment extends Fragment{
	
	View loadingLayout = null;

	public LoadingFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		loadingLayout = inflater.inflate(R.layout.loading,
				container, false);
		return loadingLayout;
	}

}
