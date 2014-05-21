package android.application.cc98.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.application.cc98.BBSListActivity;
import android.application.cc98.HomePageActivity;
import android.application.cc98.LeafBoardActivity;
import android.application.cc98.R;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class CustomizationFragment extends Fragment {

	private View customizationLayout = null;
	
	private ArrayList<String> customBoardNames = null;
	private ArrayList<String> customBoardUrls = null;
	private ArrayList<String> customBoardDescripts = null;
	
	private String homePage = null, boardUrlName = null;
	
	public CustomizationFragment() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		customizationLayout = inflater.inflate(R.layout.customization,
				container, false);	
		homePage = UserInfoUtil.getHomePageURL(getActivity());
		boardUrlName = getActivity().getString(R.string.boardUrl);
		return customizationLayout;
	}
	
	
	public void fillContent(Object outputRes) {
		
		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) outputRes;

		customBoardNames = outputs.get(1);
		customBoardUrls = outputs.get(2);
		customBoardDescripts = outputs.get(3);
		
		setCustomBoard();

	}

	private void setCustomBoard() {
		// set data
		ArrayList<HashMap<String, String>> displist = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < customBoardNames.size(); i++) {
			HashMap<String, String> map = new HashMap<String, String>();
			map.put(this.getString(R.string.customItemTitle),
					customBoardNames.get(i));
			map.put(this.getString(R.string.customItemText),
					customBoardDescripts.get(i));
			displist.add(map);
		}
		// Toast.makeText(this, "Custom List count:" + displist.size(),
		// Toast.LENGTH_LONG).show();

		SimpleAdapter mSchedule = new SimpleAdapter(this.getActivity(), displist,
				R.layout.home_custom_list_item, // ListItem XML implementation
				new String[] { this.getString(R.string.customItemTitle),
						this.getString(R.string.customItemText) }, // dynamic
																	// array and
																	// ListItem
																	// correspondings
				new int[] { R.id.customItemTitle, R.id.customItemText }); // ListItem
																			// XML's
																			// two
																			// TextView
																			// ID

		// set custom list view and listener
		ListView customLv = (ListView) customizationLayout
				.findViewById(R.id.homePageCustomList);
		customLv.setAdapter(mSchedule);
		Utility.setListViewHeightBasedOnChildren(customLv);

		// set view
		customLv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String boardUrl = homePage + customBoardUrls.get(position);
				String titleName = customBoardNames.get(position);
				Intent intent = null;
				if (titleName.contains("("))
					intent = new Intent(getActivity(),
							BBSListActivity.class);
				else
					intent = new Intent(getActivity(),
							LeafBoardActivity.class);
				intent.putExtra(boardUrlName, boardUrl);
				getActivity().startActivity(intent);
			}
		});
	}

}
