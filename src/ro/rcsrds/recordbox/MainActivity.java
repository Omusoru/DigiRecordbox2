package ro.rcsrds.recordbox;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	private Button btnRecord;
	private Button btnStop;
	private Button btnCancel;
	private AudioRecorder recorder;
	private Authentication auth;
	private Runnable Checker;
	private Handler mHandle = new Handler();
	private ProgressBar spinner;
	private TextView status;
	private String filename;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		auth = new Authentication(this);
		
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
		spinner =(ProgressBar) findViewById(R.id.spinner);
		spinner.setVisibility(View.INVISIBLE);
		status = (TextView) findViewById(R.id.Status);
		status.setText("Saving...");
		status.setVisibility(View.INVISIBLE);
		
		recorder = new AudioRecorder(auth.getUsername());
//		Checker = new Runnable() {
//			
//			@Override
//			public void run() {				
//				if(recorder.getMergeStatus()==false){
//					btnRecord.setVisibility(View.VISIBLE);
//					mHandle.removeCallbacks(this);
//					spinner.setVisibility(View.INVISIBLE);
//					status.setVisibility(View.INVISIBLE);					
//					 // Start EditRecording activity with filename parameter
//					startIntent(filename);
//				}
//				else mHandle.postDelayed(this, 100);
//				Log.d("MergeStatus", Boolean.toString(recorder.getMergeStatus()));
//				
//			}
//		};
		
		
	}
	
	private class StopRecordingTask extends AsyncTask<Object, Object, Object> {
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			btnStop.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
            btnRecord.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.VISIBLE);
            status.setVisibility(View.VISIBLE);
		}

		@Override
		protected Object doInBackground(Object... params) {
			// TODO Auto-generated method stub
			recorder.stopRecording();
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			btnRecord.setVisibility(View.VISIBLE);
			spinner.setVisibility(View.INVISIBLE);
			status.setVisibility(View.INVISIBLE);					
			 // Start EditRecording activity with filename parameter
			startIntent(filename);
			//super.onPostExecute(result);
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
//				btnStop.setVisibility(View.INVISIBLE);
//	            btnCancel.setVisibility(View.INVISIBLE);
//	            btnRecord.setVisibility(View.INVISIBLE);
//	            spinner.setVisibility(View.VISIBLE);
//	            status.setVisibility(View.VISIBLE);
//	            recorder.setMergeStatus(true);
//	            recorder.stopRecording();
//	            Checker.run();
				new StopRecordingTask().execute(true);
	            resetButton();	            
			} else if (v.getId()==R.id.btn_recorder_cancel) {
				recorder.cancelRecording();
				btnStop.setVisibility(View.INVISIBLE);
	            btnCancel.setVisibility(View.INVISIBLE);
	            resetButton();
			} else if (v.getId()==R.id.btn_recorder_start) {				
				recorder.startRecording();
				filename = recorder.getLastFilename();
				Log.d("Mediaplyer","filename after startRecording(): "+filename);
				btnStop.setVisibility(View.VISIBLE);
	            btnCancel.setVisibility(View.VISIBLE);	  
	            switchButtons();
			} 
			
		}		
		
	}
	
	private void switchButtons() {
		
		if(btnRecord.getText().toString().equalsIgnoreCase(this.getString(R.string.btn_recorder_start))) {
			btnRecord.setText(this.getString(R.string.btn_recorder_pause));
			btnRecord.setBackgroundResource(R.drawable.button_pause_big);
		} else if(btnRecord.getText().toString().equalsIgnoreCase(this.getString(R.string.btn_recorder_pause))) {
			btnRecord.setText(this.getString(R.string.btn_recorder_start));
			btnRecord.setBackgroundResource(R.drawable.button_record_big);
		}
		
	}
	
	private void resetButton() {
		btnRecord.setText(this.getString(R.string.btn_recorder_start));
		btnRecord.setBackgroundResource(R.drawable.button_record_big);
	}
	
	private void startIntent(String filename){
		Log.d("Mediaplyer","filename after startRecording(): "+filename);
        Intent intent = new Intent(MainActivity.this,EditRecordingActivity.class);
		intent.putExtra("filename", filename);
		intent.putExtra("new",true);
		startActivity(intent);
	}

}
