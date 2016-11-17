package com.klin1344.cardify;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;

public class ReviewCards extends Activity {
	private GridView setOverview;
	private static ArrayList<Card> myCards;
	private ImageAdapter2 setAdapter;
	private String titleName;
	private TextView noCards;
	private boolean hasCards = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		switch(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_key_app_theme", "0"))) {
		case 0:
			setTheme(R.style.HoloLightDarkActionBar);
			break;
		case 1:
			setTheme(R.style.HoloLight);
			break;
		case 2:
			setTheme(R.style.HoloDark);
			break;
		default:
			setTheme(R.style.HoloLightDarkActionBar);
			break;
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_cards);

		myCards = new ArrayList<Card>();

		titleName = getIntent().getStringExtra("selectedSet");

		SharedPreferences savedCardsPrefs = getSharedPreferences(titleName , 0);
		int numOfCards = savedCardsPrefs.getInt("cardsSize" , 0);

		for (int i = 0; i < numOfCards; i++) {
			myCards.add(new Card((savedCardsPrefs.getString("Word" + i, null)), (savedCardsPrefs.getString("Definition" + i, null))));
		}
		
		if (!myCards.isEmpty()) {
			noCards = (TextView) findViewById(R.id.no_cards);
			noCards.setText("");
			hasCards = true;
		}
		else {
			noCards = (TextView) findViewById(R.id.no_cards);
			noCards.setText(R.string.no_cards);
		}
		setOverview = (GridView) findViewById(R.id.card_set_overview);
		setOverview.setAdapter(new ImageAdapter2(this, myCards));
		setAdapter = (ImageAdapter2) setOverview.getAdapter(); 

