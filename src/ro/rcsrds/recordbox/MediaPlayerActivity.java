package ro.rcsrds.recordbox;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class MediaPlayerActivity extends Activity {
	
	Button btnPlay;
	Button btnStop;
	MediaPlayer player;
	String filename;
	SeekBar SeekBar;
	private Handler mHandler = new Handler();
	Runnable playing;
	Runnable timer;
	TextView curentTime;
	TextView totalTime;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_mediaplayer);
		
		btnPlay = (Button) findViewById(R.id.btn_player_play);
		btnPlay.setOnClickListener(new ButtonOnClickListener());
		btnStop = (Button) findViewById(R.id.btn_player_stop);
		btnStop.setOnClickListener(new ButtonOnClickListener());
		SeekBar = (SeekBar) findViewById(R.id.sk_bar_player);
		curentTime = (TextView) findViewById(R.id.curentTime);
		totalTime = (TextView) findViewById(R.id.totalTime);
		
		//TODO start playing this file:
		filename = getIntent().getExtras().getString("fileName");
		//Log.d("Mediaplayer",filename);		
		player = new MediaPlayer();
		//Play incepe automat
		//player.startPlaying(filename);
		
		
		
		player.startPlaying(filename);
		SeekBar.setMax(player.getPlayerStatus().getDuration());	
		totalTime.setText(getTimeFormat(player.getPlayerStatus().getDuration()));
		
		playing = new Runnable() {
			
			@Override
			public void run() {
				if(player.getPlayerStatus() != null){
		            int mCurrentPosition = player.getPlayerStatus().getCurrentPosition();
		            SeekBar.setProgress(mCurrentPosition);
		        }
		        mHandler.postDelayed(this, 100);
		        Log.d("Mediaplayer",Integer.toString(player.getPlayerStatus().getDuration()-player.getPlayerStatus().getCurrentPosition()));
		        if(player.getPlayerStatus().getDuration()-player.getPlayerStatus().getCurrentPosition()<=0)
		        {
		        	player.stopPlaying();
		        	mHandler.removeCallbacks(this);
		        	mHandler.removeCallbacks(timer);
					finish();
		        }
			}			
		};
		
		timer = new Runnable() {
			
			@Override
			public void run() {
				curentTime.setText(getTimeFormat(SeekBar.getProgress()));
				mHandler.postDelayed(this, 1000);
			}
			
		};
		
		playing.run();
		timer.run();
		
		btnPlay.setText("Pause");		
		
		SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

	        @Override
	        public void onStopTrackingTouch(SeekBar seekBar) {	        	
	        	if(player.getPlayerStatus() != null){
                	player.getPlayerStatus().seekTo(SeekBar.getSecondaryProgress());
                	curentTime.setText(getTimeFormat(SeekBar.getSecondaryProgress()));
                }
	        	mHandler.post(playing);
	        	mHandler.post(timer);
	        }

	        @Override
	        public void onStartTrackingTouch(SeekBar seekBar) {
	        	mHandler.removeCallbacks(playing);
	        	mHandler.removeCallbacks(timer);
	        }

	            @Override
	        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {                
	            	if(player.getPlayerStatus() != null && fromUser){
	                    SeekBar.setSecondaryProgress(progress);
	                    curentTime.setText(getTimeFormat(progress));
	                }
	        }
	    });		
		
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.btn_player_play) {
				player.startPlaying(filename);
				if(btnPlay.getText().toString().equalsIgnoreCase("Play")) {
					btnPlay.setText("Pause");
				} else if(btnPlay.getText().toString().equalsIgnoreCase("Pause")) {
					btnPlay.setText("Play");
				}
			} else if(v.getId()==R.id.btn_player_stop) {
				player.stopPlaying();
				mHandler.removeCallbacks(playing);
				mHandler.removeCallbacks(timer);
				finish();	
			}
					
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
