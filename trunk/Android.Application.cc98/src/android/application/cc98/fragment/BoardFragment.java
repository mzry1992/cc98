package android.application.cc98.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.application.cc98.BBSListActivity;
import android.application.cc98.HomePageActivity;
import android.application.cc98.R;
import android.application.cc98.network.UserInfoUtil;
import android.application.cc98.view.GrapeGridView;
import android.application.cc98.view.Utility;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class BoardFragment extends Fragment {

	private View boardLayout = null;

	private ArrayList<String> defaultBoardNames = null;
	private ArrayList<String> defaultBoardUrls = null;

	private String homePage = null, boardUrlName = null;

	public BoardFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		boardLayout = inflater.inflate(R.layout.board, container, false);
		homePage = UserInfoUtil.getHomePageURL(getActivity());
		boardUrlName = getActivity().getString(R.string.boardUrl);
		return boardLayout;
	}

	public void fillContent(Object outputRes) {

		ArrayList<ArrayList<String>> outputs = (ArrayList<ArrayList<String>>) outputRes;

		defaultBoardNames = outputs.get(4);
		defaultBoardUrls = outputs.get(5);

		setDefaultBoard();

	}

	private void setDefaultBoard() {
		// set data
		ArrayList<HashMap<String, Object>> displist = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < defaultBoardNames.size(); i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			String boardName = defaultBoardNames.get(i).trim();
			int imageID = getBoardImageID(boardName);
			map.put(this.getString(R.string.defaultItemTitle), boardName);
			map.put(this.getString(R.string.defaultItemImage), imageID);
			displist.add(map);
		}


		SimpleAdapter mSchedule = new SimpleAdapter(getActivity(), displist,
				R.layout.home_default_list_item, // ListItem XML implementation
				new String[] { 	this.getString(R.string.defaultItemTitle),
								this.getString(R.string.defaultItemImage)}, // dynamic
																			// array
																			// and
																			// ListItem
																			// correspondings
				new int[] { R.id.defaultItemTitle, R.id.defaultItemImage }); // ListItem XML's two
														// TextView ID

		// set custom list view and listener
		GrapeGridView defaultGv = (GrapeGridView) boardLayout
				.findViewById(R.id.homePageDefaultGrid);
		defaultGv.setAdapter(mSchedule);
		Utility.getGridViewHeightBasedOnChildren(defaultGv,4);

		// set view
		defaultGv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String boardUrl = homePage + defaultBoardUrls.get(position);
				/*
				 * Toast.makeText(getApplicationContext(), "Url:" + boardUrl,
				 * Toast.LENGTH_SHORT).show();
				 */
				Intent intent = new Intent(getActivity(), BBSListActivity.class);
				intent.putExtra(boardUrlName, boardUrl);
				getActivity().startActivity(intent);
			}
		});
	}

	private int getBoardImageID(String boardName) {
		if (boardName.equals("教师答疑"))
			return R.drawable.teacher1;
		if (boardName.equals("学习天地"))
			return R.drawable.book3;
		if (boardName.equals("校园动态"))
			return R.drawable.news2;
		if (boardName.equals("信息资讯"))
			return R.drawable.network1;
		if (boardName.equals("个性生活"))
			return R.drawable.coffee;
		if (boardName.equals("休闲娱乐"))
			return R.drawable.ice_cream;
		if (boardName.equals("体育运动"))
			return R.drawable.sport;
		if (boardName.equals("影音无限"))
			return R.drawable.movie1;
		if (boardName.equals("电脑技术"))
			return R.drawable.computer1;
		if (boardName.equals("社科学术"))
			return R.drawable.science1;
		if (boardName.equals("游戏广场"))
			return R.drawable.game1;
		if (boardName.equals("动漫天地"))
			return R.drawable.mickey;
		if (boardName.equals("感性空间"))
			return R.drawable.love;
		if (boardName.equals("瞬间永恒"))
			return R.drawable.time1;
		if (boardName.equals("交易代理"))
			return R.drawable.sale;
		if (boardName.equals("论坛管理"))
			return R.drawable.management;
		if (boardName.equals("院系交流"))
			return R.drawable.communication;
		if (boardName.equals("社团风采"))
			return R.drawable.people;
		if (boardName.equals("天下一家"))
			return R.drawable.world;
		return R.drawable.cc98_smallest;
	}

}
