package com.klin1344.cardify;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ViewCard extends Activity implements OnClickListener {

	private ArrayList<Card> myCards;
	private TextView wordView;
	private TextView cardHeadline;
	private int counter = 0;
	private int flipSpeed;
	private int slideSpeed;
	private int width;
	private Resources res;

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
		setContentView(R.layout.view_card);

		counter = getIntent().getIntExtra("position", 0);
		myCards = ReviewCards.getCards();

		wordView = (TextView) findViewById(R.id.cardContent);
		cardHeadline = (TextView) findViewById(R.id.cardHeadline);		

		ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
		ImageButton previousButton = (ImageButton) findViewById(R.id.previousButton);
		ImageButton flipButton = (ImageButton) findViewById(R.id.flipToBack);

		nextButton.setOnClickListener(this);
		previousButton.setOnClickListener(this);
		flipButton.setOnClickListener(this);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(getIntent().getStringExtra("titleName"));
		actionBar.setDisplayHomeAsUpEnabled(true);

		SharedPreferences animationPref = PreferenceManager.getDefaultSharedPreferences(this);
		flipSpeed = Integer.parseInt(animationPref.getString("pref_key_anim_flip_speed", "500"));
		slideSpeed = (Integer.parseInt(animationPref.getString("pref_key_anim_slide_speed", "500")) / 2);

		Point size = new Point();
		WindowManager wm = getWindowManager();
		wm.getDefaultDisplay().getSize(size);
		width = size.x;

		res = getResources();

		ShowWord();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.nextButton:
			NextCard();
			break;
		case R.id.previousButton:
			PreviousCard();
			break;
		case R.id.flipToBack:
			FlipCard();
			break;
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.view_card, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit:
			EditCard();
			return true;
		case R.id.action_share:
			Intent sendIntent = new Intent();
			sendIntent.setAction(Intent.ACTION_SEND);
			sendIntent.putExtra(Intent.EXTRA_TEXT, res.getString(R.string.share_intro) + "\n" + "\n" +res.getString(R.string.card_word) + "\n" + myCards.get(counter).getWord() + "\n" + "\n" + res.getString(R.string.card_definition) + "\n" + myCards.get(counter).getDefinition() + "\n" + "\n" +res.getString(R.string.share_ending));
			sendIntent.setType("text/plain");
			startActivity(Intent.createChooser(sendIntent, res.getString(R.string.share_options)));
			return true;
		case android.R.id.home:
			super.onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


	protected void ShowWord() {
		wordView.setText(myCards.get(counter).getWord());
		cardHeadline.setText(R.string.card_word);
	}

	protected void ShowDefinition() {
		wordView.setText(myCards.get(counter).getDefinition());
		cardHeadline.setText(R.string.card_definition);
	}

	private void PreviousCard() {
		if ((counter - 1) < 0) {
			Toast.makeText(ViewCard.this, R.string.out_of_cards, Toast.LENGTH_SHORT).show();
		}
		else {
			ObjectAnimator previousAnimation = ObjectAnimator.ofFloat(wordView, "translationX", 0, width).setDuration(slideSpeed);
			previousAnimation.addListener(new AnimatorListener() {
				@Override 
				public void onAnimationEnd(Animator animation) {  
					ObjectAnimator.ofFloat(wordView, "translationX", -width, 0).setDuration(slideSpeed).start();
				}
				@Override
				public void onAnimationCancel(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationStart(Animator arg0) {
					if (myCards.get(counter).getWord().equals(wordView.getText().toString())) {
						counter--;
						ShowWord();
					}
					else {
						counter--;
						ShowDefinition();

					}
				}
			});
			previousAnimation.start();
		}
	}



	private void NextCard() {
		if ((counter + 1) >= myCards.size()) {
			Toast.makeText(ViewCard.this, R.string.out_of_cards, Toast.LENGTH_SHORT).show();
		}
		else {
			ObjectAnimator nextAnimation = ObjectAnimator.ofFloat(wordView, "translationX", 0, -width).setDuration(slideSpeed);
			nextAnimation.addListener(new AnimatorListener() {
				@Override 				
				public void onAnimationEnd(Animator animation) {
					ObjectAnimator.ofFloat(wordView, "translationX", width, 0).setDuration(slideSpeed).start();
				}
				@Override
				public void onAnimationCancel(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationStart(Animator arg0) {
					if (myCards.get(counter).getWord().equals(wordView.getText().toString())) {
						counter++;
						ShowWord();
					}
					else {
						counter++;
						ShowDefinition();
					}
				}
			});
			nextAnimation.start();

		}
	}

	private void FlipCard() {
		ObjectAnimator flipAnimation = ObjectAnimator.ofFloat(wordView, "rotationY", 180, 360);
		flipAnimation.addListener(new AnimatorListener() {
			@Override 
			public void onAnimationEnd(Animator animation) {   
				if (myCards.get(counter).getWord().equals(wordView.getText().toString())) {
					ShowDefinition();
				}
				else {
					//wordView.setText(myCards.get(counter).getWord());
					ShowWord();
				}
			}
			@Override
			public void onAnimationCancel(Animator arg0) {
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationStart(Animator arg0) {
			}
		});
		flipAnimation.setDuration(flipSpeed).start();
	}

	protected void EditCard() {
		String cardWord = myCards.get(counter).getWord();
		String viewWord = wordView.getText().toString();
		if (cardWord.equals(viewWord)) {
			final EditText editWord = new EditText(this);
			editWord.setText(cardWord);
			/* create new AlertDialog.Builder */
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setCancelable(true);
			alertDialogBuilder.setMessage(res.getString(R.string.current_word) + "\n" + "\n" + myCards.get(counter).getWord() + "\n" + "\n" + res.getString(R.string.new_word));
			alertDialogBuilder.setTitle(R.string.edit_word);
			alertDialogBuilder.setView(editWord);
			alertDialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					//String newWord = editWord.getText().toString();
					myCards.get(counter).setWord(editWord.getText().toString());
					//ReviewCards.setWordArray(counter, newWord);
					ShowWord();
				}	
			});

			alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
			/* show dialog */
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			alertDialog.show();
		}
		else {
			final EditText editDefinition = new EditText(this);
			editDefinition.setText(myCards.get(counter).getDefinition());
			/* create new AlertDialog.Builder */
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setCancelable(true);
			//Html.fromHtml(R.string.current_definition) + "\n" + "\n" + myCards.get(counter).getDefinition() + "\n" + "\n" + res.getString(R.string.new_definition));
			alertDialogBuilder.setMessage(res.getString(R.string.current_definition) + "\n" + "\n" + myCards.get(counter).getDefinition() + "\n" + "\n" + res.getString(R.string.new_definition));
			alertDialogBuilder.setTitle(R.string.edit_word);
			alertDialogBuilder.setView(editDefinition);
			alertDialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					myCards.get(counter).setDefinition(editDefinition.getText().toString());
					ShowDefinition();
				}	
			});

			alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			});
			/* show dialog */
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			alertDialog.show();
		}
	}

}
