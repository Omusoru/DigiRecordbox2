package ro.rcsrds.recordbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.koofr.api.v2.DefaultClientFactory;
import net.koofr.api.v2.StorageApi;
import net.koofr.api.v2.StorageApiException;
import net.koofr.api.v2.resources.File;
import net.koofr.api.v2.resources.Mount;
import net.koofr.api.v2.transfer.upload.FileUploadData;
import net.koofr.api.v2.transfer.upload.UploadData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class FileManager {
	
	public static final String PREFS_NAME = "Authentication";
	private SharedPreferences preferences;
	private String localFilePath;
	private String username;
	private String password;
	private Mount mount;
	private String mounty;
	private StorageApi api;
	
	public FileManager(Context context){				
		preferences = context.getSharedPreferences(PREFS_NAME, 0);
		username = preferences.getString("username", "").toLowerCase();
		username = username.replace(".", "DOT");
		username = username.replace("@", "AT");
		localFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DigiRecordbox/"+username+"/";
		username = preferences.getString("username", "");
		password = preferences.getString("password", "");
	}
	
	public void connectToCloud() {
		
		try {
			api = DefaultClientFactory.create("storage.rcs-rds.ro",username, password);
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
		}
		try {
			mount = api.getMounts().get(0);
			mounty = api.getMounts().get(0).toString();
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
		}
		
	}
	
	public boolean upload(String file){

		file = localFilePath + file;
		file.replace("/", "\\");
		
		try {
			api.createFolder(mount.getId(), "/", "DigiRecordbox");
			return true;
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
		}
		
		try {
			UploadData data = new FileUploadData(file);  
			api.filesUpload(mount.getId(), "/DigiRecordbox/", data, new SimpleProgressListener());
			return true;
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
			return false;
		}

	}
	
	public boolean download(String file){
		try {
			api.filesDownload(mount.getId(), "/DigiRecordbox/"+file, localFilePath, new SimpleProgressListener());
			return true;
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
			return false;
		}
	}
	
	public boolean deleteCloud(String file){
		try {
			api.removePath(mount.getId(), "/DigiRecordbox/"+file);
			return true;
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
			return false;
		}
	}
	
	public List<File> getFileListCloud(){
		try {
			return api.listFiles(mount.getId(), "/DigiRecordbox/");
		} catch (StorageApiException e1) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e1.getMessage());
			List<File> files = null;
			return files;
		}
		
	}
	
	public boolean deleteLocal(String file){
		java.io.File localFile = new java.io.File(localFilePath+file);
		if(localFile.exists()){
			localFile.delete();
			return true;
		}
		else return false;
	}
	
	public String getFileLink(String file){		
		try {
			//Log.d("dltest","AJUNG AICI BAH");
			//Log.d("dltest",api.getDownloadURL(mount.getId(), "/DigiRecordBox/"+file));
			//api.get
			return api.getDownloadURL(mount.getId(), "/DigiRecordBox/"+file);
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
			return null;
		}
	}
	
	public ArrayList<String> getFileListLocal(){
		java.io.File localFileFolder = new java.io.File(localFilePath);
		
		ArrayList<String> myList = new ArrayList<String>();
		java.io.File list[] = localFileFolder.listFiles();
		
		for( int i=0; i< list.length; i++)
	    {
	            myList.add( list[i].getName() );
	    }		
		
		return myList;
	}
	
	public boolean rename(String originalName,String newName){
		
		java.io.File file = new java.io.File(localFilePath+originalName);
		if(file.exists()){
			java.io.File file2 = new java.io.File(localFilePath+newName);
			file.renameTo(file2);
			return true;
		} else {
			return false;
		}
	}
	
	public boolean renameCloud(String originalName,String newName){
		
		try {
			api.renamePath(mount.getId(),"/RecordBox/"+originalName, newName);
			return true;
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			return false;
		}		
	}
	
}
