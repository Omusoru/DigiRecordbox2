package ro.rcsrds.recordbox;

import java.io.IOException;
import java.util.List;

import net.koofr.api.v2.DefaultClientFactory;
import net.koofr.api.v2.StorageApi;
import net.koofr.api.v2.StorageApiException;
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

	
	public FileManager(Context context){				
		preferences = context.getSharedPreferences(PREFS_NAME, 0);
		username = preferences.getString("username", "").toLowerCase();
		username = username.replace(".", "DOT");
		username = username.replace("@", "AT");
		localFilePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/DigiRecordbox/"+username+"/";
		username = preferences.getString("username", "");
		password = preferences.getString("password", "");
		/*try {
			api = DefaultClientFactory.create("storage.rcs-rds.ro",username, password);
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mount = api.getMounts().get(0);
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	public boolean upload(String file){

		file = localFilePath + file;
		file.replace("/", "\\");
		
		StorageApi api = null;
		Mount mount =null;
		try {
			api = DefaultClientFactory.create("storage.rcs-rds.ro",username, password);
		} catch (StorageApiException e1) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e1.getMessage());
			return false;
		}
		try {
			mount = api.getMounts().get(0);
		} catch (StorageApiException e1) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e1.getMessage());
			return false;
		}

		try {
			api.createFolder(mount.getId(), "/", "DigiRecordbox");
		} catch (StorageApiException e) {
			Log.d("FileManager",e.getMessage());
		}
		try {
			UploadData data = new FileUploadData(file);  
			api.filesUpload(mount.getId(), "/DigiRecordbox", data, new SimpleProgressListener());
			return true;
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
			return false;
		}

	}
	
	public boolean download(String file){
		String location = localFilePath.replace("/", "\\");
		StorageApi api = null;
		Mount mount =null;
		try {
			api = DefaultClientFactory.create("storage.rcs-rds.ro",username, password);
		} catch (StorageApiException e1) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e1.getMessage());
			return false;
		}
		try {
			mount = api.getMounts().get(0);
		} catch (StorageApiException e1) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e1.getMessage());
			return false;
		}
		try {
			api.filesDownload(mount.getId(), "/DigiRecordbox/"+file, location, new SimpleProgressListener());
			return true;
		} catch (StorageApiException e) {
			// TODO Auto-generated catch block
			Log.d("FileManager",e.getMessage());
			return false;
		}
	}
	
	public boolean delete(String file){
		//api.
		return false;
	}
	
	public String[] getFileList(){
		DigiFTPClient ftp=new DigiFTPClient("storage.rcs-rds.ro",21,preferences.getString("username", ""),preferences.getString("password", ""));
		String[] files;
		try {
			ftp.connect();
			ftp.changeWorkingDir("Digi Cloud");
			if(!ftp.dirExists("DigiRecordbox")){
				ftp.makeDir("DigiRecordbox");
			}
			ftp.changeWorkingDir("DigiRecordbox");
			files=ftp.getFileList(ftp.getWorkingDirectory());
			ftp.disconnect();
			return files;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return files=new String[]{};
		}
	}
	
}
