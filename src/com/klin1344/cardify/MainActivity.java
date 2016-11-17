package com.klin1344.cardify;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static ArrayList<String> cardsTitle;
	private GridView cardsOverview;
	private boolean titleAlreadySet = false;
	private ImageAdapter cardsAdapter;
	private TextView flashcardsTitleView;
	private ArrayList<String> cardsNumbers;
	private TextView noSets;

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
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences showDialogPrefs = getSharedPreferences("showDialog", 0);
		boolean showDialog = showDialogPrefs.getBoolean("showDialog", true);

		if (showDialog == true) {
			donateDialog("donate");
		}

		SharedPreferences cardsTitlesPrefs = getSharedPreferences("savedCardSets", 0);

		cardsTitle = new ArrayList<String>();
		int cardsTitleSize = cardsTitlesPrefs.getInt("titleSize", 0);

		for (int i = 0; i < cardsTitleSize; i++) {
			cardsTitle.add(cardsTitlesPrefs.getString("CardSet" + i, null));
		}

		if (!cardsTitle.isEmpty()) {
			flashcardsTitleView = (TextView) findViewById(R.id.flashcardsTitleView);
			flashcardsTitleView.setText(R.string.my_flashcards);
			noSets = (TextView) findViewById(R.id.no_sets);
			noSets.setText("");
			titleAlreadySet = true;
		}
		else {
			noSets = (TextView) findViewById(R.id.no_sets);
			noSets.setText(R.string.no_sets);
		}

		cardsNumbers = new ArrayList<String>();
		CountCards();

		cardsOverview = (GridView) findViewById(R.id.my_cards_overview);
		cardsOverview.setAdapter(new ImageAdapter(this, cardsTitle, cardsNumbers));
		cardsAdapter = (ImageAdapter) cardsOverview.getAdapter();

		cardsOverview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent openCards = new Intent(MainActivity.this, ReviewCards.class);
				openCards.putExtra("selectedSet", cardsTitle.get(position));
				startActivity(openCards);
			}
		});

		cardsOverview = (GridView) findViewById(R.id.my_cards_overview);
		cardsOverview.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
		cardsOverview.setHapticFeedbackEnabled(true);
		cardsOverview.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
				int selectCount = cardsOverview.getCheckedItemCount();
				mode.setTitle(R.string.select_items);
				mode.setSubtitle(selectCount + " of " + cardsOverview.getCount() + " items selected");
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case R.id.action_delete:
					//SparseBooleanArray checkedItems = cardsOverview.getCheckedItemPositions();
					DeleteSelectedItems(cardsOverview.getCheckedItemPositions());
					mode.finish();
					return true;
				case R.id.action_delete_all:
					DeleteAllItems();
					mode.finish();
					return true;

				case R.id.action_save:
					SaveToStorage();
					mode.finish();
					return true;
				default:
					return false;
				}
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.cab_main, menu);
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
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		final TextView noResultsText = (TextView) findViewById(R.id.noResultsText);

		MenuItem searchItem = menu.findItem(R.id.action_search_sets);

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) searchItem.getActionView();

		// Assumes current activity is the searchable activity
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(false); // Do not iconify the widget;
		// expand it by default
		searchView.setQueryHint("Search flashcard sets");

		searchItem.setOnActionExpandListener(new OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				for (int i = 0; i < menu.size(); i++) {
					menu.getItem(i).setVisible(true);
					cardsOverview.setVisibility(View.VISIBLE);
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
				cardsOverview.setVisibility(View.VISIBLE);
				noResultsText.setVisibility(View.GONE);
				int j = 0;
				boolean resultsFound = false;
				if (!newText.isEmpty()) {
					for (int i = 0; i < cardsTitle.size(); i++) {
						if (cardsTitle.get(i).contains(newText)) {
							Collections.swap(cardsTitle, i, j);
							j++;
							resultsFound = true;
						}
					}
					cardsAdapter.notifyDataSetChanged();
				}
				if ((!resultsFound) && (!newText.isEmpty())) {
					cardsOverview.setVisibility(View.GONE);
					noResultsText.setText(MainActivity.this.getString(R.string.no_results) + " " + newText);
					noResultsText.setVisibility(View.VISIBLE);

				}
				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}
		};
		searchView.setOnQueryTextListener(queryTextListener);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_new_set:
			AddNewSet();
			return true;
		case R.id.action_sort_sets_random:
			RandomShuffle();
			return true;
		case R.id.action_sort_sets_alphabetically:
			SortAlphabetically();
			return true;
		case R.id.action_settings:
			Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(settingsIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void SortAlphabetically() {
		Collections.sort(cardsTitle, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.compareToIgnoreCase(s2);
			}
		});
		cardsAdapter.notifyDataSetChanged();
	}

	private void RandomShuffle() {
		Collections.shuffle(cardsTitle);
		cardsAdapter.notifyDataSetChanged();
	}

	private void AddNewSet() {
		final EditText editName = new EditText(this);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(R.string.add_set);
		alertDialogBuilder.setMessage(R.string.set_name);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setView(editName);

		alertDialogBuilder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String name = editName.getText().toString();
				if (name.isEmpty()) {
					dialog.cancel();
					Toast.makeText(MainActivity.this, R.string.card_is_empty, Toast.LENGTH_SHORT).show();
				} 
				else if (cardsTitle.contains(name)){
					dialog.cancel();
					Toast.makeText(MainActivity.this, R.string.duplicate_set, Toast.LENGTH_SHORT).show();
				}
				else {
					cardsTitle.add(name);
					cardsNumbers.add(0 + " " + getResources().getString(R.string.cards));
					cardsAdapter.notifyDataSetChanged();
					if (!titleAlreadySet) {
						flashcardsTitleView = (TextView) findViewById(R.id.flashcardsTitleView);
						flashcardsTitleView.setText(R.string.my_flashcards);
						noSets = (TextView) findViewById(R.id.no_sets);
						noSets.setText("");
						titleAlreadySet = true;
					}
				}
			}
		});

		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alertDialog.show();
	}

	private void DeleteSelectedItems(final SparseBooleanArray checkedItems) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setTitle(R.string.delete);
		alertDialogBuilder.setMessage(R.string.confirm_delete_selected);
		alertDialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//SparseBooleanArray checkedItems = cardsOverview.getCheckedItemPositions();
				int size = cardsAdapter.getCount();
				//ArrayList<Integer> selected = new ArrayList<Integer>();
				//Log.d("size ", size + "");
				for (int i = size - 1; i >= 0; i--) {
					int checkedKey = checkedItems.keyAt(i);
					if (checkedItems.valueAt(i)) {
						//Log.d("kevin " + i, checkedItems.keyAt(i) + "");

						if (checkedKey >= size) {
						}
						else {
							SharedPreferences cardsTitlesPrefs = getSharedPreferences(cardsAdapter.getItem(checkedKey), 0);
							cardsTitlesPrefs.edit().clear().commit();
							File SharedPrefsFile = new File(getFilesDir().getParent() + "/shared_prefs/" + cardsAdapter.getItem(checkedKey) + ".xml");
							SharedPrefsFile.delete();
							cardsAdapter.remove(checkedItems.keyAt(i));
							//checkedItems.clear();
						}
					}
				}
				//checkedItems.clear();
				cardsAdapter.notifyDataSetChanged();

				if (cardsTitle.isEmpty() && titleAlreadySet == true) {
					flashcardsTitleView.setText("");
					noSets.setText(R.string.no_sets);
					titleAlreadySet = false;
				}
			}
		});

		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
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
			public void onClick(DialogInterface dialog, int id) {
				/* call removeAll in ImageAdapter, then update */
				for (int i = 0; i < cardsTitle.size(); i++) {
					File SharedPrefsFile = new File("data/data/com.klin1344.cardify/shared_prefs/" + cardsAdapter.getItem(i) + ".xml");
					SharedPrefsFile.delete();
				}

				cardsTitle.clear();
				cardsAdapter.notifyDataSetChanged();

				if (titleAlreadySet) {
					flashcardsTitleView.setText("");
					noSets.setText(R.string.no_sets);
					titleAlreadySet = false;
				}
			}
		});

		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		/* show dialog */
		alertDialogBuilder.create().show();
	}

	private void donateDialog(String dialogType) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setTitle(R.string.welcome);
		alertDialogBuilder.setMessage(R.string.donate);
		alertDialogBuilder.setPositiveButton(R.string.donate_button, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Uri donationLink = Uri.parse("http://www.foodforthepoor.org/");
				Intent donationIntent = new Intent(Intent.ACTION_VIEW, donationLink);
				startActivity(donationIntent);
			}
		});

		alertDialogBuilder.setNeutralButton(R.string.dismiss, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});

		/* button to never show this popup again */
		alertDialogBuilder.setNegativeButton(R.string.dont_show_again, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				/*
				 * on button click, store a false boolean in
				 * SharedPreferences
				 */
				SharedPreferences showDialogPrefs = getSharedPreferences("showDialog", 0);
				SharedPreferences.Editor editor = showDialogPrefs.edit();
				editor.putBoolean("showDialog", false);
				editor.commit();
			}
		});
		/* show dialog */
		alertDialogBuilder.create().show();
	}

	private void SaveToStorage() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.setTitle(R.string.save);
		alertDialogBuilder.setMessage(R.string.save_to_storage);
		alertDialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String state = Environment.getExternalStorageState();
				if (Environment.MEDIA_MOUNTED.equals(state)) {
					File HoloFlashcardsDir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/Cardify");
					HoloFlashcardsDir.mkdirs();
					SparseBooleanArray checkedItems = cardsOverview.getCheckedItemPositions();
					ArrayList<Card> temp = new ArrayList<Card>();
					for (int j = cardsOverview.getCount() - 1; j >= 0; j--) {
						if (checkedItems.valueAt(j)) {
							int itemSelected = cardsAdapter.getPosition(checkedItems.keyAt(j));
							File file = new File(HoloFlashcardsDir, cardsTitle.get(itemSelected) + ".txt");
							SharedPreferences savedCardsPrefs = getSharedPreferences(cardsTitle.get(itemSelected), 0);
							int numOfCards = savedCardsPrefs.getInt("cardsSize" , 0);
							for (int p = 0; p < numOfCards; p++) {
								temp.add(new Card((savedCardsPrefs.getString("Word" + p, null)), (savedCardsPrefs.getString("Definition" + p, null))));
							}
							try {
								file.createNewFile();
								BufferedWriter out = new BufferedWriter(new FileWriter(file));
								for (int k = 0; k < temp.size(); k++) {
									out.write("Word " + (k + 1) + ":" + "\n" + temp.get(k).getWord() + " - " + temp.get(k).getDefinition() + "\n" + "\n");
									out.flush();
								}	
								out.close();
								temp.clear();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				} 
				else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
					Toast.makeText(MainActivity.this, R.string.storage_not_available, Toast.LENGTH_SHORT).show();
					dialog.cancel();
				} 
				else {
					Toast.makeText(MainActivity.this, R.string.storage_not_available, Toast.LENGTH_SHORT).show();
					dialog.cancel();
				}
				Toast.makeText(MainActivity.this, R.string.cards_saved, Toast.LENGTH_LONG).show();
			}
		});

		alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		alertDialogBuilder.create().show();
	}

	private void CountCards() {
		cardsNumbers.clear();
		for (int i = 0; i < cardsTitle.size(); i++) {
			SharedPreferences savedCardsPrefs = getSharedPreferences(cardsTitle.get(i), 0);
			cardsNumbers.add(savedCardsPrefs.getInt("cardsSize" , 0) + " " + getResources().getString(R.string.cards));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		/* store data in SharedPreferences on onPause() */
		SharedPreferences cardsTitlesPrefs = getSharedPreferences("savedCardSets", 0);
		SharedPreferences.Editor editor = cardsTitlesPrefs.edit();
		editor.clear();
		/* loop through every title in ArrayList cardsTitle */
		for (int i = 0; i < cardsTitle.size(); i++) {
			/* store each String element with a key */
			editor.putString("CardSet" + i, cardsTitle.get(i));
		}
		editor.putInt("titleSize", cardsTitle.size());
		editor.commit();

		//CountCards();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cardsTitle.clear();
	}

	@Override
	protected void onResume() {
		super.onResume();
		CountCards();
		cardsAdapter.notifyDataSetChanged();
	}

	public static ArrayList<String> getCardsTitles() {
		return cardsTitle;
	}

}
