package com.klin1344.cardify;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingsActivity extends Activity{
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

		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
		getActionBar().setDisplayHomeAsUpEnabled(true);
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
}
