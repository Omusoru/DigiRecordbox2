package ro.rcsrds.recordbox;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {

	private Button btnRecord;
	private Button btnStop;
	private Button btnCancel;
	private AudioRecorder recorder;
	public static final String PREFS_NAME = "Authentication";
	private Authentication auth;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
		auth = new Authentication(preferences);
		
		//Check if user is logged in
		if(!auth.isLoggedIn()) {
			//Close main activity so user can't bypass login screen
			finish();
			//Start login activity
			Intent login = new Intent(MainActivity.this,LoginActivity.class);
			startActivity(login);			
		}		
		
		btnRecord = (Button) findViewById(R.id.btn_recorder_start);
		btnRecord.setOnClickListener(new ButtonClickListener());
		btnStop = (Button) findViewById(R.id.btn_recorder_stop);
		btnStop.setOnClickListener(new ButtonClickListener());
		btnStop.setVisibility(View.INVISIBLE);
		btnCancel = (Button) findViewById(R.id.btn_recorder_cancel);
		btnCancel.setOnClickListener(new ButtonClickListener());
		btnCancel.setVisibility(View.INVISIBLE);

		recorder = new AudioRecorder();
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if(item.getItemId()==R.id.option_menu_logout) {			
			auth.logOut();	
		    //Close main activity so user can't bypass login screen
			finish();
			//Start login activity
			Intent login = new Intent(MainActivity.this,LoginActivity.class);
			startActivity(login);	
		} else if(item.getItemId()==R.id.option_menu_list) {
			Intent mediaPlayer = new Intent(MainActivity.this,RecordingListActivity.class);
			startActivity(mediaPlayer);
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ButtonClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			if (v.getId()==R.id.btn_recorder_stop) {
				Log.d("Mediaplayer",recorder.stopRecording());
				btnStop.setVisibility(View.INVISIBLE);
	            btnCancel.setVisibility(View.INVISIBLE);
	            switchButtons();
			} else if (v.getId()==R.id.btn_recorder_cancel) {
				recorder.cancelRecording();
				btnStop.setVisibility(View.INVISIBLE);
	            btnCancel.setVisibility(View.INVISIBLE);
			} else if (v.getId()==R.id.btn_recorder_start) {
				switchButtons();
				recorder.startRecording();
				btnStop.setVisibility(View.VISIBLE);
	            btnCancel.setVisibility(View.VISIBLE);	  
			} 
			
		}		
		
	}
	
	private void switchButtons() {
		
		if(btnRecord.getText().toString().equalsIgnoreCase(this.getString(R.string.btn_recorder_start))) {
			btnRecord.setText(this.getString(R.string.btn_recorder_pause));
		} else if(btnRecord.getText().toString().equalsIgnoreCase(this.getString(R.string.btn_recorder_pause))) {
			btnRecord.setText(this.getString(R.string.btn_recorder_start));
		}
		
	}
}
