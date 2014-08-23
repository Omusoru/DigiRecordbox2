package ro.rcsrds.recordbox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
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
	private boolean buttonPlaying;
	private boolean playingStatus;
	
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
		tvDescriptionContent.setMovementMethod(new ScrollingMovementMethod());
		
		//Get recording information
		int id = getIntent().getExtras().getInt("id");
		DatabaseHelper db = new DatabaseHelper(this);		
		recording = new Recording();
		recording = db.getRecording(id);
		fm = new FileManager(MediaPlayerActivity.this);
		
		if(recording.isOnLocal()){
			//Log.d("dltest", "TEST INAINTE DE FM");
			filename = recording.getLocalFilename();
			online = false;
		}
		else{
		
			new Thread(new Runnable() {
				 public void run() {		    	
				    	if(isNetworkConnected()) {
				    		fm.connectToCloud();
				    		online = true;
				    		filename = fm.getFileLink(recording.getCloudFilename());				    		
				    	} else {
				    		//TODO toast
				    		Log.d("Connection","Not network connected");
				    		online = false;
				    		filename = "a";
				    		runOnUiThread(new Runnable() {
					            public void run() {
					            	Toast.makeText(getApplicationContext(), R.string.message_no_internet_checking, Toast.LENGTH_SHORT).show();					            	
					            }
					        });
				    	}
			   }
			}).start();			
			while(filename == null){
			}
			if(online==false){
				finish();
			}
		}
		tvNameContent.setText(recording.getName());
		tvDescriptionContent.setText(recording.getDescription());
	
		player = new MediaPlayer(this);
		player.startPlaying(filename,online);
		sbarPlayer.setMax(player.getPlayerStatus().getDuration());	
		tvTotalTime.setText(getTimeFormat(player.getPlayerStatus().getDuration()-200));
		
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
		        	//player.pausePlaying();
		        	//sbarPlayer.setProgress(sbarPlayer.getProgress());
		        	//player.startPlaying(filename,online);
		        	sbarPlayer.setProgress(sbarPlayer.getMax());
		        	player.setCurentPosition(sbarPlayer.getMax());
		        	player.pausePlaying();
		        	mHandler.removeCallbacks(this);
		        	mHandler.removeCallbacks(timer);
		        	
		        	btnPlay.setBackgroundResource(R.drawable.button_play_small);
					buttonPlaying = true;
					///finish();
		        	
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
		
		buttonPlaying = false;
		btnPlay.setBackgroundResource(R.drawable.button_pause_small);
		
		sbarPlayer.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

	        @Override
	        public void onStopTrackingTouch(SeekBar sbarPlayer) {	        		       		
	        		if(sbarPlayer.getProgress()==sbarPlayer.getMax()){
	        		//sbarPlayer.setProgress(sbarPlayer.getMax());
	        		player.setCurentPosition(sbarPlayer.getMax());
	        		mHandler.post(playing);
    	        	mHandler.post(timer);
	        		}
	        		else{
	        			sbarPlayer.setProgress(sbarPlayer.getProgress());
	                	tvCurentTime.setText(getTimeFormat(sbarPlayer.getProgress()));
	                	mHandler.post(playing);
	    	        	mHandler.post(timer);
	    	        	if(playingStatus==true)
		        		{
		        			player.resumePlayingAt(sbarPlayer.getProgress());
		                	player.setCurentPosition(sbarPlayer.getProgress());        			
		        		}
		        		else {
		        			player.setCurentPosition(sbarPlayer.getProgress());
		        			player.isPlaying=false;
		        		}
	        		}
	        		
	        		
	        }

	        @Override
	        public void onStartTrackingTouch(SeekBar sbarPlayer) {
	        	mHandler.removeCallbacks(playing);
	        	mHandler.removeCallbacks(timer);
	        	if(!buttonPlaying){
	        		playingStatus=true;
	        		//btnPlay.setBackgroundResource(R.drawable.button_pause_small);
	    			//buttonPlaying = false;
	        		player.pausePlaying();
	        	}
	        	else{ 
	        		playingStatus=false;	        		
	        	}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mediaplayer, menu);
		return true;		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.option_menu_list) {
			Intent mediaPlayer = new Intent(MediaPlayerActivity.this,RecordingListActivity.class);
			startActivity(mediaPlayer);
		} else if(item.getItemId()==R.id.option_menu_recorder) {
			Intent mediaPlayer = new Intent(MediaPlayerActivity.this,MainActivity.class);
			startActivity(mediaPlayer);
		} else if(item.getItemId()==R.id.option_menu_about) {
			Intent intent = new Intent(MediaPlayerActivity.this,AboutActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
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
				if(sbarPlayer.getProgress()==sbarPlayer.getMax()){
					player.resumePlayingAt(0);
					sbarPlayer.setProgress(0);
					mHandler.post(playing);
					mHandler.post(timer);
				}
				else {
					if(player.isPlaying){
						player.pausePlaying();
						mHandler.removeCallbacks(playing);
						mHandler.removeCallbacks(timer);
					}
					else{
						player.resumePlaying();
						mHandler.post(playing);
						mHandler.post(timer);
					}
					/*player.setCurentPosition(sbarPlayer.getProgress());					
					//player.startPlaying(filename,online);
					//player.resumePlaying();
					mHandler.removeCallbacks(playing);
					mHandler.removeCallbacks(timer);
					mHandler.post(playing);
					mHandler.post(timer);//*/
				}
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
		if(buttonPlaying) {
			btnPlay.setBackgroundResource(R.drawable.button_pause_small);
			buttonPlaying = false;
		} else if(!buttonPlaying) {
			btnPlay.setBackgroundResource(R.drawable.button_play_small);
			buttonPlaying = true;
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
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  return (cm.getActiveNetworkInfo() != null);
	 }

}
