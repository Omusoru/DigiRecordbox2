package ro.rcsrds.recordbox;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class AboutActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.option_menu_list) {
			Intent mediaPlayer = new Intent(AboutActivity.this,RecordingListActivity.class);
			startActivity(mediaPlayer);
		} else if(item.getItemId()==R.id.option_menu_recorder) {
			Intent mediaPlayer = new Intent(AboutActivity.this,MainActivity.class);
			startActivity(mediaPlayer);
		}
		return super.onOptionsItemSelected(item);
	}
}
