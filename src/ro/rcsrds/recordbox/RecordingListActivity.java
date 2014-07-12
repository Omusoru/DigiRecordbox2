package ro.rcsrds.recordbox;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RecordingListActivity extends ListActivity {	
	
	ArrayList<String> listItems=new ArrayList<String>();
	ArrayAdapter<String> adapter;
	List<Recording> recordingList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recordinglist);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);
	    setListAdapter(adapter);
	   
	    loadRecordings();
    	    
}
	
	public void loadRecordings() {
		
		DatabaseHelper db = new DatabaseHelper(this);
		recordingList = new ArrayList<Recording>();
		recordingList = db.getAllRecordings();
		
		for(Recording recording : recordingList) {
			listItems.add(recording.getName());
		}
		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Start MediaPlayerActivity with selected record's filename as parameter
		Intent intent = new Intent(RecordingListActivity.this,MediaPlayerActivity.class);
		intent.putExtra("filename", recordingList.get((int)id).getFilename());
		startActivity(intent);
	}
	
	

}