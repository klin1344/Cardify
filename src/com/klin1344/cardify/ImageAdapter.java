package com.klin1344.cardify;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;


public class ImageAdapter extends BaseAdapter {
	/* declare stuff */
	private Context context;
	private ArrayList<String> cardsTitle;
	private ArrayList<String> cardsNumbers;
	private int paddingTop;
	private int paddingRight;
	private int paddingLeft;
	private int paddingBottom;
	private int paddingTop1;
	private int height;

	/* constructor the ImageAdapter */
	public ImageAdapter(Context context, ArrayList<String> cardsTitle, ArrayList<String> cardsNumbers) {
		/* get the context so we can use it in this non-Activity class */
		this.context = context;
		/* get the ArrayList cardsTitle */
		this.cardsTitle = cardsTitle;
		this.cardsNumbers = cardsNumbers;

		/* use getDisplayMetrics().density to get the scale for use with multiplying later to get the correct dip for resizing and padding */
		final float scale = context.getResources().getDisplayMetrics().density;
		paddingLeft = (int) (8 * scale + 0.5f);
		paddingTop = (int) (28 * scale + 0.5f);
		paddingRight = (int) (38 * scale + 0.5f);
		paddingBottom = (int) (50 * scale + 0.5f);
		height = (int) (155 * scale + 0.5f);
		

		paddingTop1 = (int) (66 * scale + 0.5f);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TextView cardsTitleView;
		TextView cardsNumber;
		CheckableLayout checkLayout;
		
		if (convertView == null) {
			cardsTitleView = new TextView(context);
			cardsTitleView.setTextSize(16);
			cardsTitleView.setTextColor(Color.BLACK);
			cardsTitleView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
			cardsTitleView.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, height));
			cardsTitleView.setGravity(Gravity.CENTER);
			cardsTitleView.setBackgroundResource(R.drawable.ic_flashcard_stack);
			//cardsTitleView.setId(2);
			
			cardsNumber = new TextView(context);
			cardsNumber.setTextSize(12);
			cardsNumber.setTextColor(Color.GRAY);
			cardsNumber.setPadding(paddingLeft, paddingTop1, paddingRight, 0);
			//RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			//params.addRule(RelativeLayout.ALIGN_BOTTOM, cardsTitleView.getId());
			//cardsNumber.setLayoutParams(params);
			//cardsNumber.setLayoutParams(new .LayoutParams(LayoutParams.MATCH_PARENT, height));
			cardsNumber.setGravity(Gravity.CENTER);
			
			checkLayout = new CheckableLayout(context);
			checkLayout.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT));
			checkLayout.addView(cardsTitleView);	
			checkLayout.addView(cardsNumber);
		}
		/* if convertView is already created, recycle the Views */
		else {
			checkLayout = (CheckableLayout) convertView;
			cardsTitleView = (TextView) checkLayout.getChildAt(0);
			cardsNumber = (TextView) checkLayout.getChildAt(1);
		}
		
		cardsNumber.setText(cardsNumbers.get(position));
		cardsTitleView.setText(cardsTitle.get(position));
		return checkLayout;
	}


	/* get the size of cardsTitle ArrayList */
	@Override
	public int getCount() {
		return cardsTitle.size();
	}

	/* get a specific item using an index */
	@Override
	public String getItem(int position) {
		return cardsTitle.get(position);
	}

	/* get the item id; not used right now */
	@Override
	public long getItemId(int position) {
		return 0;
	}

	/* get the position  */
	public int getPosition(int position) {
		return position;
	}
	
	public void remove(int position) {
		cardsTitle.remove(position);
		cardsNumbers.remove(position);
		
	}
}