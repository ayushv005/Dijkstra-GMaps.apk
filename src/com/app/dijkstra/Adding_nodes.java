package com.app.dijkstra;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

public class Adding_nodes{
	
	protected Cursor cursor;
	SQLHelper dbHelper;

	String[][] modif_graph = new String[100][100];
	String node_oldStr = "";
	int node_new;
	
	String node_dest_Str = "";
	
	
	/*
	 * @function
	 * Insert the new node
	 * Eg node 5-4 , inserted into 5-6-4
	 * And the node 4-5 , inserted into 4-6-5
	 * @parameter
	 * Nodes0 : eg { " nodes " : " 5-4 " } then nodes_awal0 = 5
	 * Nodes1 : eg { " nodes " : " 5-4 " } then nodes_awal1 = 4
	 * index_ccordinate_json : index coordinate array in JSON
	 * Context: MainActivity.context
	 * Graph [ ] [ ] : array to accommodate the graph of DB
	 * Example output : graph [ 5 ] [ 0 ] = 4- > 439 281
	 * Graph [ 6 ] [ 0 ] = 1- > 216 281
	 * Increase_row_id : row id DB new reply
	 * @return
	 * node_old = nodes0 + " - " + nodes1
	 * node_new = initial node
	 * Graph [ ] [ ]
	 */
	public void doubleNode(int nodes0, int nodes1, int index_coordinates_json, Context context, String[][] graph, 
							int increase_row_id ) throws JSONException{
		
		// read DB
		SQLHelper dbHelper = new SQLHelper(context);
		SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
		
		//insert into DB
		SQLiteDatabase dbInsert = dbHelper.getWritableDatabase();
		
		String index_column_graph = "";

		// Find the column index nodes1 ( 4 ) of the graph [ row ] [ column index ]
		for(int l = 0; l < 100; l++){
			if(graph[nodes0][l] != null){
				
				String nodeStart = graph[nodes0][l]; // [5][0] = 4->721.666				
		
				// 4->721.666
				String [] explode = nodeStart.split("->");				
				node_dest_Str = explode[0]; // 4
				
				// if 4 == 4 (node1)
				if(node_dest_Str.trim().equals( String.valueOf(nodes1).trim()) ){
					
					// index column; example graph[line][column]
					index_column_graph = String.valueOf(l);
				}
							
			}else break;
						
		}// for
		
		// index of the graph [ row ] [ column ] to be edited
		int path = nodes0;
		int column = Integer.parseInt(index_column_graph);

		// get the coordinates of the vertices 5-4
		cursor = dbInsert.rawQuery("SELECT jalur FROM graph where simpul_awal = "+ nodes0 +" and simpul_tujuan = "+ nodes1, null);
		cursor.moveToFirst();
		cursor.moveToPosition(0);

		// get coordinates JSON
		String json_coordinates = cursor.getString(0).toString();		
		JSONObject jObject = new JSONObject(json_coordinates);		
		JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");

		// maximum search node , ( create a new node numbering )
		cursor = dbRead.rawQuery("SELECT max(simpul_awal), max(simpul_tujuan) FROM graph", null);
		cursor.moveToFirst();
		int max_node_db		= 0;
		int max_nodeStart_db 	= Integer.parseInt(cursor.getString(0).toString());			
		int max_nodeDest_db = Integer.parseInt(cursor.getString(1).toString());
		if(max_nodeStart_db >= max_nodeDest_db){
			max_node_db = max_nodeStart_db;
		}else{
			max_node_db = max_nodeDest_db;
		}
		
		// break coordinates of beg- > CENTRAL		
		int limit = index_coordinates_json;		
		Count_Weights ct = new Count_Weights();
		ct.Count_Weights(0, limit, jArrCoordinates); // 0, middle coordinates, jSON coordinates
		
		//replace array graph[5][0] = 6->888.6
		graph[path][column] = (max_node_db+1)+"->"+ct.weight;
		
		
		int start_loop = 0;
		// create and save a (new record) new json coordinates to DB
		createAndSave_NewJsonCoordinate(start_loop, limit, jArrCoordinates, increase_row_id, path, (max_node_db + 1), ct.weight,
										dbInsert, dbRead); // 501 : new record index

		
		// reset weight
		ct.weight = 0;

			
		// break coordinates of middle- > END
		int start_loop1 = index_coordinates_json;
		int limit1 = (jArrCoordinates.length() - 1); // - 1 coz array starts from 0
		ct.Count_Weights(index_coordinates_json, limit1, jArrCoordinates); // coordinates from mid to end
		
		
		// new array graph[6][0] = 4->777.4
		graph[(max_node_db+1)][0] = nodes1 + "->" + ct.weight; //define [ 0 ] because of new index in the graph 
		// create and save new json record to DB
		createAndSave_NewJsonCoordinate(start_loop1, limit1, jArrCoordinates, ++increase_row_id, (max_node_db + 1), nodes1, ct.weight,
										dbInsert, dbRead); // 502 : new record index
		
		
		// reset weight
		ct.weight = 0;


		// CALCULATE NODE ( 4-5 ) , NOT THE ORIGINAL ( 5-4 )
		
		String index_column_graph1 = "";
		String nodes_inside_column = "";

		// reversed, so nodes0 nodes1 ; example ( 5-4 ) finished ( 4-5 )
		int t_nodes0 = nodes1; // 4
		int t_nodes1 = nodes0; // 5
	
		// Find the index column of the graph [ 4 ] [ index column ]
		for(int l = 0; l < 100; l++){

			if(graph[t_nodes0][l] != null){

				// == Get the destination node , example : 5- > 9585,340
				String nodeStart = graph[t_nodes0][l];
				String [] explode1 = nodeStart.split("->");
				
				nodes_inside_column = explode1[0];
				
				if(nodes_inside_column.trim().equals( String.valueOf(t_nodes1)) ){
					index_column_graph1 = String.valueOf(l);
				}
				
			}else break;
		}
			
		
		// Index of the graph [ Line1 ] [ column1 ] to be edited
		int path1 = t_nodes0;
		int column1 = Integer.parseInt(index_column_graph1);

		// Retrieve the coordinates of the vertices 4-5
		cursor = dbRead.rawQuery("SELECT jalur FROM graph where simpul_awal = "+t_nodes0+" and simpul_tujuan = "+t_nodes1, null);
		cursor.moveToFirst();
		cursor.moveToPosition(0);

		// get coordinates JSON from DB
		String json1 = cursor.getString(0).toString();
		JSONObject jObject1 = new JSONObject(json1);
		JSONArray jArrCoordinates1 = jObject1.getJSONArray("coordinates");

		// break coordinates of start->MIDDLE
		int index_dobel_koordinat_json = ( (jArrCoordinates1.length()-1) - index_coordinates_json );
		ct.Count_Weights(0, index_dobel_koordinat_json, jArrCoordinates1);

		//replace array graph[4][0] = 6->777.4
		graph[path1][column1] = (max_node_db+1)+"->"+ct.weight;
		
		// create and save a new json record to DB
		int start_loop2 = 0;
		createAndSave_NewJsonCoordinate(start_loop2, index_dobel_koordinat_json, jArrCoordinates1, ++increase_row_id, path1, (max_node_db + 1), ct.weight,
										dbInsert, dbRead); // 503 : new record index
		
				
		// reset weight
		ct.weight = 0;

				
		// break coordinates of middle- > END
		int limit2 = (jArrCoordinates1.length() - 1); // 
		ct.Count_Weights(index_dobel_koordinat_json, limit2, jArrCoordinates1); // coordinates from mid to end
		
		//replace array graph[6][1] = 5->888.6
		graph[(max_node_db+1)][1] = t_nodes1+"->"+ct.weight; //define [ 1 ] because of new index in the graph [ ] [ ]
		
		createAndSave_NewJsonCoordinate(index_dobel_koordinat_json, limit2, jArrCoordinates1, ++increase_row_id, (max_node_db + 1), t_nodes1, ct.weight,
										dbInsert, dbRead); // 503 : new record index	
		
		
		
		// return
		node_oldStr = nodes0 + "-" + nodes1;
		node_new = (max_node_db + 1);
		modif_graph = graph;
	}

