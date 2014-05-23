package android.application.cc98.view;

import java.lang.reflect.Field;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class Utility {
	public static void setListViewHeightBasedOnChildren(ListView listView) {
		if (listView == null) return;
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		int count = listAdapter.getCount();
		for (int i = 0; i < count; i++) { // listAdapter.getCount()返回数据项的数目
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (count - 0));
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		listView.setLayoutParams(params);
	}

	public static void setGridViewHeightBasedOnChildren(GridView gridView) {
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		int count = listAdapter.getCount();
		View listItem = listAdapter.getView(0, null, gridView);
		listItem.measure(0, 0); // 计算子项View 的宽高
		totalHeight = listItem.getMeasuredHeight() + 10; // 统计所有子项的总高度
		int yu = count % 4;
		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		if (yu > 0) {
			params.height = (count - yu) / 4 * (totalHeight + 10) + totalHeight;
		} else {
			params.height = count / 4 * totalHeight + (count / 4 - 1) * 10;
		}
		gridView.setLayoutParams(params);
	}

	// /**
	// * get GridView height according to every children
	// *
	// * @param view
	// * @return
	// */
	public static int getGridViewHeightBasedOnChildren(GridView view, int numColumns) {
		int height = getAbsListViewHeightBasedOnChildren(view);
		ListAdapter adapter;
		int adapterCount;
		if (view != null && (adapter = view.getAdapter()) != null
				&& (adapterCount = adapter.getCount()) > 0 && numColumns > 0) {
			int rowCount = (int) Math.ceil(adapterCount / (double) numColumns);
			height = rowCount
					* (height / adapterCount + getGridViewVerticalSpacing(view));
		}
		ViewGroup.LayoutParams params = view.getLayoutParams();
		params.height = height;
		view.setLayoutParams(params);
		return height;
	}

	/**
	 * get AbsListView height according to every children
	 * 
	 * @param view
	 * @return
	 */
	public static int getAbsListViewHeightBasedOnChildren(AbsListView view) {
		ListAdapter adapter;
		if (view == null || (adapter = view.getAdapter()) == null) {
			return 0;
		}

		int height = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			View item = adapter.getView(i, null, view);
			if (item instanceof ViewGroup) {
				item.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
			item.measure(0, 0);
			height += item.getMeasuredHeight();
		}
		height += view.getPaddingTop() + view.getPaddingBottom();
		return height;
	}

	/**
	 * get GridView vertical spacing
	 * 
	 * @param view
	 * @return
	 */
	public static int getGridViewVerticalSpacing(GridView view) {
		// get mVerticalSpacing by android.widget.GridView
		Class<?> demo = null;
		int verticalSpacing = 0;
		try {
			//demo = Class.forName(CLASS_NAME_GRID_VIEW);
			//Field field = demo.getDeclaredField(FIELD_NAME_VERTICAL_SPACING);
			//field.setAccessible(true);
			//verticalSpacing = (Integer) field.get(view);
			return verticalSpacing;
		} catch (Exception e) {
			/**
			 * accept all exception, include ClassNotFoundException,
			 * NoSuchFieldException, InstantiationException,
			 * IllegalArgumentException, IllegalAccessException,
			 * NullPointException
			 */
			e.printStackTrace();
		}
		return verticalSpacing;
	}

	public static String parseLoginName(Element body) {
		String name = null;
		Elements elements = body.getElementsByClass("TopLighNav1");
		if (elements.size() > 0) {
			Elements brNames = elements.get(0).getElementsByTag("b");
			if (brNames.size() > 0)
				name = brNames.get(0).text();
		}
		return name;
	}
}
