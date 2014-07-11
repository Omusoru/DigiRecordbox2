package ro.rcsrds.recordbox;

import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class MediaPlayer {
	private static final String LOG_TAG="Testing MediaPlayer";
	
	private android.media.MediaPlayer Player = null;	
	
	private String file = null;
	
	private int playingPausedAt;
	
	private boolean isPlaying = false;
	private boolean canPlay = true;
	
	public void startPlaying(String filename) {
		String filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/DigiRecordbox/"+filename;		
		if(canPlay){
	    	file=filePath;
	        Player = new android.media.MediaPlayer();
	        
	        try {
	            Player.setDataSource(file);
	            Player.prepare();
	            Player.start();
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "prepare() failed");
	        }        
	        isPlaying=true;
	        canPlay=false;
		}    	
    	else if(isPlaying){
    		Player.pause();
    		playingPausedAt = Player.getCurrentPosition();
    		isPlaying=false;
    	}
    	else if(!isPlaying){
    		Player.seekTo(playingPausedAt);
    		Player.start();    		
    		isPlaying=true;    	   
    	}
		
		/*while(Player.getDuration()-Player.getCurrentPosition()>0){
			while(Player.getDuration()-Player.getCurrentPosition()<=0)
			{
				stopPlaying();
			}
		}*/
	}
    
    public void stopPlaying() {    	
        Player.release();
        Player = null;
        canPlay=true;
        isPlaying=false;

    }
    
    public android.media.MediaPlayer getPlayerStatus()
    {
    	return Player;
    }
    
    public void setCurentPosition(int timeorposition){
    	playingPausedAt=timeorposition;
    }
    
}
