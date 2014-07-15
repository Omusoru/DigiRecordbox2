package ro.rcsrds.recordbox;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MediaPlayerActivity extends Activity {
	
	private Button btnPlay;
	private Button btnStop;
	private MediaPlayer player;
	private String filename = null;
	private SeekBar sbarPlayer;
	private Handler mHandler = new Handler();
	private Runnable playing;
	private Runnable timer;
	private TextView tvCurentTime;
	private TextView tvTotalTime;	
	private TextView tvNameContent;
	private TextView tvDescriptionContent;
	private boolean online;
	private FileManager fm;
	private Recording recording;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_mediaplayer);
		
		btnPlay = (Button) findViewById(R.id.btn_player_play);
		btnPlay.setOnClickListener(new ButtonOnClickListener());
		btnStop = (Button) findViewById(R.id.btn_player_stop);
		btnStop.setOnClickListener(new ButtonOnClickListener());
		sbarPlayer = (SeekBar) findViewById(R.id.sk_bar_player);
		tvCurentTime = (TextView) findViewById(R.id.curentTime);
		tvTotalTime = (TextView) findViewById(R.id.totalTime);
		tvNameContent = (TextView) findViewById(R.id.tv_name_content);
		tvDescriptionContent = (TextView) findViewById(R.id.tv_description_content);
		
		//Get recording information
		int id = getIntent().getExtras().getInt("id");
		DatabaseHelper db = new DatabaseHelper(this);		
		recording = new Recording();
		recording = db.getRecording(id);
		if(recording.isOnLocal()){
			//Log.d("dltest", "TEST INAINTE DE FM");
			filename = recording.getFilename();
			online = false;
		}
		else{
			//Log.d("dltest", "TEST INAINTE DE FM");
			
			new Thread(new Runnable() {
			      public void run() {			    	
			       fm = new FileManager(MediaPlayerActivity.this);
			       filename = fm.getFileLink(recording.getFilename());
			       //Log.d("dltest",filename);
			     }
			  }).start();
			//Log.d("dltest", recording.getFilename());
			//Log.d("dltest",fm.getFileLink(recording.getFilename()));
			//filename = fm.getFileLink(recording.getFilename());
			online = true;
			while(filename == null){			
			}
		}
		tvNameContent.setText(recording.getName());
		tvDescriptionContent.setText(recording.getDescription());
	
		player = new MediaPlayer(this);
		player.startPlaying(filename,online);
		sbarPlayer.setMax(player.getPlayerStatus().getDuration());	
		tvTotalTime.setText(getTimeFormat(player.getPlayerStatus().getDuration()));
		
		playing = new Runnable() {
			
			@Override
			public void run() {
				if(player.getPlayerStatus() != null){
		            int mCurrentPosition = player.getPlayerStatus().getCurrentPosition();
		            sbarPlayer.setProgress(mCurrentPosition);
		        }
		        mHandler.postDelayed(this, 100);
		        Log.d("Mediaplayer",Integer.toString(player.getPlayerStatus().getDuration()-player.getPlayerStatus().getCurrentPosition()));
		        if(player.getPlayerStatus().getDuration()-player.getPlayerStatus().getCurrentPosition()<=200)
		        {
		        	//player.stopPlaying();		        
		        	mHandler.removeCallbacks(this);
		        	mHandler.removeCallbacks(timer);
		        	sbarPlayer.setProgress(player.getPlayerStatus().getDuration());
					//finish();
		        }
			}			
		};
		
		timer = new Runnable() {
			
			@Override
			public void run() {
				tvCurentTime.setText(getTimeFormat(sbarPlayer.getProgress()));
				mHandler.postDelayed(this, 1000);
			}
			
		};
		
		playing.run();
		timer.run();
		
		btnPlay.setText("Pause");	
		btnPlay.setBackgroundResource(R.drawable.button_pause_small);
		
		sbarPlayer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

	        @Override
	        public void onStopTrackingTouch(SeekBar sbarPlayer) {
	        		player.pausePlaying();
	        		if(sbarPlayer.getProgress()==player.getPlayerStatus().getDuration()){
	        		sbarPlayer.setProgress(sbarPlayer.getProgress());
	        		}
	        		else{
	        			sbarPlayer.setProgress(sbarPlayer.getProgress());
	        		
                	player.resumePlayingAt(sbarPlayer.getProgress());
                	player.setCurentPosition(sbarPlayer.getProgress());
                	tvCurentTime.setText(getTimeFormat(sbarPlayer.getProgress()));
                	mHandler.post(playing);
    	        	mHandler.post(timer);
	        		}
	        }

	        @Override
	        public void onStartTrackingTouch(SeekBar sbarPlayer) {
	        	mHandler.removeCallbacks(playing);
	        	mHandler.removeCallbacks(timer);	        	
	        }

	        @Override
	        public void onProgressChanged(SeekBar sbarPlayer, int progress, boolean fromUser) {                
	            	if(player.getPlayerStatus() != null && fromUser){
	            		sbarPlayer.setProgress(progress);
	                    tvCurentTime.setText(getTimeFormat(progress));
	                    Log.d("SeekBar",Integer.toString(player.getPlayerStatus().getDuration()-progress));
	                }
	        }
	    });		
		
	}
	
	@Override
	protected void onStop() {		
		super.onStop();
		mHandler.removeCallbacks(playing);
    	mHandler.removeCallbacks(timer);
    	if(player.getPlayerStatus() != null){
    		player.stopPlaying();
    	}
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.btn_player_play) {
				player.startPlaying(filename,online);
				switchButtons();
			} else if(v.getId()==R.id.btn_player_stop) {
				player.stopPlaying();
				mHandler.removeCallbacks(playing);
				mHandler.removeCallbacks(timer);
				finish();	
			}
					
		}
		
	}
	
	public void switchButtons() {
		if(btnPlay.getText().toString().equalsIgnoreCase(this.getString(R.string.btn_player_play))) {
			btnPlay.setText(this.getString(R.string.btn_player_pause));
			btnPlay.setBackgroundResource(R.drawable.button_pause_small);
		} else if(btnPlay.getText().toString().equalsIgnoreCase(this.getString(R.string.btn_player_pause))) {
			btnPlay.setText(this.getString(R.string.btn_player_play));
			btnPlay.setBackgroundResource(R.drawable.button_play_small);
		}
	}
	
	public String getTimeFormat(int timeinms){
		String totext=null;
		if((timeinms/1000)/60<10)
			totext="0"+Integer.toString((timeinms/1000)/60);
		else totext=Integer.toString((timeinms/1000)/60);
		if((timeinms/1000)%60<10)
			totext+=":0"+Integer.toString((timeinms/1000)%60);
		else totext+=":"+Integer.toString((timeinms/1000)%60);
		
		return totext;
	}

}