	/*
	 * @funtion
	 *  Insert the new node
	 *  example node 5-4, inserted into 5-6-4
	 * @parameter
	 *  nodes_start0 : eg {"nodes": "5-4"} then nodes_start0 = 5
	 *  nodes_start1 : eg {"nodes": "5-4"} then nodes_start1 = 4
	 *  index_ccordinate_json : index array coordinates in JSON
	 *  context : MainActivity.context
	 *  graph[][] : array to accommodate the graph of DB
	 *   			example output : graph[5][0] = 4->439.281
	 *   							 graph[6][0] = 1->216.281 
	 *  increase_row_id : new row id DB 
	 * @return
	 *  node_new : node dest
	 *  graph[][]
	 */	
	public void singleNode(int nodes0, int nodes1, int index_coordinates_json, 
								Context context, String[][] graph, int increase_row_id) throws JSONException{
	
		// read DB
		SQLHelper dbHelper = new SQLHelper(context);
		SQLiteDatabase dbRead = dbHelper.getReadableDatabase();
		
		//insert into DB
		SQLiteDatabase dbInsert = dbHelper.getWritableDatabase();
		
		// CALC ORIGNAL NODE (5-4)
		String index_column_graph = "";
		
		// search index nodes_akhir1 column ( 4 ) of the graph [ row ] [ column ]
		for(int l = 0; l < 100; l++){
			if(graph[nodes0][l] != null){

				String nodeStart = graph[nodes0][l]; // [5][0] = 4->721.666
				String [] explode = nodeStart.split("->");
				
				// 6->721.666
				String value_node_array = explode[0];
				
				// if 4 == 4 (node_akhir1)
				if( value_node_array.trim().equals(String.valueOf(nodes1).trim()) ){
					
					// COL index; example graph[LINE][COLUMN]
					index_column_graph = String.valueOf(l);
				}
				
			}else break;
		}

		
		// Index of the graph [ row ] [ column ] to be edited
		int path = nodes0;
		int column = Integer.parseInt(index_column_graph);

		
		// Retrieve the coordinates of node9 3-6
		cursor = dbRead.rawQuery("SELECT jalur FROM graph where simpul_awal = "+nodes0+" and simpul_tujuan = "+nodes1, null);
		cursor.moveToFirst();
		cursor.moveToPosition(0);
		
		// get coordinates JSON
		String json_coordinates = cursor.getString(0).toString();		
		JSONObject jObject = new JSONObject(json_coordinates);		
		JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");
		
		// Find the maximum node , ( create a new node numbering )
		cursor = dbRead.rawQuery("SELECT max(simpul_awal) FROM graph", null);
		cursor.moveToFirst();
		int max_node_db = Integer.parseInt(cursor.getString(0).toString());			
	
		
		// break coordinates of start-> MIDDLE	
		System.out.println("single awal->tengah");
		int limit = index_coordinates_json;	
		Count_Weights ct = new Count_Weights();
		ct.Count_Weights(0, limit, jArrCoordinates); 
		//replace array graph[5][0] = 6->888.6
		graph[path][column] = (max_node_db+1)+"->"+ct.weight;

		int start_loop = 0;		
		
		// Create and store (new record) json new coordinates to DB
		createAndSave_NewJsonCoordinate(start_loop, limit, jArrCoordinates, increase_row_id, path, (max_node_db + 1), ct.weight,
										dbInsert, dbRead); // 501 : new record index
		
		// reset weight
		ct.weight = 0;
		
		// Create and store (new record) json new coordinates to DB
		int start_loop1 = index_coordinates_json; 
		int limit1 = (jArrCoordinates.length() - 1);
		ct.Count_Weights(index_coordinates_json, limit1, jArrCoordinates);
		
		// new array graph[6][0] = 4->777.4
		graph[(max_node_db+1)][0] = nodes1 + "->" + ct.weight; 
		
		createAndSave_NewJsonCoordinate(start_loop1, limit1, jArrCoordinates, ++increase_row_id, (max_node_db + 1), nodes1, ct.weight,
										dbInsert, dbRead); // 502 : new record index

		// return
		node_oldStr = nodes0 + "-" + nodes1;
		node_new = (max_node_db + 1);
		modif_graph = graph;
	}
	
