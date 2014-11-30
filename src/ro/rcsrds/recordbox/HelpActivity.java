package ro.rcsrds.recordbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.option_menu_recorder:
				Intent recorder = new Intent(HelpActivity.this,MainActivity.class);
				startActivity(recorder);
				break;
			case R.id.option_menu_list:
				Intent recordingList = new Intent(HelpActivity.this,RecordingListActivity.class);
				startActivity(recordingList);
				break;
			case R.id.option_menu_settings:
				Intent settings = new Intent(HelpActivity.this,SettingsActivity.class);
				startActivity(settings);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
