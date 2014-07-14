package ro.rcsrds.recordbox;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class FileManager {
	
	public static final String PREFS_NAME = "Authentication";
	private static SharedPreferences preferences;
	
	public FileManager(Context context){
		preferences=context.getSharedPreferences(PREFS_NAME, 0);
	}
	
	public boolean upload(String file){
		
		 DigiFTPClient ftp=new DigiFTPClient("storage.rcs-rds.ro",21,preferences.getString("username", ""),preferences.getString("password", ""));
		 try {
				ftp.connect();
				ftp.changeWorkingDir("Digi Cloud");
				if(!ftp.dirExists("DigiRecordbox")){
					ftp.makeDir("DigiRecordbox");
				}
				ftp.changeWorkingDir("DigiRecordbox");
				ftp.putFile(file,".");
				ftp.disconnect();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
	}
	
	public boolean download(String file){
		String username = preferences.getString("username", "").toLowerCase();
		username = username.replace(".", "DOT");
		username = username.replace("@", "AT");
		String filePath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/DigiRecordbox/"+username;
		filePath=filePath.replace("/", "\\");
		
		DigiFTPClient ftp=new DigiFTPClient("storage.rcs-rds.ro",21,preferences.getString("username", ""),preferences.getString("password", ""));
		 try {
				ftp.connect();
				ftp.changeWorkingDir("Digi Cloud");
				if(!ftp.dirExists("DigiRecordbox")){
					return false;
				}
				ftp.changeWorkingDir("DigiRecordbox");
				if(ftp.fileExists(file)){
					ftp.getFile(ftp.getWorkingDirectory()+"/"+file, filePath);
					ftp.disconnect();
					return true;
				}
				else return false;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
	}
	
	public boolean delete(String file){
		DigiFTPClient ftp=new DigiFTPClient("storage.rcs-rds.ro",21,preferences.getString("username", ""),preferences.getString("password", ""));
		 try {
				ftp.connect();
				ftp.changeWorkingDir("Digi Cloud");
				if(!ftp.dirExists("DigiRecordbox")){
					return false;
				}
				ftp.changeWorkingDir("DigiRecordbox");
				if(ftp.fileExists(file))
				{
				ftp.deleteFile(file);
				ftp.disconnect();
				return true;
				}
				else return false;
		 } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
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
