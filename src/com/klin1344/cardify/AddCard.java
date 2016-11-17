package com.klin1344.cardify;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;



public class AddCard extends Activity implements OnClickListener {
	private EditText editWord;
	private EditText editDefinition;
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
		setContentView(R.layout.new_card_fields);

		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		
		final TextView inputWord = (TextView) findViewById(R.id.inputWord);
		final TextView inputDefinition = (TextView) findViewById(R.id.inputDefinition);

		editWord = (EditText) findViewById(R.id.editWord);
		editDefinition = (EditText) findViewById(R.id.editDefinition);
		
		editWord.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub	
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				inputWord.setText(editWord.getText().toString());
				inputDefinition.setText(editDefinition.getText().toString());
			}
		});
		editDefinition.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				inputDefinition.setText(editDefinition.getText().toString());
			}
		});
		Button saveButton = (Button) findViewById(R.id.saveButton);
		Button cancelButton = (Button) findViewById(R.id.cancelButton);

		saveButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(getIntent().getStringExtra("titleName") + " - " + getResources().getString(R.string.add_card));
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		AdView adView = (AdView) findViewById(R.id.adView);
	    AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.saveButton:
			SaveCard();
			break;
		case R.id.cancelButton:
			finish();
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void SaveCard() {
		String word = editWord.getText().toString();
		String definition = editDefinition.getText().toString();
		if (word.isEmpty() | definition.isEmpty()) {
			Toast.makeText(AddCard.this, R.string.card_is_empty, Toast.LENGTH_SHORT).show();
			finish();
		}
		else {
			Intent returnIntent = new Intent();
			returnIntent.putExtra("word", word);
			returnIntent.putExtra("definition", definition);
			setResult(RESULT_OK, returnIntent);     
			finish();
		}
	}
}