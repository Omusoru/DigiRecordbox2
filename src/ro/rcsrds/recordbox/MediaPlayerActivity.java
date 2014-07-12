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
	private String filename;
	private SeekBar sbarPlayer;
	private Handler mHandler = new Handler();
	private Runnable playing;
	private Runnable timer;
	private TextView tvCurentTime;
	private TextView tvTotalTime;	
	
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
		
		//TODO start playing this file:
		filename = getIntent().getExtras().getString("fileName");
		//Log.d("Mediaplayer",filename);		
		player = new MediaPlayer();
		//Play incepe automat
		//player.startPlaying(filename);
		
		
		
		player.startPlaying(filename);
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
				tvCurentTime.setText(getTimeFormat(sbarPlayer.getProgress()));
				mHandler.postDelayed(this, 1000);
			}
			
		};
		
		playing.run();
		timer.run();
		
		btnPlay.setText("Pause");		
		
		sbarPlayer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

	        @Override
	        public void onStopTrackingTouch(SeekBar sbarPlayer) {	        	
	        	if(player.getPlayerStatus() != null){
                	player.getPlayerStatus().seekTo(sbarPlayer.getSecondaryProgress());
                	player.setCurentPosition(sbarPlayer.getSecondaryProgress());
                	tvCurentTime.setText(getTimeFormat(sbarPlayer.getSecondaryProgress()));
                	
                }
	        	mHandler.post(playing);
	        	mHandler.post(timer);
	        }

	        @Override
	        public void onStartTrackingTouch(SeekBar sbarPlayer) {
	        	mHandler.removeCallbacks(playing);
	        	mHandler.removeCallbacks(timer);
	        }

	            @Override
	        public void onProgressChanged(SeekBar sbarPlayer, int progress, boolean fromUser) {                
	            	if(player.getPlayerStatus() != null && fromUser){
	            		sbarPlayer.setSecondaryProgress(progress);
	                    tvCurentTime.setText(getTimeFormat(progress));
	                }
	        }
	    });		
		
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.btn_player_play) {
				player.startPlaying(filename);
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
		} else if(btnPlay.getText().toString().equalsIgnoreCase(this.getString(R.string.btn_player_pause))) {
			btnPlay.setText(this.getString(R.string.btn_player_play));
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
