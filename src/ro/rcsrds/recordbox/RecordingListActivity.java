package ro.rcsrds.recordbox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class RecordingListActivity extends Activity {	
	
	private List<Recording> recordingList;
	private ListView list;
	private RecordingListAdapter adapter;
	private FileManager fm;
	private DatabaseHelper db;
	private EditText searchField;
	public static final String PREFS_NAME = "Authentication";
	private ProgressDialog dlgProgress;
	
	//globals
	private boolean fileExists;
	private String looping;
	ArrayList<String> onlineFiles = null;
	private String duration;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recordinglist);
		
		fm = new FileManager(RecordingListActivity.this);	
		
		final Handler handler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(isNetworkConnected()) {
					fm.connectToCloud();
		    		fm.createFolderCloud("DigiRecordbox");
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						checkFiles();
					}
				});
			}
		}).start();
		
		// load the recordings in the list
		loadRecordings();		
		list = (ListView) findViewById(R.id.list);
		searchField = (EditText) findViewById(R.id.searchField);
	    
		adapter = new RecordingListAdapter(this, recordingList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new ListOnClickListener());
		registerForContextMenu(list);
		list.setTextFilterEnabled(true);
	
		searchField.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence cs, int start, int before, int count) {
				recordingList=adapter.getRecList(cs);

			}			
			@Override
			public void beforeTextChanged(CharSequence cs, int start, int count,int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
   
}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.recordinglist, menu);
		return true;
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.option_menu_recorder) {
			Intent mediaPlayer = new Intent(RecordingListActivity.this,MainActivity.class);
			startActivity(mediaPlayer);
		} else if(item.getItemId()==R.id.option_menu_import_files) {
			int count = 0;
			count += importLocalFiles();
			if(isNetworkConnected()) {
				count += importCloudFiles();
			} else {
				Toast.makeText(getApplicationContext(), R.string.message_cloud_not_imported, Toast.LENGTH_SHORT).show();
			}
			if(count == 0) {
				Toast.makeText(this, "All audio files are already in the database", Toast.LENGTH_SHORT).show();
			} else if (count == 1) {
				Toast.makeText(this, "Added one audio file to the database", Toast.LENGTH_SHORT).show();
				adapter.notifyDataSetChanged();
			} else if (count > 1) {
				Toast.makeText(this, "Added "+count+" audio files to the database", Toast.LENGTH_SHORT).show();
				adapter.notifyDataSetChanged();
			}
			
		} else if(item.getItemId()==R.id.option_menu_about) {
			Intent intent = new Intent(RecordingListActivity.this,AboutActivity.class);
			startActivity(intent);
		} else if(item.getItemId()==R.id.option_menu_check_files) {
			if(checkFiles()) {
				adapter.notifyDataSetChanged();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.list) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		    menu.setHeaderTitle(recordingList.get(info.position).getName());
		    String[] menuItems = getResources().getStringArray(R.array.context_recordinglist);
		    for (int i = 0; i<menuItems.length; i++) {
		      menu.add(Menu.NONE, i, i, menuItems[i]);
		    }
		  
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		int menuItemIndex = item.getItemId();
		//String[] menuItems = getResources().getStringArray(R.array.context_recordinglist);
		//String menuItemName = menuItems[menuItemIndex];
		//String listItemName = recordingList.get(info.position).getName();
	
		switch(menuItemIndex) {
		case 0: // Edit recording
			editRecording(info.position);
			break;			
		case 1: // Upload to cloud
			uploadToCloud(info.position);
			break;	
		case 2: // Download from cloud
			downloadFromCloud(info.position);
			break;
		case 3: // Delete local file
			deleteFromLocal(info.position);
			break;
		case 4: // Delete cloud file
			deleteFromCloud(info.position);
			break;
		default:
			Log.d("Recordinglist","Nothing");
		}
		
		return super.onContextItemSelected(item);
	}
	
	private class ListOnClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Recording recording = recordingList.get((int)id);
			// check for local file
			if(recording.isOnLocal()) {
				if(checkFile("local",recording.getLocalFilename())) {
					// if file exists start media player
					Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
					intent.putExtra("id", recording.getId());
					startActivity(intent);
				} else {
					//show message
					Toast.makeText(getApplicationContext(), R.string.message_not_on_local, Toast.LENGTH_SHORT).show();
					//update database
					updateEntry("local", false, position);
					//refresh list
					adapter.notifyDataSetChanged();
				}
				
			// check for cloud file
			} else if(recording.isOnCloud()) {
				if(isNetworkConnected()) {
					if(checkFile("cloud",recording.getCloudFilename())) {
						// if file exists start media player
						Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
						intent.putExtra("id", recording.getId());
						startActivity(intent);
					} else {
						//show message
						Toast.makeText(getApplicationContext(), R.string.message_not_on_cloud, Toast.LENGTH_SHORT).show();
						//update database
						updateEntry("cloud", false, position);
						//refresh list
						adapter.notifyDataSetChanged();
					}
				} else {
					Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
				}
			}
			
		}
	}
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  return (cm.getActiveNetworkInfo() != null);
	 }
  	   
	
	public void loadRecordings() {
		db = new DatabaseHelper(this);
		recordingList = new ArrayList<Recording>();
		recordingList = db.getAllRecordings();
	}
	
	private void editRecording(final int position) {
		Intent intent = new Intent(RecordingListActivity.this, EditRecordingActivity.class);
		intent.putExtra("id", recordingList.get(position).getId());
		intent.putExtra("new",false);
		startActivity(intent);
	}
	
	private void uploadToCloud(final int position) {
		if(!isNetworkConnected()) {
			Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
		} else {
			if(!recordingList.get(position).isOnLocal()) {
				Toast.makeText(getApplicationContext(), R.string.message_not_on_local, Toast.LENGTH_SHORT).show();
			} else if(recordingList.get(position).isOnCloud()) {
				Toast.makeText(getApplicationContext(), R.string.message_already_on_cloud, Toast.LENGTH_SHORT).show();
			} else {
				// upload file
				if(checkFile("local", recordingList.get(position).getLocalFilename())) {
					new UploadToCloudTask().execute(recordingList.get(position).getLocalFilename());
					// update recording database entry on_cloud to True
					updateEntry("cloud", true, position);
					adapter.notifyDataSetChanged();
				} else {
					Toast.makeText(getApplicationContext(), R.string.message_not_on_local, Toast.LENGTH_SHORT).show();
					// update recording database entry on_local to False
					updateEntry("local", false, position);
					adapter.notifyDataSetChanged();
				}		
			}
		}	
	}
	
	private class UploadToCloudTask extends AsyncTask<String, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_uploading));
			dlgProgress.setMessage(getResources().getString(R.string.message_uploading));
			dlgProgress.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			fm.upload(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			dlgProgress.dismiss();
		}
		
	}
	
	private void downloadFromCloud(final int position) {
		if(!isNetworkConnected()) {
			Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
    	} else {
    		if(!recordingList.get(position).isOnCloud()) {
    			Toast.makeText(getApplicationContext(), R.string.message_not_on_cloud, Toast.LENGTH_SHORT).show();
    		} else if(recordingList.get(position).isOnLocal()) {
    			Toast.makeText(getApplicationContext(), R.string.message_already_on_local, Toast.LENGTH_SHORT).show();
    		} else {
    			// download file
    			if(checkFile("cloud", recordingList.get(position).getCloudFilename())) {
    				new DownloadFromCloudTask().execute(recordingList.get(position).getCloudFilename());
    				// update recording database entry on_local to True
    				updateEntry("local",true,position);
    				adapter.notifyDataSetChanged();
    			} else {
    				Toast.makeText(getApplicationContext(), R.string.message_not_on_cloud, Toast.LENGTH_SHORT).show();
    				// update recording database entry on_cloud to False
    				updateEntry("cloud", false, position);
    				adapter.notifyDataSetChanged();
    			}
    		}
    	}		
	}
	
	private class DownloadFromCloudTask extends AsyncTask<String, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_downloading)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_downloading));
			dlgProgress.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			fm.download(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			dlgProgress.dismiss();
		}
		
	}
	
	private void deleteFromLocal(final int position) {		
		if(!recordingList.get(position).isOnLocal()) {
			Toast.makeText(getApplicationContext(), R.string.message_not_on_local, Toast.LENGTH_SHORT).show();
		} else {
			// delete local file
			new DeleteFromLocalTask().execute(recordingList.get(position).getLocalFilename());
			
			// update recording database entry on_local to False
			updateEntry("local",false,position);
			adapter.notifyDataSetChanged();
		}
	}
	
	private class DeleteFromLocalTask extends AsyncTask<String, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_deleting)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_deleting));
			dlgProgress.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			fm.deleteLocal(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			dlgProgress.dismiss();
		}
		
	}
	
	private void deleteFromCloud(final int position) {
		if(!isNetworkConnected()) {
			Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
    	} else {
    		if(!recordingList.get(position).isOnCloud()) {
    			Toast.makeText(getApplicationContext(), R.string.message_not_on_cloud, Toast.LENGTH_SHORT).show();
    		} else {
    			// delete cloud file
    			new DeleteFromCloudTask().execute(recordingList.get(position).getLocalFilename());
    			
    			// update recording database entry on_cloud to False
    			updateEntry("cloud",false,position);
    			adapter.notifyDataSetChanged();
    		}
    	}
	}
	
	private class DeleteFromCloudTask extends AsyncTask<String, Void, Void> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_deleting)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_deleting));
			dlgProgress.show();
		}

		@Override
		protected Void doInBackground(String... params) {
			fm.deleteCloud(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			dlgProgress.dismiss();
		}
		
	}
	
	private int importCloudFiles() {
		looping = null;
		new Thread(new Runnable() {
		    public void run() {
		    	onlineFiles = fm.getFileListCloud();
		    	looping = "";
		    }
		}).start();
		while(looping == null) {}
		looping = null;//
		DatabaseHelper db = new DatabaseHelper(this);
		boolean fileInDb = false;
		int count = 0;
		
		for(String filename : onlineFiles) {
			if(isValidFileType(filename)) {
				fileInDb = false;
				for(Recording recording : recordingList) {
					if(filename.equalsIgnoreCase(recording.getCloudFilename())){
						fileInDb = true;
						break;
					}
				}
				if(!fileInDb) {
					Recording newRecording = new Recording();
					newRecording.setName("Imported - " + filename.substring(0, filename.length()-4));
					newRecording.setDescription("");
					newRecording.setDate(getCurrentFormatedDate());
					newRecording.setOwner(getSharedPreferences(PREFS_NAME, 0).getString("username", ""));
					newRecording.setLocalFilename(filename);
					newRecording.setCloudFilename(filename);
					newRecording.setDuration(getDuration("cloud",filename));
					newRecording.setOnLocal(false);
					newRecording.setOnCloud(true);
					db.insertRecording(newRecording);
					recordingList.add(newRecording);
					count ++;
				}
			}
			
		}
		
		return count;
	}
	
	private int importLocalFiles() {
		
		ArrayList<String> filenames = fm.getFileListLocal();
		DatabaseHelper db = new DatabaseHelper(this);
		boolean fileInDb = false;
		int count = 0;
		
		for(String filename : filenames) {
			if(isValidFileType(filename)) {
				fileInDb = false;
				for(Recording recording : recordingList) {
					if(filename.equalsIgnoreCase(recording.getLocalFilename())){
						fileInDb = true;
						break;
					}
				}
				if(!fileInDb) {
					Recording newRecording = new Recording();
					newRecording.setName("Imported - " + filename.substring(0, filename.length()-4));
					newRecording.setDescription("");
					newRecording.setDate(getCurrentFormatedDate());
					newRecording.setOwner(getSharedPreferences(PREFS_NAME, 0).getString("username", ""));
					newRecording.setLocalFilename(filename);
					newRecording.setCloudFilename(filename);
					newRecording.setDuration(getDuration("local",filename));
					newRecording.setOnLocal(true);
					newRecording.setOnCloud(false);
					db.insertRecording(newRecording);
					recordingList.add(newRecording);
					count ++;
				}
			}
			
		}
		
		return count;
		
	}
	
	private void updateEntry(String location, boolean status, int position) {
		
		Recording recording = new Recording();
		recording = recordingList.get(position);
		if(location.equalsIgnoreCase("cloud")) {
			recording.setOnCloud(status);			
		} else if(location.equalsIgnoreCase("local")) {
			recording.setOnLocal(status);
		}
		if((recording.isOnCloud()==false)&&(recording.isOnLocal()==false)) {
			db.deleteRecording(recording);
			recordingList.remove(position);
		} else {
			db.updateRecording(recording);
		}		
		recording = null;
		
	}
	
	private boolean checkFile(String location,final String filename) {
		fileExists = false;
		looping = null;
		if(location.equalsIgnoreCase("cloud")) {
			new Thread(new Runnable() {
			    public void run() {
			    	fileExists =  fm.checkFileOnline(filename);	
			    	looping = "";
			    }
			}).start();
			while(looping == null) {}
			looping = null;
				    			
		} else if(location.equalsIgnoreCase("local")) {
			fileExists =  fm.checkFileLocal(filename);
		}
		return fileExists;
	}
	
	@SuppressLint("SimpleDateFormat")
	private String getCurrentFormatedDate() {
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return df.format(c.getTime());
		
	}
	
	private boolean isValidFileType(String filename) {
		
		filename = filename.substring(filename.length()-3, filename.length());
		if(filename.equalsIgnoreCase("mp4")) {
			return true;
		} else {
			return false;
		}
	}
	
	private String getDuration(String location,final String filename) {
		duration = "";
		looping = null;
		if(location.equalsIgnoreCase("local")) {
			duration = getTimeFormat(fm.getDurationLocal(filename));
		} else if(location.equalsIgnoreCase("cloud")) {
			new Thread(new Runnable() {
			    public void run() {
			    	duration = getTimeFormat(fm.getDurationCloud(filename));
			    	looping = "";
			    }
			}).start();
			while(looping == null) {}
			looping = null;//
			
		}
		return duration;
	}
	
	private String getTimeFormat(int timeinms){
		String totext=null;
		if((timeinms/1000)/60<10)
			totext="0"+Integer.toString((timeinms/1000)/60);
		else totext=Integer.toString((timeinms/1000)/60);
		if((timeinms/1000)%60<10)
			totext+=":0"+Integer.toString((timeinms/1000)%60);
		else totext+=":"+Integer.toString((timeinms/1000)%60);
		
		return totext;
	}
	
	private boolean checkFiles(){
		
		boolean filesHaveChanged = false;
		DatabaseHelper db = new DatabaseHelper(this);
		ArrayList<String> localFiles = fm.getFileListLocal();
		
		// Search local files
		for(int i=0;i<recordingList.size();i++){
			if((!localFiles.contains(recordingList.get(i).getLocalFilename()))&&(recordingList.get(i).isOnLocal())){
				recordingList.get(i).setOnLocal(false);
				db.updateRecording(recordingList.get(i));
				filesHaveChanged = true;
			}
		}
		
		// Search cloud files
		if(isNetworkConnected()){			
			looping=null;
			new Thread(new Runnable() {
				@Override
				public void run() {
					onlineFiles=fm.getFileListCloud();
					looping="";
				}
			}).start();
			while(looping==null){}
			looping=null;
			
			
			for(int i=0;i<recordingList.size();i++){
				if((!onlineFiles.contains(recordingList.get(i).getLocalFilename()))&&(recordingList.get(i).isOnCloud())){
					recordingList.get(i).setOnCloud(false);
					db.updateRecording(recordingList.get(i));
					filesHaveChanged = true;
				}
			}
			
		} else {
    		Toast.makeText(getApplicationContext(), R.string.message_no_internet_checking, Toast.LENGTH_SHORT).show();
		}
		
		for(int i=0;i<recordingList.size();i++){
			if((recordingList.get(i).isOnCloud()==false)&&(recordingList.get(i).isOnLocal()==false)) {
				db.deleteRecording(recordingList.get(i));
				i--;
				recordingList = db.getAllRecordings();
			}
		}
		
		return filesHaveChanged;
	}
}
