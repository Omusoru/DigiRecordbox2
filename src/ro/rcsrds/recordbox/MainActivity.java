package ro.rcsrds.recordbox;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
	private TextView tvRecorderTime;
	private AudioRecorder recorder;
	private Authentication auth;
//	private Runnable Checker;
//	private Handler mHandle = new Handler();
	private ProgressBar spinner;	
	private TextView status;
	private String filename;
	
	//Timer
	private long startTime = 0L;
	private Handler myHandler = new Handler();
	long timeInMillies = 0L;
	long timeSwap = 0L;
	long finalTime = 0L;
	private boolean paused = true;
	private String previousTime;
	
	
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
		tvRecorderTime = (TextView) findViewById(R.id.tv_recorder_time);
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
			super.onPreExecute();
			btnStop.setVisibility(View.INVISIBLE);
            btnCancel.setVisibility(View.INVISIBLE);
            btnRecord.setVisibility(View.INVISIBLE);
            spinner.setVisibility(View.VISIBLE);
            status.setVisibility(View.VISIBLE);
		}

		@Override
		protected Object doInBackground(Object... params) {
			recorder.stopRecording();
			return null;
		}
		
		@Override
		protected void onPostExecute(Object result) {
			btnRecord.setVisibility(View.VISIBLE);
			spinner.setVisibility(View.INVISIBLE);
			status.setVisibility(View.INVISIBLE);					
			 // Start EditRecording activity with filename parameter
			startIntent(filename);
			super.onPostExecute(result);
		}
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
				previousTime = tvRecorderTime.getText().toString();
				new StopRecordingTask().execute(true);
	            resetButton();	
	            stopTimer();
			} else if (v.getId()==R.id.btn_recorder_cancel) {
				recorder.cancelRecording();
				btnStop.setVisibility(View.INVISIBLE);
	            btnCancel.setVisibility(View.INVISIBLE);
	            resetButton();
	            stopTimer();
			} else if (v.getId()==R.id.btn_recorder_start) {	
				recorder.startRecording();
				filename = recorder.getLastFilename();
				Log.d("Mediaplyer","filename after startRecording(): "+filename);
				btnStop.setVisibility(View.VISIBLE);
	            btnCancel.setVisibility(View.VISIBLE);	  
	            switchButtons();
	            startTimer();
			} 
			
		}		
		
	}
	
	private void startTimer() {
		if(paused) {
        	startTime = SystemClock.uptimeMillis();
			myHandler.postDelayed(updateTimerMethod, 0);
			paused = false;
        } else {
        	timeSwap += timeInMillies;
			myHandler.removeCallbacks(updateTimerMethod);
			paused = true;
        }
	}
	
	private void stopTimer() {
		myHandler.removeCallbacks(updateTimerMethod);
		paused = true;
		tvRecorderTime.setText("00:00");
		startTime = 0L;
		timeInMillies = 0L;
		timeSwap = 0L;
		finalTime = 0L;
	}
	
	private Runnable updateTimerMethod = new Runnable() {

		public void run() {
			timeInMillies = SystemClock.uptimeMillis()-startTime;
			finalTime = timeSwap + timeInMillies;			
			tvRecorderTime.setText(getTimeFormat(finalTime));
			myHandler.postDelayed(this, 0);
		}
	
	};
	
	public String getTimeFormat(long timeinms){
		String totext=null;
		if((timeinms/1000)/60<10)
			totext="0"+Long.toString((timeinms/1000)/60);
		else totext=Long.toString((timeinms/1000)/60);
		if((timeinms/1000)%60<10)
			totext+=":0"+Long.toString((timeinms/1000)%60);
		else totext+=":"+Long.toString((timeinms/1000)%60);
		
		return totext;
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
		intent.putExtra("duration", previousTime);
		startActivity(intent);
	}

}
