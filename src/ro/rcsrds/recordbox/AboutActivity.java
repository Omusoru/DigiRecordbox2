package ro.rcsrds.recordbox;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;

public class AboutActivity extends Activity {
	
	private Button btnMore;
	private Button btnTos;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		btnTos = (Button) findViewById(R.id.btn_tos);
		btnTos.setOnClickListener(new ButtonClickListener());
		
		//more button
		btnMore = (Button) findViewById(R.id.btn_more);
		btnMore.setOnClickListener(new ButtonClickListener());
		btnMore.setVisibility(View.GONE);				
		if(android.os.Build.VERSION.SDK_INT >= 14) {
			if(!ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()) {
				btnMore.setVisibility(View.VISIBLE);
			}
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.option_menu_recorder:
				Intent recorder = new Intent(AboutActivity.this,MainActivity.class);
				startActivity(recorder);
				break;
			case R.id.option_menu_list:
				Intent recordingList = new Intent(AboutActivity.this,RecordingListActivity.class);
				startActivity(recordingList);
				break;
			case R.id.option_menu_settings:
				Intent settings = new Intent(AboutActivity.this,SettingsActivity.class);
				startActivity(settings);
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.btn_tos) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://storage.rcs-rds.ro/legal/tos"));
				startActivity(browserIntent);
			} else if (v.getId()==R.id.btn_more) {
				openOptionsMenu();
			}
		}
		
	}

}