	/* @function
	* Create and save the new coordinates in the form of JSON to DB
	* @parameter
	* Start : start looping , eg 0
	* Limit: coordinate array index , eg i [ 7 ] then limit = 7
	* JArrCoordinates : Coordinates of DB in the form JSONArray
	* NEW_ID : id new record
	* // Rows: multidimensional arrays , eg i [ row ] [ column ]
	* // max_node_db : max no of records in table graph
	* new_weight : new weight to solve coordinate lines
	* DbInsert : insert into database
	* DbRead : read a db record
	* @return
	* No return
	*/
	public void createAndSave_NewJsonCoordinate(int start1, int limit, JSONArray jArrCoordinates,
													 int new_id, int field_node_start, int field_node_dest, double new_weight,
													SQLiteDatabase dbInsert, SQLiteDatabase dbRead) throws JSONException{
		
		// JSON for save new coordinate
		JSONObject json_path = new JSONObject();
		JSONArray new_root_coordinates = new JSONArray();
		
		// Looping from beginning coordinate to MID coordinatE
		// or
		// from MID to end coordinate
		// Then , move to new coordinate from old coordinate
		for(int ne = start1; ne <= limit; ne++){
			
			JSONArray latlng = jArrCoordinates.getJSONArray(ne);
			double new_lat = latlng.getDouble(0);
			double new_lng = latlng.getDouble(1);
			
			JSONArray new_list_coordinates = new JSONArray();
			new_list_coordinates.put(new_lat);
			new_list_coordinates.put(new_lng);
			
			// coordinates
			new_root_coordinates.put(new_list_coordinates);			
		}


		// nodes
		JSONArray nodes = new JSONArray();
		String join_Nodes = String.valueOf(field_node_start) + '-' + String.valueOf(field_node_dest);
		nodes.put(join_Nodes);
		
		// distance_metres
		JSONArray distance_metres = new JSONArray();
		distance_metres.put(new_weight);
		
		
		// create new JSON
		json_path.put("nodes", nodes);
		json_path.put("coordinates", new_root_coordinates);
		json_path.put("distance_metres", distance_metres);
		
		String jalur_baru = json_path.toString();
		System.out.println(jalur_baru);		
		
		// INSERT NEW NODES
		ContentValues newCon = new ContentValues();
		newCon.put("id", new_id);
		newCon.put("simpul_awal", field_node_start);	
		newCon.put("simpul_tujuan", field_node_dest);
		newCon.put("jalur", jalur_baru);	
		newCon.put("bobot", new_weight);	
		dbInsert.insert("graph", null, newCon);
		
	}
}