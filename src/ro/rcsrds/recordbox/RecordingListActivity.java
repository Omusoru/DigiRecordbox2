package ro.rcsrds.recordbox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.EditText;

public class RecordingListActivity extends Activity {	
	
	private List<Recording> recordingList;
	private ListView list;
	private RecordingListAdapter adapter;
	private FileManager fm;
	private DatabaseHelper db;
	private EditText searchField;
	public static final String PREFS_NAME = "Authentication";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recordinglist);
		
		
		fm = new FileManager(RecordingListActivity.this);
		new Thread(new Runnable() {
			 public void run() {		    	
			    	if(isNetworkConnected()) {
			    		fm.connectToCloud();
			    	} else {
			    		//TODO toast
			    		Log.d("Connection","Not network connected");
			    		runOnUiThread(new Runnable() {
				            public void run() {
				            	Toast.makeText(getApplicationContext(), R.string.message_no_internet, Toast.LENGTH_SHORT).show();
				            }
				        });
			    	}
		   }
		}).start();
		
		
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recordinglist, menu);
		return true;
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.option_menu_recorder) {
			Intent mediaPlayer = new Intent(RecordingListActivity.this,MainActivity.class);
			startActivity(mediaPlayer);
		} else if(item.getItemId()==R.id.option_menu_import_local) {
			importLocalFiles();
		}
		return super.onOptionsItemSelected(item);
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
		String[] menuItems = getResources().getStringArray(R.array.context_recordinglist);
		//String menuItemName = menuItems[menuItemIndex];
		//String listItemName = recordingList.get(info.position).getName();
	
		switch(menuItemIndex) {
		case 0: // Edit recording
			Log.d("Recordinglist","Edit recording");
			Intent intent = new Intent(RecordingListActivity.this, EditRecordingActivity.class);
			intent.putExtra("id", recordingList.get(info.position).getId());
			intent.putExtra("new",false);
			startActivity(intent);
			break;
			
		case 1: // Upload to cloud
			Log.d("Recordinglist","Upload to cloud");			
			uploadToCloud(info.position);
			restartActivity();
			break;			
		case 2: // Download from cloud
			Log.d("Recordinglist","Download from cloud");
			downloadFromCloud(info.position);
			restartActivity();
			break;
		case 3: // Delete local file
			Log.d("Recordinglist","Delete local file");
			deleteFromLocal(info.position);
			restartActivity();
			break;
		case 4: // Delete cloud file
			Log.d("Recordinglist","Delete cloud file");
			deleteFromCloud(info.position);
			restartActivity();
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
			Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
			intent.putExtra("id", recordingList.get((int)id).getId());
			startActivity(intent);
		}
		
			
	}
	
	private void restartActivity() {
		finish();		
		Intent intent = new Intent(RecordingListActivity.this,RecordingListActivity.class);
		startActivity(intent);
	}
	
	private void importLocalFiles() {
		
		ArrayList<String> filenames = fm.getFileListLocal();
		DatabaseHelper db = new DatabaseHelper(this);
		List<Recording> recordingList = db.getAllRecordings();
		boolean fileInDb = false;
		int count = 0;
		
		for(String filename : filenames) {
			if(isValidFileType(filename)) {
				fileInDb = false;
				for(Recording recording : recordingList) {
					if(filename.equalsIgnoreCase(recording.getFilename())){
						fileInDb = true;
						break;
					}
				}
				if(!fileInDb) {
					Recording newRecording = new Recording();
					newRecording.setName("Untitled");
					newRecording.setDescription("");
					newRecording.setDate(getCurrentFormatedDate());
					newRecording.setOwner(getSharedPreferences(PREFS_NAME, 0).getString("username", ""));
					newRecording.setFilename(filename);
					newRecording.setDuration(getDuration(filename));
					newRecording.setOnLocal(true);
					newRecording.setOnCloud(false);
					db.insertRecording(newRecording);
					count ++;
				}
			}
			
		}
		
		if(count == 0) {
			Toast.makeText(this, "All audio files are already in the database", Toast.LENGTH_SHORT).show();
		} else if (count == 1) {
			Toast.makeText(this, "Added one audio file to the database", Toast.LENGTH_SHORT).show();
			restartActivity();
		} else if (count > 1) {
			Toast.makeText(this, "Added one "+count+" audio files to the database", Toast.LENGTH_SHORT).show();
			restartActivity();
		}
		
	}
	
	private void uploadToCloud(final int position) {
		// upload file
		new Thread(new Runnable() {
		    public void run() {
		    	fm.upload(recordingList.get(position).getFilename());
		   }
		}).start();
		
		// update recording database entry on_cloud to True
		updateEntry("cloud", true, position);
		restartActivity();
	}
	
	private void downloadFromCloud(final int position) {
		
		// download file
		new Thread(new Runnable() {
		    public void run() {
		    	fm.download(recordingList.get(position).getFilename());
		    }
		}).start();
		
		// update recording database entry on_local to True
		updateEntry("local",true,position);
		restartActivity();
		
	}
	
	private void deleteFromCloud(final int position) {
		// delete cloud file
		new Thread(new Runnable() {
		    public void run() {
		    	fm.deleteCloud(recordingList.get(position).getFilename());
		    }
		}).start();
		
		// update recording database entry on_cloud to False
		updateEntry("cloud",false,position);
		restartActivity();
	}
	
	private void deleteFromLocal(final int position) {
		// delete local file
		new Thread(new Runnable() {
		    public void run() {
		    	fm.deleteLocal(recordingList.get(position).getFilename());
		    }
		}).start();
		
		// update recording database entry on_local to False
		updateEntry("local",false,position);
		restartActivity();
	}
	
	private void updateEntry(String location, boolean status, int position) {
		
		Recording recording = new Recording();
		recording = recordingList.get(position);
		if(location.equals("cloud")) {
			recording.setOnCloud(status);			
		} else if(location.equals("local")) {
			recording.setOnLocal(status);
		}
		if((recording.isOnCloud()==false)&&(recording.isOnLocal()==false)) {
			db.deleteRecording(recording);
		} else {
			db.updateRecording(recording);
		}		
		recording = null;
		
	}
	
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
	
	private String getDuration(String filename) {
		MediaPlayer player = new MediaPlayer(this);	
		return getTimeFormat(player.getDuration(filename));
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
}
