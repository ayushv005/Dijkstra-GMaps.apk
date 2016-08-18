package com.app.dijkstra;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.widget.Toast;

public class Get_coordinate_start_end extends Activity{

	Cursor cursor;	
	
	int fix_node_start = 0;
	String explode_lat_only = "";	
	Location posisiUser = new Location("");
	ArrayList<String> a_tmp_graph = new ArrayList<String>();
	
	JSONObject jadi_json = new JSONObject();
	
	List<String> lineDouble = new ArrayList<String>();
	List<String> indexLineList = new ArrayList<String>();
	
	/*
	 * @function
	 *  select the node to be used
	 *  if any node 1-0 and 0-1 then 1-0 ( because of the same coordinates with 0-1 1-0 (coordinates only reversed ) )
	 * @parameter
	 *   latx : latitude user or school
	 *   lngx : longitude user or destination
	 *   context : MainActivity context
	 * @return
	 *   JSON (index coordinates, nodes0, nodes1)
	 */	
	public JSONObject Get_simpul(double latx, double lngx, Context context) throws JSONException {
		// TODO Auto-generated constructor stub		
		
		// your coordinate
		posisiUser.setLatitude(latx);
		posisiUser.setLongitude(lngx);		

		//add to array
		List<String> lineDouble = new ArrayList<String>();
		List<String> indexLineList = new ArrayList<String>();
			
		
		SQLHelper dbHelper = new SQLHelper(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		// filter node to be used
		cursor = db.rawQuery("SELECT * FROM graph where simpul_awal != '' and simpul_tujuan != '' and jalur != '' and bobot != ''", null);
		cursor.moveToFirst();
		
		for (int i = 0; i < cursor.getCount(); i++){
			
			cursor.moveToPosition(i);
			
			// start_node
			String fieldStartNode = cursor.getString(1).toString();
			
			// end_node
			String fieldEndNode = cursor.getString(2).toString();
			
			String joininNodeStr = fieldStartNode+","+fieldEndNode;			
			String joininPrevNodeStr = fieldEndNode+","+fieldStartNode;

			// select one, for example : 1.0
			if(lineDouble.isEmpty()){
				
				lineDouble.add(joininPrevNodeStr);
				
				// id field in the table graph
				indexLineList.add(cursor.getString(0).toString());
			}else{
				
				if(!lineDouble.contains(joininNodeStr)){	
					lineDouble.add(joininPrevNodeStr);
					
					// id field in the table graph
					indexLineList.add(cursor.getString(0).toString());
				}
			}
		}
		
		
		// node list
		StringBuilder indexLineList1 = new StringBuilder();		
		for(int j = 0; j < indexLineList.size(); j++){
			
			if(indexLineList1.length() == 0){
				
				indexLineList1.append(indexLineList.get(j));
			}else{
				indexLineList1.append(","+indexLineList.get(j)); //where in ('0,1')
			}
		}


		// Query that lines are not double
		cursor = db.rawQuery("SELECT * FROM graph where id in("+indexLineList1+")",null);
		cursor.moveToFirst();

		JSONObject obj = new JSONObject();
	
		for(int k = 0; k < cursor.getCount(); k++){

			List<Double> UserDist_NodeCoordinates = new ArrayList<Double>();	
			cursor.moveToPosition(k);
	
			String json = cursor.getString(3).toString();

			// manipulating JSON
			JSONObject jObject = new JSONObject(json);
			JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");
			JSONArray jArrNodes = jObject.getJSONArray("nodes");
		
			// get coordinate
			for(int w = 0; w < jArrCoordinates.length(); w++){
				JSONArray latlngs = jArrCoordinates.getJSONArray(w);
				Double lats = latlngs.getDouble(0);
				Double lngs = latlngs.getDouble(1);

				//SET LAT,LNG
				Location coordinateNode = new Location("");
				coordinateNode.setLatitude(lats);
				coordinateNode.setLongitude(lngs);					
				
				//SEARCH DIST OF POS WRT USER COORDINATES IN METRES
				double distance = posisiUser.distanceTo(coordinateNode);
				
				UserDist_NodeCoordinates.add(distance);

			}
			
			// SEARCH MIN WEIGHTS
			int index_coordinateNode = 0;
			for(int m = 0; m < UserDist_NodeCoordinates.size(); m++){
				
				if(UserDist_NodeCoordinates.get(m) <= UserDist_NodeCoordinates.get(0)){
					UserDist_NodeCoordinates.set(0, UserDist_NodeCoordinates.get(m));
					
					// index array of MIN value
					index_coordinateNode = m;
				}				
			}	

			int row_id = cursor.getInt(0);
			
			JSONObject list = new JSONObject();					
			
			// enter the coordinates of the array index, the smallest weight and no of coordinates to JSON
			list.put("row_id", row_id);
			list.put("index", index_coordinateNode);
			list.put("bobot", UserDist_NodeCoordinates.get(0));
			list.put("nodes", jArrNodes.getString(0));
			list.put("count_koordinat", (jArrCoordinates.length() - 1));
			
			JSONArray ja = new JSONArray();
			ja.put(list);
			
			obj.put("" + k, ja);
		}
		
		double x = 0;
		double y = 0;
		int rowId_json = 0;
		int indexCoordinate_json = 0;
		int countCoordinate_json = 0;
		String nodes_json = "";

		//find the smallest weight of JSON
		for(int s = 0; s < obj.length(); s++){
			
			if(s == 0){
				
				JSONArray a = obj.getJSONArray("0");			
				JSONObject b = a.getJSONObject(0);
				x = Double.parseDouble(b.getString("bobot"));
				
				rowId_json = Integer.parseInt(b.getString("row_id"));
				indexCoordinate_json = Integer.parseInt(b.getString("index"));
				countCoordinate_json = Integer.parseInt(b.getString("count_koordinat"));
				nodes_json = b.getString("nodes").toString();
				
			}
			else{
				JSONArray c = obj.getJSONArray("" + s);			
				JSONObject d = c.getJSONObject(0);
				y = Double.parseDouble(d.getString("bobot"));
				
				if(y <= x){
					x = y;
					
					rowId_json = Integer.parseInt(d.getString("row_id"));			
					indexCoordinate_json = Integer.parseInt(d.getString("index"));
					countCoordinate_json = Integer.parseInt(d.getString("count_koordinat"));
					nodes_json = d.getString("nodes").toString();				
				}		
			}
		}		
		
		// nodes : 0-1
		String[] exp_nodes = nodes_json.split("-");

		
		int field_node_start = Integer.parseInt(exp_nodes[0]);
		int field_node_end = Integer.parseInt(exp_nodes[1]);

		// Coordinates added at the beginning or end , no need to add nodes
		if(indexCoordinate_json == 0 || indexCoordinate_json == countCoordinate_json){
			
		//specify the beginning or end node with a position near the user 's position
			if(indexCoordinate_json == 0){
				
				// nodes in field node_start
				fix_node_start = field_node_start; 
			}else if(indexCoordinate_json == countCoordinate_json){
				
				// nodes in field node_dest
				fix_node_start = field_node_end;
			}
			
			jadi_json.put("status", "jalur_none");
			
		}
		else{
			cursor = db.rawQuery("SELECT id FROM graph where simpul_awal = "+ field_node_end + " and simpul_tujuan = " + field_node_start, null);
			cursor.moveToFirst();
			cursor.moveToPosition(0);
			
			int dobel = cursor.getCount();
	
			if(dobel == 1){
				jadi_json.put("status", "jalur_double");
			}
			else if(dobel == 0){
				jadi_json.put("status", "jalur_single");
			}
		}
		
		// JSON
		jadi_json.put("node_simpul_awal0", field_node_start);
		jadi_json.put("node_simpul_awal1", field_node_end);				
		jadi_json.put("index_coordinate_json", indexCoordinate_json);
		jadi_json.put("explode_lat_only", explode_lat_only);
				
		return jadi_json;
		}
}