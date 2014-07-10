package ro.rcsrds.recordbox;

import java.io.File;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RecordingListActivity extends ListActivity {	
	
	ArrayList<String> listItems=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recordinglist);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
	    setListAdapter(adapter);
	   
	    loadRecordings();
    	    
}
	
	public void loadRecordings() {
		
		String path = Environment.getExternalStorageDirectory().toString()+"/DigiRecordbox";
		//Log.d("Test", "Path: " + path);
		File f = new File(path);        
		File file[] = f.listFiles();
		//Log.d("Test", "Size: "+ file.length);
		for (int i=0; i < file.length; i++)
		{
		    //Log.d("Test", "FileName:" + file[i].getName());
			listItems.add(file[i].getName());
		}
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Start MediaPlayerActivity with selected file as parameter
		Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
		intent.putExtra("fileName", listItems.get((int)id));
		startActivity(intent);
	}
	
	

}
