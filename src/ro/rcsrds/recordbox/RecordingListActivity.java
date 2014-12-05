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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
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
	private Button btnMore;
	public static final String PREFS_NAME = "Authentication";
	private ProgressDialog dlgProgress;
	
	//globals
	private boolean fileExists;
	private String looping;
	ArrayList<String> onlineFiles;
	private String duration;
	private boolean allowPlay;
	private int currentRecordingPos;
	
	//tasks
	private DownloadFromCloudTask downloadTask;
	private UploadToCloudTask uploadTask;
	private SimpleProgressListener uploadListener;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recordinglist);
		
		fm = new FileManager(RecordingListActivity.this);
		
		// load the recordings in the list
		loadRecordings();
		adapter = new RecordingListAdapter(this, recordingList);
		
		list = (ListView) findViewById(R.id.list);		
		list.setAdapter(adapter);
		list.setOnItemClickListener(new ListOnClickListener());
		registerForContextMenu(list);
		list.setTextFilterEnabled(true);
		
		searchField = (EditText) findViewById(R.id.searchField);
		searchField.addTextChangedListener(new SearchTextWatcher());
		
		//more button
		btnMore = (Button) findViewById(R.id.btn_more);
		btnMore.setOnClickListener(new ButtonOnClickListener());
		btnMore.setVisibility(View.GONE);				
		if(android.os.Build.VERSION.SDK_INT >= 14) {
			if(!ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey()) {
				btnMore.setVisibility(View.VISIBLE);
			}
		}		
		
		// check for missing files
		new CheckFilesTask().execute(RecordingListActivity.this);
		
		//initialize globals
		allowPlay = true;
		onlineFiles = null;
}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.recordinglist, menu);
		return true;
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.option_menu_recorder:
				Intent recorder = new Intent(RecordingListActivity.this,MainActivity.class);
				startActivity(recorder);
				break;
			case R.id.option_menu_import_files:
				new ImportFilesTask().execute();
				break;
			case R.id.option_menu_check_files:
				new CheckFilesTask().execute(this);
				break;
			case R.id.option_menu_settings:
				Intent settings = new Intent(RecordingListActivity.this,SettingsActivity.class);
				startActivity(settings);
				break;
			case R.id.option_menu_help:
				Intent about = new Intent(RecordingListActivity.this,HelpActivity.class);
				startActivity(about);
				break; 
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		allowPlay = true; // prevents multiple play requests
		// refresh the list. usually happens when returning from edit activity
		recordingList = db.getAllRecordings();
		adapter = new RecordingListAdapter(RecordingListActivity.this, recordingList);
		list.setAdapter(adapter);
		super.onResume();
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
		}
		
		return super.onContextItemSelected(item);
	}
	
	private class SearchTextWatcher implements TextWatcher {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			recordingList=adapter.getRecList(s);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		
		@Override
		public void afterTextChanged(Editable s) {}
		
	}
	
	private class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (v.getId()==R.id.btn_more) {
				openOptionsMenu();				
			}			
		}
		
		
	}
	
	private class ListOnClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			if(allowPlay) { // prevents multiple play requests
				
				Recording recording = recordingList.get((int)id);
				// check for local file
				if(recording.isOnLocal()) {
					if(checkFile("local",recording.getLocalFilename())) {
						// if file exists start media player
						Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
						intent.putExtra("id", recording.getId());
						startActivity(intent);
						allowPlay = false;
					} else {
						//show message
						Toast.makeText(getApplicationContext(), R.string.message_not_on_local, Toast.LENGTH_SHORT).show();
						//update database
						updateEntry("local", false, position, "");
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
							allowPlay = false;
						} else {
							//show message
							Toast.makeText(getApplicationContext(), R.string.message_not_on_cloud, Toast.LENGTH_SHORT).show();
							//update database
							updateEntry("cloud", false, position, "");
							//refresh list
							adapter.notifyDataSetChanged();
						}
					} else {
						Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
					}
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
	
	final Handler timerHandler = new Handler();
	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			/*
			if(isNetworkConnected()) {
				Log.d("CONN","Connection is alive");
				timerHandler.postDelayed(this,100);
			} else {					
				Log.d("CONN","Connection is dead");
				//downloadTask.cancel(true);
				//if(!uploadListener.isCanceled()) {
					//uploadListener.cancel();
				//}
				//uploadTask.cancel(true);
				timerHandler.removeCallbacks(this);				
			}*/
			
			
			
		}
		
	};
	
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
					uploadTask = new UploadToCloudTask();
					uploadTask.execute(recordingList.get(position).getLocalFilename());
					currentRecordingPos = position;
				} else {
					Toast.makeText(getApplicationContext(), R.string.message_not_on_local, Toast.LENGTH_SHORT).show();
					// update recording database entry on_local to False
					updateEntry("local", false, position, "");
					adapter.notifyDataSetChanged();
				}		
			}
		}	
	}
	
	private class UploadToCloudTask extends AsyncTask<String, Boolean, Boolean> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_uploading));
			dlgProgress.setMessage(getResources().getString(R.string.message_uploading));
			dlgProgress.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			fm.connectToCloud();
			fm.createFolderCloud("DigiRecordBox");
			uploadListener = new SimpleProgressListener();
			boolean succeded = fm.upload(params[0], uploadListener);
			return succeded; 
		}
		
		@Override
		protected void onPostExecute(Boolean succeded) {
			dlgProgress.dismiss();
			if(succeded) {
				updateEntry("cloud", true, currentRecordingPos, recordingList.get(currentRecordingPos).getLocalFilename());
				adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), R.string.message_upload_success, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.message_upload_fail, Toast.LENGTH_LONG).show();
			}
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
    				currentRecordingPos = position;
    				//new DownloadFromCloudTask().execute(recordingList.get(position).getCloudFilename());
    				downloadTask = new DownloadFromCloudTask();
    				downloadTask.execute(recordingList.get(position).getCloudFilename());
    			} else {
    				Toast.makeText(getApplicationContext(), R.string.message_not_on_cloud, Toast.LENGTH_SHORT).show();
    				// update recording database entry on_cloud to False
    				updateEntry("cloud", false, position, "");
    				adapter.notifyDataSetChanged();
    			}
    		}
    	}		
	}
	
	private class DownloadFromCloudTask extends AsyncTask<String, Void, Boolean> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_downloading)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_downloading));
			dlgProgress.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {				
			fm.connectToCloud();
			fm.createFolderCloud("DigiRecordBox");			
			boolean succeded = fm.download(params[0], new SimpleProgressListener());
			return succeded; 		
		}
		
		@Override
		protected void onPostExecute(Boolean succeded) {
			dlgProgress.dismiss();
			if(succeded) {
				updateEntry("local",true,currentRecordingPos, recordingList.get(currentRecordingPos).getCloudFilename());
				adapter.notifyDataSetChanged();	
				Toast.makeText(getApplicationContext(), R.string.message_download_success, Toast.LENGTH_LONG).show();				
			} else {
				Toast.makeText(getApplicationContext(), R.string.message_download_fail, Toast.LENGTH_LONG).show();
			}
		}
		
		@Override
		protected void onCancelled() {
			Toast.makeText(getApplicationContext(), "Download Canceled", Toast.LENGTH_LONG).show();
			super.onCancelled();
		}
		
	}
	
	private void deleteFromLocal(final int position) {		
		if(!recordingList.get(position).isOnLocal()) {
			Toast.makeText(getApplicationContext(), R.string.message_not_on_local, Toast.LENGTH_SHORT).show();
		} else {
			// delete local file
			currentRecordingPos = position;
			new DeleteFromLocalTask().execute(recordingList.get(position).getLocalFilename());
		}
	}
	
	private class DeleteFromLocalTask extends AsyncTask<String, Boolean, Boolean> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_deleting)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_deleting));
			dlgProgress.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return fm.deleteLocal(params[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean succeded) {
			dlgProgress.dismiss();
			if(succeded) {
				updateEntry("local",false,currentRecordingPos, "");
				adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), R.string.message_deletion_success, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.message_deletion_fail, Toast.LENGTH_LONG).show();
			}
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
    			currentRecordingPos = position;
    			new DeleteFromCloudTask().execute(recordingList.get(position).getLocalFilename());
    		}
    	}
	}
	
	private class DeleteFromCloudTask extends AsyncTask<String, Boolean, Boolean> {
		
		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_deleting)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_deleting));
			dlgProgress.show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			fm.connectToCloud();
			fm.createFolderCloud("DigiRecordBox");
			return fm.deleteCloud(params[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean succeded) {
			dlgProgress.dismiss();
			if(succeded) {
    			updateEntry("cloud",false,currentRecordingPos, "");
    			adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), R.string.message_deletion_success, Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), R.string.message_deletion_fail, Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	private class ImportFilesTask extends AsyncTask<Void, Void, Integer> {

		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_importing)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_importing));
			dlgProgress.show();
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			int count = 0;
			count += importLocalFiles();
			if(isNetworkConnected()) {
				count += importCloudFiles();
			}
			return count;
		}
		
		@Override
		protected void onPostExecute(Integer count) {
			dlgProgress.dismiss();
			if(count == 0) {
				Toast.makeText(RecordingListActivity.this, "All audio files are already in the database", Toast.LENGTH_SHORT).show();
			} else if (count == 1) {
				Toast.makeText(RecordingListActivity.this, "Added one audio file to the database", Toast.LENGTH_SHORT).show();
				adapter.notifyDataSetChanged();
			} else if (count > 1) {
				Toast.makeText(RecordingListActivity.this, "Added "+count+" audio files to the database", Toast.LENGTH_SHORT).show();
				adapter.notifyDataSetChanged();
			}
		}
		
	}
	
	private int importCloudFiles() {
		fm.connectToCloud();
		fm.createFolderCloud("DigiRecordBox");
    	
		onlineFiles = fm.getFileListCloud();
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
					int lastId = db.insertRecording(newRecording);
					newRecording.setId(lastId);
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
					int lastId = db.insertRecording(newRecording);
					newRecording.setId(lastId);
					recordingList.add(newRecording);
					count ++;
				}
			}
			
		}
		
		return count;
		
	}
	
	private void updateEntry(String location, boolean status, int position, String newName) {
		
		Recording recording = new Recording();
		recording = recordingList.get(position);
		if(location.equalsIgnoreCase("cloud")) {
			recording.setOnCloud(status);	
			recording.setCloudFilename(newName);
		} else if(location.equalsIgnoreCase("local")) {
			recording.setOnLocal(status);
			recording.setLocalFilename(newName);
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
	
	private class CheckFilesTask extends AsyncTask<Context, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			dlgProgress = new ProgressDialog(RecordingListActivity.this,ProgressDialog.STYLE_SPINNER);
			dlgProgress.setTitle(getResources().getString(R.string.title_checking_files)); 
			dlgProgress.setMessage(getResources().getString(R.string.message_checking_files));
			dlgProgress.show();
		}
		
		@Override
		protected Boolean doInBackground(Context... contexts) {
			boolean filesHaveChanged = false;
			DatabaseHelper db = new DatabaseHelper(contexts[0]);
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
				fm.connectToCloud();
				fm.createFolderCloud("DigiRecordBox");
				onlineFiles=fm.getFileListCloud();
				
				for(int i=0;i<recordingList.size();i++){
					if((!onlineFiles.contains(recordingList.get(i).getCloudFilename()))&&(recordingList.get(i).isOnCloud())){
						recordingList.get(i).setOnCloud(false);
						db.updateRecording(recordingList.get(i));
						filesHaveChanged = true;
					}
				}
			}	
			
			for(int i=0;i<recordingList.size();i++){
				if((recordingList.get(i).isOnCloud()==false)&&(recordingList.get(i).isOnLocal()==false)) {
					db.deleteRecording(recordingList.get(i));
					recordingList.remove(i);
					i--;
				}
			}
			
			return filesHaveChanged;
		}
		
		@Override
		protected void onPostExecute(Boolean filesHaveChanged) {
			dlgProgress.dismiss();
			if(filesHaveChanged) {
				adapter.notifyDataSetChanged();
			}
		}
		
		
	}
	
}
