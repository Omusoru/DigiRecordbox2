package ro.rcsrds.recordbox;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class AudioRecorder {
	private static final String LOG_TAG = "AudioRecordTest";
	
	private MediaRecorder Recorder = null;
	
	private String filePath;
	private String currentFile;	

	private boolean isRecording = false;
	private boolean canRecord = true;
	private boolean isMerging = false;
	
	ArrayList<String> tempAudio = new ArrayList<String>();
	private int timesPaused = 0;
	
	private Runnable merger;
	public Thread t = null;

	private RandomAccessFile randomAccessFile;
	
	
	public AudioRecorder(String username){
		username = username.toLowerCase();
		username = username.replace(".", "DOT");
		username = username.replace("@", "AT");
		filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/DigiRecordbox/"+username;
	}
	
	public void startRecording(){
		
		if(canRecord){
			
			File folderTest=new File(filePath);
			if(!folderTest.isDirectory()){
				folderTest.mkdirs();
			}
			
			folderTest = new File(filePath+"/Temp");
			if(folderTest.isDirectory()){
				deleteDirectory(folderTest);
			}
			folderTest.mkdirs();
			
			Recorder = new MediaRecorder();
			Recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			Recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			
			Calendar c = Calendar.getInstance();
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String currentDateandTime = df.format(c.getTime());
			currentFile=filePath+"/"+currentDateandTime+".mp4";
			
			tempAudio.add(filePath+"/Temp/temp"+"("+timesPaused+").mp4");
			
			Recorder.setOutputFile(tempAudio.get(0));
			Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			
			
	        try {
	            Recorder.prepare();
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "prepare() failed");
	        }
	        
	        Recorder.start();
	        canRecord=false;
	        isRecording=true;	        
		}
		//pause Recording
		else if(isRecording){
			Recorder.stop();
			Recorder.release();
			Recorder = null;
			
			timesPaused++;
			isRecording=false;
		}
		else if(!isRecording){
			Recorder = new MediaRecorder();
			Recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			Recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			
			tempAudio.add(filePath+"/Temp/temp"+"("+timesPaused+").mp4");
			
			Recorder.setOutputFile(tempAudio.get(timesPaused));
			Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

			
	        try {
	            Recorder.prepare();
	        } catch (IOException e) {
	            Log.e(LOG_TAG, "prepare() failed");
	        }
	        
	        Recorder.start();
	        isRecording=true;
		}
	}

	public void stopRecording(){
		if(Recorder!=null){
			Recorder.stop();
	        Recorder.release();
	        Recorder = null;
		}
        if(tempAudio.size()==1)
        {
        	renameFile(tempAudio.get(0),currentFile);
        	tempAudio=new ArrayList<String>();
        	deleteDirectory(new File(filePath+"/Temp"));
        	isMerging=false;
        	//Log.d("Test Tibi","intra aici ?");
        }
        else{
        	merger= new Runnable() {
					@Override
					public void run() {						
						try {
							mergeAudio(tempAudio);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						isMerging=false;
						deleteDirectory(new File(filePath+"/Temp"));
			        	tempAudio=new ArrayList<String>();
					}
				};
				
			if((t!=null))
			{
				t.stop();
				t=null;
			}
			
			t = new Thread(merger);
			t.start();
        }
        
        canRecord = true;
        isRecording = false;
        timesPaused = 0;
                
	}
	
	public void cancelRecording(){
		if(Recorder!=null){
			Recorder.stop();
	        Recorder.release();
	        Recorder = null;
		}
		deleteDirectory(new File(filePath+"/Temp"));
		tempAudio=new ArrayList<String>();
		canRecord = true;
        isRecording = false;
        timesPaused = 0;
	}
	
    private void deleteDirectory(File path){
    	if(path.exists()) {
    		File[] files = path.listFiles();
    	    	for(int i=0; i<files.length; i++) {
    	    		if(files[i].isDirectory()) {
    	    			deleteDirectory(files[i]);
    	    		}
    	    		else {
    	    			files[i].delete();
    	    		}
    	    	}
    	}
    	
    	path.delete();
    }    
	
	private void renameFile(String originalName,String newName)
	{
		File file = new File(originalName);
		File file2 = new File(newName);
		file.renameTo(file2);
	}
	
	public boolean getMergeStatus()
	{		
		return isMerging;
	}
	
	public void setMergeStatus(boolean status)
	{
		this.isMerging=status;
	}
	
	public String mergeAudio(ArrayList<String> files) throws IOException
	{
		String fileDestination=currentFile;


        Movie[] inMovies = new Movie[files.size()];
        
        for(int i=0; i<files.size();i++)
        {
        	inMovies[i] = MovieCreator.build(files.get(i));
        }

        //List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();

        for (Movie m : inMovies) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                /*if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }*/
            }
        }

        Movie result = new Movie();

        if (audioTracks.size() > 0) {
            result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
        }
        /*if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
        }*/

        Container out = new DefaultMp4Builder().build(result);

        randomAccessFile = new RandomAccessFile(String.format(fileDestination), "rw");
		FileChannel fc = randomAccessFile.getChannel();
        out.writeContainer(fc);
        fc.close();
        
	return fileDestination;

	}
	
	public String getLastFilename(){
        String filename = currentFile;
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DigiRecordbox";
        filename = filename.replace(path+"/", "");        
        
        return filename;
	}

}
