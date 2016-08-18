package com.app.dijkstra;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.dijkstra.SQLHelper;


//CONVERT GRAPH FROM DB TO ARRAY
 
public class GraphToArray {
	
	// DB
	SQLHelper dbHelper;
	SQLiteDatabase db;
	protected Cursor cursor;
	
	// Array Graph
	String[][] graph = new String[100][100];
	
	public String[][] convertToArray(Context mainContext){
		
		dbHelper = new SQLHelper(mainContext);
		db = dbHelper.getReadableDatabase();
	
		// MOVE TO GRAPH OF DB GRAPH ARRAY
		cursor = db.rawQuery("SELECT * FROM graph order by simpul_awal,simpul_tujuan asc", null);
		cursor.moveToFirst();
		
		String temp_index_line = "";
		int index_column = 0;
		int jml_path = cursor.getCount();
		
		for(int i = 0; i < jml_path; i++){
			
			// path
			cursor.moveToPosition(i);
			
			// find index column
			int nodeStartDB = Integer.parseInt(cursor.getString(1)); 
			
			if(temp_index_line == ""){
				temp_index_line = String.valueOf(nodeStartDB);
			}else{
				
				if(Integer.parseInt(temp_index_line) != nodeStartDB){
					index_column = 0;
					temp_index_line = String.valueOf(nodeStartDB);
				}
			}
						
			//graph array
			String nodeDest_Weight = "";
			if(cursor.getString(2).equals("") && cursor.getString(3).equals("") && cursor.getString(4).equals("")){ 
				nodeDest_Weight = ";";
			}
			
			else{			
				
				// example output : 2->789.98
				nodeDest_Weight = cursor.getString(2).toString()+"->"+cursor.getString(4).toString();
			}
			
			graph[nodeStartDB][index_column] = nodeDest_Weight;
			index_column++;
		}
		
		
		return graph;
		
	}
	
}