		/* setOnItemClickListener for when the user chooses a set to open */
		setOverview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				/* create Intent to go to ReviewCards */
				Intent viewCard = new Intent(ReviewCards.this, ViewCard.class);
				viewCard.putExtra("position", position);
				viewCard.putExtra("titleName", titleName);
				startActivity(viewCard);
			}
		});

		/* this code is for the CAB, to be able to use multi selection like the Gmail app */
		setOverview = (GridView) findViewById(R.id.card_set_overview);
		setOverview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		setOverview.setHapticFeedbackEnabled(true);
		setOverview.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				/* get the number of checked items */
				//int selectCount = setOverview.getCheckedItemCount();
				/* update CAB with the number of selected out of the total size of the setOverview GridView */
				mode.setTitle(R.string.select_items);
				mode.setSubtitle(setOverview.getCheckedItemCount() + " of " + setOverview.getCount() + " items selected");
				//setOverview.setSelection(position);
				//setOverview.setItemChecked(position, true);
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case R.id.action_delete:
					/* delete only the selected items */
					DeleteSelectedItems(setOverview.getCheckedItemPositions());
					mode.finish();
					return true;
				case R.id.action_delete_all:
					/* delete all items */
					DeleteAllItems();
					mode.finish();
					return true;
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.cab_review_cards, menu);
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
		});

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(titleName);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.review_cards, menu);
		final TextView noCardsText = (TextView) findViewById(R.id.noCardsText);
		//noResultsText.setVisibility(View.GONE);

		MenuItem searchItem = menu.findItem(R.id.action_search_cards);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) searchItem.getActionView();

		// Assumes current activity is the searchable activity
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false); // Do not iconify the widget;
		// expand it by default
		searchView.setQueryHint("Search flashcards");

		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				for (int i = 0; i < menu.size(); i++) {
					menu.getItem(i).setVisible(true);
					setOverview.setVisibility(View.VISIBLE);
				}
				return true; // Return true to collapse action view
			}

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				for (int i = 0; i < menu.size(); i++) {
					menu.getItem(i).setVisible(false);
				}
				return true; // Return true to expand action view
			}
		});

		SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {
				setOverview.setVisibility(View.VISIBLE);
				noCardsText.setVisibility(View.GONE);
				int j = 0;
				boolean resultsFound = false;
				// compare the first letter, then filter out the results

				if (!newText.isEmpty()) {
					for (int i = 0; i < myCards.size(); i++) {
						if (myCards.get(i).getWord().contains(newText)) {
							Collections.swap(myCards, i, j);
							j++;
							resultsFound = true;
						}
					}
					setAdapter.notifyDataSetChanged();
				}
				if ((!resultsFound) && (!newText.isEmpty())) {
					setOverview.setVisibility(View.GONE);
					noCardsText.setText(ReviewCards.this.getString(R.string.no_results) + " " + newText);
					noCardsText.setVisibility(View.VISIBLE);

				}
				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}
		};

		searchView.setOnQueryTextListener(queryTextListener);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_new_card:
			AddCard();
			return true;
		case R.id.action_sort_cards_random:
			RandomShuffle();
			return true;
		case R.id.action_sort_cards_alphabetically:
			SortAlphabetically();
			return true;
		case android.R.id.home:
			super.onBackPressed();
			return true;
		case R.id.action_settings:
			Intent settingsIntent = new Intent(ReviewCards.this, SettingsActivity.class);
			startActivity(settingsIntent);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void SortAlphabetically() {
		Collections.sort(myCards, new Comparator<Card>() {
			public int compare(Card card1, Card card2) {
				return card1.getWord().compareTo(card2.getWord()); 
			}
		});
		setAdapter.notifyDataSetChanged();
	}

	private void RandomShuffle() {
		Collections.shuffle(myCards);
		setAdapter.notifyDataSetChanged();
	}

	private void AddCard() {
		Intent addCardIntent = new Intent(ReviewCards.this, AddCard.class);
		addCardIntent.putExtra("titleName", titleName);
		startActivityForResult(addCardIntent, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){     
				myCards.add(new Card(data.getStringExtra("word"), data.getStringExtra("definition")));
				setAdapter.notifyDataSetChanged();   
				if (!hasCards) {
					noCards = (TextView) findViewById(R.id.no_cards);
					noCards.setText("");
					hasCards = true;
				}

			}
		}
	}

	private void DeleteSelectedItems(final SparseBooleanArray checkedItems) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setTitle(R.string.delete);
		alertDialogBuilder.setMessage(R.string.confirm_delete_selected);
		alertDialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				//SparseBooleanArray checkedItems = setOverview.getCheckedItemPositions();
				int size = setAdapter.getCount();
				for (int i = size - 1; i >= 0; i--) {
					if (checkedItems.valueAt(i)) {
						if (checkedItems.keyAt(i) >= size) {
						}
						else {
							setAdapter.remove(checkedItems.keyAt(i));
						}
					}
				}

				//checkedItems.clear();
				setAdapter.notifyDataSetChanged();
				if (myCards.isEmpty() && hasCards == true) {
					noCards.setText(R.string.no_cards);
					hasCards = false;
				}
			}	
		});

		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});
		alertDialogBuilder.create().show();
	}

	private void DeleteAllItems() {
		/* create new AlertDialog.Builder */
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setTitle(R.string.delete_all);
		alertDialogBuilder.setMessage(R.string.confirm_delete_all);
		alertDialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				/* call removeAll in ImageAdapter, then update */
				myCards.clear();
				//setAdapter.removeAll();
				setAdapter.notifyDataSetChanged();
				if (hasCards) {
					noCards.setText(R.string.no_cards);
					hasCards = false;
				}
			}	
		});

		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});
		/* show dialog */
		alertDialogBuilder.create().show();
	}

	@Override
	protected void onPause() {
		super.onPause();
		/* store data in SharedPreferences on onPause() */
		SharedPreferences savedCardsPrefs = getSharedPreferences(titleName, 0);
		SharedPreferences.Editor editor = savedCardsPrefs.edit();
		editor.clear();
		/* loop through every title in ArrayList cardsTitle*/
		for (int i = 0; i < myCards.size(); i++) {
			/* store each String element with a key */
			editor.putString("Word" + i, myCards.get(i).getWord());
			editor.putString("Definition" + i, myCards.get(i).getDefinition());
		}
		editor.putInt("cardsSize", myCards.size());
		editor.commit();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myCards.clear();
	}

	@Override
	protected void onResume() {
		super.onResume();
		setAdapter.notifyDataSetChanged();
	}

	public static ArrayList<Card> getCards() {
		return myCards;
	}
}

