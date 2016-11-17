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


public class ImageAdapter2 extends BaseAdapter {
	private Context context;
	private ArrayList<Card> cardNames;
	private int paddingTop;
	private int paddingRight;
	private int paddingLeft;
	private int paddingBottom;
	private int height;

	public ImageAdapter2(Context context, ArrayList<Card> cardNames) {
		/* get the context so we can use it in this non-Activity class */
		this.context = context;
		/* get the ArrayList cardNames */
		this.cardNames = cardNames;

		/* use getDisplayMetrics().density to get the scale for use with multiplying later to get the correct dip for resizing and padding */
		final float scale = context.getResources().getDisplayMetrics().density;
		paddingLeft = (int) (3 * scale + 0.5f);
		paddingTop = (int) (3 * scale + 0.5f);
		paddingRight = (int) (3 * scale + 0.5f);
		paddingBottom = (int) (5 * scale + 0.5f);
		height = (int) (130 * scale + 0.5f);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView reviewCardsView;
		CheckableLayout checkLayout;
		if (convertView == null) {
			reviewCardsView = new TextView(context);
			reviewCardsView.setTextSize(14);
			reviewCardsView.setTextColor(Color.BLACK);
			reviewCardsView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
			reviewCardsView.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, height));
			reviewCardsView.setGravity(Gravity.CENTER);
			//reviewCardsView.setBackgroundResource(typedValue.resourceId);
			/* the the flashcard stack drawable as background */
			reviewCardsView.setBackgroundResource(R.drawable.ic_flashcard);
			checkLayout = new CheckableLayout(context);
			checkLayout.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT));
			checkLayout.addView(reviewCardsView);
		}
		else {
			checkLayout = (CheckableLayout) convertView;
			reviewCardsView = (TextView) checkLayout.getChildAt(0);
		}
		reviewCardsView.setText(cardNames.get(position).getWord());
		return checkLayout;
	}


	@Override
	public int getCount() {
		return cardNames.size();
	}

	@Override
	public Object getItem(int position) {
		return cardNames.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	/* get the position  */
	public int getPosition(int position) {
		return position;
	}

	public void remove(int position) {
		cardNames.remove(position);
		
	}
}
