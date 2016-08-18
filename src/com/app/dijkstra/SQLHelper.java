package com.app.dijkstra;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class SQLHelper extends SQLiteOpenHelper{

	private static final String DATABASE_NAME = "schoolnav.sqlite";
	private static final int DATABASE_VERSION = 1;
	private static String DB_PATH = "/data/data/com.app.dijkstra/databases/";
	private Context myContext;
	
	public SQLHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
		myContext=context;
	}
	
	public void createDataBase() throws IOException{
		if(DataBaseisExist()){
    		//do nothing - database already exists
		//	Toast.makeText(myContext, "", Toast.LENGTH_LONG).show();
    	}
		else{
        	this.getReadableDatabase();
 
        	try {
        		copyDataBase();
        		//Toast.makeText(myContext, "", Toast.LENGTH_LONG).show();
    		} catch (IOException e) {
    			throw new Error("Error copying database");
        	}
    	}
 
    }
	
	private boolean DataBaseisExist(){
    	SQLiteDatabase checkDB = null;
    	try{
    		String myPath = DB_PATH + DATABASE_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
    		//database does't exist yet.
    	}
    	if(checkDB != null){
    		checkDB.close();
    	}
    	if(checkDB != null )return true ;else return false;
    }
	
	private void copyDataBase() throws IOException{
    	
    	InputStream myInput = myContext.getAssets().open(DATABASE_NAME);
    	String outFileName = DB_PATH + DATABASE_NAME;
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
     	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
}