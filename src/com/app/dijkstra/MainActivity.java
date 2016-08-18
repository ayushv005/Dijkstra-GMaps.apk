package com.app.dijkstra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.app.dijkstra.SQLHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.app.dijkstra.dijkstra;
import com.app.dijkstra.Adding_nodes;
import com.app.dijkstra.MainActivity;

import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnMapClickListener, OnMapLongClickListener {

	// DB
	SQLHelper dbHelper;
	Cursor cursor;

	// Google Maps
	GoogleMap googleMap;

	public String __global_endposition = null;
	public String __global_startposition = null;
	public int __global_node_start;
	public int __global_node_end;	
	public String __global_old_node_start = "";
	public String __global_old_node_end = "";
	public int __global_maxRow0;
	public int __global_maxRow1;
	private String[][] __global_graphArray;
	private LatLng __global_yourCoordinate_exist = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// create DB
		dbHelper = new SQLHelper(this);
        try {
        	dbHelper.createDataBase();
        } 
        catch (Exception ioe) {
        	Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
        }
 		
        // CREATE MAP
      	if(googleMap == null){
      		googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.peta)).getMap();
      		googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(31.6366497, 74.8788724), 14.0f));
      		if(googleMap != null){
      			googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.peta)).getMap();
      		}
      	}
      	
      	// event map
      	googleMap.setOnMapClickListener(this);
      	googleMap.setOnMapLongClickListener(this);
        
		// Query DB to show all institutes
		dbHelper = new SQLHelper(this);
		final SQLiteDatabase db = dbHelper.getReadableDatabase();		
		cursor = db.rawQuery("SELECT * FROM sekolah", null);
		cursor.moveToFirst();
		
		ArrayList<String> spinner_list_smk = new ArrayList<String>();		
		// Adapter spinner smk
		ArrayAdapter<String> adapter_spinner_smk;	
		
		// put school names to array
		spinner_list_smk.add("-- SELECT SCHOOL --");
		for(int i = 0; i < cursor.getCount(); i++){
			cursor.moveToPosition(i);
			spinner_list_smk.add(cursor.getString(1).toString());
		}
		
		// School names enter into spinner list ( dropdown )
		Spinner spinner = (Spinner) findViewById(R.id.spinner_list_smk);	
	    adapter_spinner_smk = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinner_list_smk);
		adapter_spinner_smk.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);	
		spinner.setAdapter(adapter_spinner_smk);
		spinner.setBackgroundColor(Color.WHITE);
		
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
	
				if(arg0.getItemAtPosition(arg2).toString() != "-- SELECT SCHOOL --"){

	 				String pilih_smk = arg0.getItemAtPosition(arg2).toString();
	 				cursor = db.rawQuery("SELECT koordinat FROM sekolah where sekolah = '" + pilih_smk + "'", null);
	 				cursor.moveToFirst();
	 				cursor.moveToPosition(0);

	 			// get coordinate SMK from field "koordinat"
	 				__global_endposition = cursor.getString(0).toString();
	 				
	 			// user taps the map
	 				if(__global_yourCoordinate_exist != null){
	 					
	 					// your coordinate
	 					double latUser = __global_yourCoordinate_exist.latitude;
	 					double  lngUser = __global_yourCoordinate_exist.longitude;
	 					
	 					// destination coordinates
	 					String[] exp_endCoordinate = __global_endposition.split(",");
	 					double lat_endposition = 31.6366497;//Double.parseDouble(exp_endCoordinate[0]);
	 					double lng_endposition = 74.8788724;//Double.parseDouble(exp_endCoordinate[1]);
	 					
	 				// CORE SCRIPT
	 				// search function on start and destination node , create a graph to Dijkstra's algorithm
	 					try {
	 						startingScript(latUser, lngUser, lat_endposition, lng_endposition);
	 					} catch (JSONException e) {
	 						// TODO Auto-generated catch block
	 						e.printStackTrace();
	 					}
	 					
	 				}else{
	 					Toast.makeText(getApplicationContext(), "Tap on the map to determine your Position", Toast.LENGTH_LONG).show();
	 				}
				}			
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub	
			}
	    });
	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		
		// your coordinate position
		double latUser = arg0.latitude;
		double lngUser = arg0.longitude;
		
		__global_yourCoordinate_exist = arg0;
		
		// destination coordinate position
		String endposition = __global_endposition;

		if(endposition != null){

			// broken coordinate SMK
			String[] exp_endposition = endposition.split(",");
			double lat_endposition = Double.parseDouble(exp_endposition[0]);
			double lng_endposition = Double.parseDouble(exp_endposition[1]);

			// CORE SCRIPT 
			// using start and destination node to make graph using Dijkstra's Algorithm
			try {
				startingScript(latUser, lngUser, lat_endposition, lng_endposition);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}else{
			Toast.makeText(getApplicationContext(), "Select First Institute", Toast.LENGTH_LONG).show();
		}	
	}
	
	/*
	 * CORE SCRIPT
	 * 
	 * @The main function
	 * ( 1 ) obtain the coordinates of the beginning and end around Public Transit lines
	 *
	 * ( 2 ) start coordinate converted into initial node and destination coordinates as end node
	 *
	 * ( 3) The beginning and the end node is then used as ' input ' for the calculation by Dijkstra's algorithm
	 *
	 * ( 4 ) after calculation, the shortest paths obtained drawn o path using polyline
	 *
	 * @parameter
	 * LatUser and lngUser : coordinates of the user's position
	 * Lat_endposition and lng_endposition : School's position coordinate
	 *
	 * @return
	 * No return
	 */
	public void startingScript(double latUser, double lngUser, double lat_endposition, double lng_endposition) throws JSONException{				
		    	
		// delete temporary record DB
		deleteTemporaryRecord();
		
		// reset google map
		googleMap.clear();
		
		// convert graph from DB to Array; graph[][]
		GraphToArray DBGraph = new GraphToArray();
		__global_graphArray = DBGraph.convertToArray(this); // return graph[][] Array
		
		// get max++ row temporary DB
		maxRowDB();
		
		// GET COORDINATE OF BEGINNING NODE
		// start coordinate converted to initial node
		// return __global_node_start, __global_graphArray[][]
		Get_coordinate_start_end start_coordinate_line = new Get_coordinate_start_end();
		getNodesStartEndPath(start_coordinate_line, latUser, lngUser, "awal");

		// GET COORDINATE OF END NODE
		// last coordinate converted to the end node
		// return __global_node_end, __global_graphArray[][]
		Get_coordinate_start_end destination_coordinate_line = new Get_coordinate_start_end();		
		getNodesStartEndPath(destination_coordinate_line, lat_endposition, lng_endposition, "akhir");

		// DIJKSTRA'S ALGORITHM
		dijkstra algo = new dijkstra();
		algo.shortestPath(__global_graphArray, __global_node_start, __global_node_end);

		// no result for dijkstra's algorithm
		if(algo.status == "die"){
		
			Toast.makeText(getApplicationContext(), "Your location is nearby Destination", Toast.LENGTH_LONG).show();
		
		}else{
			// return the shortest path; example 1->5->6->7
	       	String[] exp = algo.shortestPathString1.split("->");
			 
	     // DRAW PUBLIC TRANSPORT PATH 
	       	drawPath(algo.shortestPathString1, exp);			
		}

	}
	
	
	/*
	* @function
	* Draw public transit lines
	* Determine the type of public transport passing lane
	* Create a marker to your position and the destination position
	* @parameter
	* Exp[ ] : the shortest path ; example 1- > 5- > 6- > 7
	* @return
	* No return
	*/
	public void drawPath(String alg, String[] exp) throws JSONException{
		
        int start = 0;
		
     // FINDING OUT THE PATH
		dbHelper = new SQLHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();  
		
        for(int i = 0; i < exp.length-1; i++){
        
        	ArrayList<LatLng> lat_lng = new ArrayList<LatLng>();
        	
        	cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal ="+exp[start]+" and simpul_tujuan ="+exp[(++start)], null);
			cursor.moveToFirst();

			
			// get the Lat, Lng coordinates and that of the field 
			String json = cursor.getString(0).toString();

			// get JSON
			JSONObject jObject = new JSONObject(json);
			JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");

			// get coordinate JSON
			for(int w = 0; w < jArrCoordinates.length(); w++){
				
				JSONArray latlngs = jArrCoordinates.getJSONArray(w);
				Double lats = latlngs.getDouble(0);
				Double lngs = latlngs.getDouble(1);
					
				
				lat_lng.add( new LatLng(lats, lngs) );

			}
			
			// make blue line with given width
			PolylineOptions regBlueLine = new PolylineOptions();
			regBlueLine.addAll(lat_lng).width(7).color(0xff4b9efa).geodesic(true);
			googleMap.addPolyline(regBlueLine);
			
        }
        
        
     // MAKE MARKER FOR YOUR POSITION AND DESTINATION POSITION
        // your position
        googleMap.addMarker(new MarkerOptions()
        .position(__global_yourCoordinate_exist)
        .title("Your position")
        .snippet("Your position")
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));        
         
		String[] exp_endCoordinate = __global_endposition.split(",");
		double lat_endPosition = Double.parseDouble(exp_endCoordinate[0]);
		double lng_endPosition = Double.parseDouble(exp_endCoordinate[1]);		
		LatLng endx = new LatLng(lat_endPosition, lng_endPosition);
        
		// destination position
		googleMap.addMarker(new MarkerOptions()
        .position(endx)
        .title("Destination position")
        .snippet("Destination position")
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));          

		
		// SET OF PUBLIC TRANSPORT FOR THE PATH
		// example exp[] = 1->5->6->7
		int m = 0;
		
		
		String[] startStr = __global_old_node_start.split("-"); // example 4-5
		String[] endStr = __global_old_node_end.split("-"); // example 8-7

		int change_a = 0;
		int change_b = 0;
		int nodeStartDijkstra = Integer.parseInt(exp[0]);

		String joinNodes_all = "";
    	Map<String, ArrayList> listPublicTransport = new HashMap<String, ArrayList>();
    	ArrayList<Integer> listNodesTransport = new ArrayList<Integer>();

    	// find node_old before coordinates are broken
    	// example 4-5 broken into 4-6-5, node_old beg = 5, node_old dest = 4
        for(int e = 0; e < (exp.length - 1); e++){
        	
        	if(e == 0){ // start

        		// algo run only if the results of two vertices, example : 4->5
        		if(exp.length == 2 /* 2 nodes (4-5)*/){
            		
        			// No new node at the beginning ( 10 ) and at the end ( 11 ), example 10->11
        			if( exp[0].equals(String.valueOf(__global_maxRow0)) && exp[1].equals(String.valueOf(__global_maxRow1)) ){				
        				
    					if(String.valueOf(__global_maxRow0).equals(endStr[0])){
    						change_b = Integer.parseInt(endStr[1]);
    					}else{
    						change_b = Integer.parseInt(endStr[0]);
    					}
    					
    					if(String.valueOf(change_b).equals(startStr[0])){
    						change_a = Integer.parseInt(startStr[1]);
    					}else{
    						change_a = Integer.parseInt(startStr[0]);
    					}
        			}
        			else{
        				// No new node at the beginning ( 10 ) , example 10- > 5
        				// Then find initial node
        				if( exp[0].equals(String.valueOf(__global_maxRow0)) ){
        					
            				if(exp[1].equals(startStr[1])){
	        					change_a = Integer.parseInt(startStr[0]);
            				}else{
            					change_a = Integer.parseInt(startStr[1]);
            				}
	            				change_b = Integer.parseInt(exp[1]);
        				}
        				// No new node at the end ( 10 ) , example 5- > 10
        				// then find the end node
        				else if( exp[1].equals(String.valueOf(__global_maxRow0)) ){
        					
        					if(exp[0].equals(endStr[0])){
	        					change_b = Integer.parseInt(endStr[1]);
            				}else{
            					change_b = Integer.parseInt(endStr[0]);
            				}       					
        					change_a = Integer.parseInt(exp[0]);   					
        				}
        				// no additional node 
        				else{
        					change_a = Integer.parseInt(exp[0]);
        					change_b = Integer.parseInt(exp[1]);
        				}
        			}
        		}
        		// algo results more dr 2 : 4->5->8->7-> etc ..
        		else{        			
            		if(exp[1].equals(startStr[1])){ // 5 == 5
            			change_a = Integer.parseInt(startStr[0]); // return 4
            		}else{
            			change_a = Integer.parseInt(startStr[1]); // return 5
            		}
            		
        			change_b = Integer.parseInt( exp[++m] );
        		}
        	}	        
        	else if(e == (exp.length - 2)){ // end
        		
        		if(exp[ (exp.length - 2) ].equals(endStr[1])){ // 7 == 7
        			change_b = Integer.parseInt(endStr[0]); // return 8
        		}else{
        			change_b = Integer.parseInt(endStr[1]); // return 7
        		}
        		
        		change_a = Integer.parseInt( exp[m] );
        		
        	}else{ // the middle
        		change_a = Integer.parseInt( exp[m] );
        		change_b = Integer.parseInt( exp[++m] );
        	}

        	joinNodes_all += "," + change_a + "-" + change_b + ","; // ,1-5,
        	String joinNodesStr = "," + change_a + "-" + change_b + ","; // ,1-5,
        	
			cursor = db.rawQuery("SELECT * FROM angkutan_umum where simpul like '%" + joinNodesStr + "%'", null);
			cursor.moveToFirst();

			ArrayList<String> listTransport1 = new ArrayList<String>();
			
			for(int ae = 0; ae < cursor.getCount(); ae++){				
				cursor.moveToPosition(ae);
				listTransport1.add( cursor.getString(1).toString() );				
			}        	
        	
			listPublicTransport.put("angkutan" + e, listTransport1);
			
			// // add nodes of transportation
			listNodesTransport.add( Integer.parseInt(exp[e]) ); 

        }
 
		
        String replace_path = joinNodes_all.replace(",,", ","); //  ,1-5,,5-6,,6-7, => ,1-5,5-6,6-7,
		cursor = db.rawQuery("SELECT * FROM angkutan_umum where simpul like '%" + replace_path + "%'", null);
		cursor.moveToFirst();
		cursor.moveToPosition(0);
		
		// No 1 transportation lines that pass from the beginning to the end
		if(cursor.getCount() > 0){

			String siTransport2 = cursor.getString(1).toString();

			// get coordinates
			cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal = '" + nodeStartDijkstra + "'", null);
			cursor.moveToFirst();
			String json_coordinate = cursor.getString(0).toString();
			
			// manipulating JSON
			JSONObject jObject = new JSONObject(json_coordinate);
			JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");
			JSONArray latlngs = jArrCoordinates.getJSONArray(0);
			
			// first lat-lng
			Double lats = latlngs.getDouble(0);
			Double lngs = latlngs.getDouble(1);

			googleMap.addMarker(new MarkerOptions()
	        .position(new LatLng(lats, lngs))
	        .title("You")
	        .snippet(siTransport2)
	        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))).showInfoWindow(); 
			
			// die()
			return;
		}
		
		//two or more public transportation that passes through the beginning to the end
		int manyTransports = 0;
		int indexRt = 0;
		int indexNodeTransport3 = 1;
        int lengthTransport4 = listPublicTransport.size();
        Map<String, ArrayList> transportFix5 = new HashMap<String, ArrayList>();

        for(int en = 0; en < lengthTransport4; en++ ){

        	// temporary retainAll()
        	ArrayList<String> temps = new ArrayList<String>();
        	for(int u = 0; u < listPublicTransport.get("angkutan0").size(); u++){
        		temps.add( listPublicTransport.get("angkutan0").get(u).toString() );
        	}        	        	
        	
        	if(en > 0 ){
	    		ArrayList listCurrent1 = listPublicTransport.get("angkutan0");
				ArrayList listFurther1 = listPublicTransport.get("angkutan" + en);	
				
				// intersection
				listCurrent1.retainAll(listFurther1);
	            
	            if(listCurrent1.size() > 0){
	            	
	            	listNodesTransport.remove(indexNodeTransport3);
	            	--indexNodeTransport3;

	            	listPublicTransport.remove("angkutan" + en);

	            	if(en == (lengthTransport4 - 1)){
	            		
		            	ArrayList<String> tempWithin = new ArrayList<String>();
		            	for(int es = 0; es < listCurrent1.size(); es++){
		            		tempWithin.add( listCurrent1.get(es).toString() );
		            	}
		            	
	            		transportFix5.put("angkutanFix" + indexRt, tempWithin);
		            	++indexRt;	
	            	}
	            }	            
	            else if(listCurrent1.size() == 0){
	            	
	            	transportFix5.put("angkutanFix" + indexRt, temps);
	            	
	            	ArrayList<String> tempWithin = new ArrayList<String>();
	            	for(int es = 0; es < listFurther1.size(); es++){
	            		tempWithin.add( listFurther1.get(es).toString() );
	            	}
	            	
	            	//if(en == 1) break;
	            	listPublicTransport.get("angkutan0").clear();
	            	listPublicTransport.put("angkutan0", tempWithin);
	            	
	            	//if(en != (listPublicTransport.size() - 1)){
	            		listPublicTransport.remove("angkutan" + en);	
	            	//}
	            	
		            ++indexRt;
		            
	            	if(en == (lengthTransport4 - 1)){
	            		
		            	ArrayList<String> tempWithin2 = new ArrayList<String>();
		            	for(int es = 0; es < listFurther1.size(); es++){
		            		tempWithin2.add( listFurther1.get(es).toString() );
		            	}
		            	
	            		transportFix5.put("angkutanFix" + indexRt, tempWithin2);
		            	++indexRt;	
	            	}		            
	            }
	        	
	        	++indexNodeTransport3;
        	}
        }
        
        for(int r = 0; r < listNodesTransport.size(); r++){
        	String simpulx = listNodesTransport.get(r).toString();
			// get coordinate simpulAngkutan
			cursor = db.rawQuery("SELECT jalur FROM graph where simpul_awal = '" + simpulx + "'", null);
			cursor.moveToPosition(0);
			
			// get Lat, Lng and coordinates of the field
			String json = cursor.getString(0).toString();

			// get JSON
			JSONObject jObject = new JSONObject(json);
			JSONArray jArrCoordinates = jObject.getJSONArray("coordinates");

			// get first coordinate JSON
			JSONArray latlngs = jArrCoordinates.getJSONArray(0);
			Double lats = latlngs.getDouble(0);
			Double lngs = latlngs.getDouble(1);
				
			LatLng nodePTransport = new LatLng(lats, lngs);
			String siTransport2 = transportFix5.get("angkutanFix" + r).toString();
			
			if(r == 0){
				googleMap.addMarker(new MarkerOptions()
		        .position(nodePTransport)
		        .title("You")
		        .snippet(siTransport2)
		        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))).showInfoWindow(); 
			}else{
				googleMap.addMarker(new MarkerOptions()
		        .position(nodePTransport)
		        .title("You")
		        .snippet(siTransport2)
		        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));		
			}
        }
        
	}
	
	public void getNodesStartEndPath(Get_coordinate_start_end objects, double latx, double lngx, String statusObject) throws JSONException{
		
		// return JSON index position coordinates , nodes0 , nodes1
		JSONObject jStart = objects.Get_simpul(latx, lngx, this);

		// index JSON
		String status = jStart.getString("status");
		int node_node_start0 = jStart.getInt("node_simpul_awal0");
		int node_node_start1 = jStart.getInt("node_simpul_awal1");
		int index_coordinate_json = jStart.getInt("index_coordinate_json");
		
		
		int fix_node_start = 0;
		
		// if the coordinates of position is above the node/nodes
		// it is not necessary to add a new node  jalur==path
		if(status.equals("jalur_none")){
			
			//specify the beginning or end node with a position near the user 's position
			if(index_coordinate_json == 0){ // start		
				fix_node_start = node_node_start0;				
			}else{ // end		
				fix_node_start = node_node_start1;				
			}
			
			if(statusObject == "awal"){	
				
				// return
				__global_old_node_start = node_node_start0 + "-" + node_node_start1;
				__global_node_start = fix_node_start; // example 0				
			}else{
				
				// return
				__global_old_node_end = node_node_start0 + "-" + node_node_start1;
				__global_node_end = fix_node_start; // example 0				
			}
		
						
		}
		// if coordinates are b/w node 5 and node 4 or b/w node 4 and node 5
		//necessary to add a new node
		else if(status.equals("jalur_double")){

			// return		
			if(statusObject == "awal"){				
				
				// find nodes (5,4) and (4-5) in Adding_nodes.java
				Adding_nodes obj_plus = new Adding_nodes();
				obj_plus.doubleNode(node_node_start0, node_node_start1, index_coordinate_json, 
										this, __global_graphArray, 401
									); // 401 : new row id
										
				
				// return
				__global_old_node_start = obj_plus.node_oldStr;
				__global_node_start = obj_plus.node_new; // example 6
				__global_graphArray = obj_plus.modif_graph; // graph[][]

			}else{
			
				// find nodes (5,4) and (4-5) in Adding_nodes.java
				Adding_nodes obj_plus = new Adding_nodes();
				obj_plus.doubleNode(node_node_start0, node_node_start1, index_coordinate_json, 
										this, __global_graphArray, 501
									); // 501 : new row id
										
				
				// return
				__global_old_node_end = obj_plus.node_oldStr;
				__global_node_end = obj_plus.node_new; // example 4			
				__global_graphArray = obj_plus.modif_graph; // graph[][]
								
			}

		}
		//If the coordinates are only b/w node 5 and node 4
				// It is necessary to add a new node
		else if(status.equals("jalur_single")){

			if(statusObject == "awal"){
				
				// find node (5,4) in Adding_nodes.java
				Adding_nodes obj_plus1 = new Adding_nodes();
				obj_plus1.singleNode(node_node_start0, node_node_start1, index_coordinate_json, 
										this, __global_graphArray, 401
									); // 401 : new row id 
										
				
				// return
				__global_old_node_start = obj_plus1.node_oldStr;
				__global_node_start = obj_plus1.node_new; // example 6
				__global_graphArray = obj_plus1.modif_graph; // graph[][]
				
			}else{
				
				// find node (5,4) in Adding_nodes.java
				Adding_nodes obj_plus1 = new Adding_nodes();
				obj_plus1.singleNode(node_node_start0, node_node_start1, index_coordinate_json, 
						this, __global_graphArray, 501
					); // 501 : new row id

				
				// return
				__global_old_node_end = obj_plus1.node_oldStr;
				__global_node_end = obj_plus1.node_new; // example 4			
				__global_graphArray = obj_plus1.modif_graph; // graph[][]	
			}		
		}		
	}
	
	
	/*
	 * @function
	 *  delete temporary record DB
	 *  (This is used to temporarily accommodate the new node)
	 * @parameter
	 *  no parameter
	 * @return
	 *  no return
	 */
	public void deleteTemporaryRecord(){
		
		// delete DB
		final SQLiteDatabase dbDelete = dbHelper.getWritableDatabase();

		// delete temporary record DB
		for(int i = 0; i < 4; i++){								
			//remove additional initial node, start dr id 401,402,403,404
			String deleteQuery_ = "DELETE FROM graph where id ='"+ (401+i) +"'";
			dbDelete.execSQL(deleteQuery_);	
			
			//remove additional destination node, start dr id 501,502,503,504
			String deleteQuery = "DELETE FROM graph where id ='"+ (501+i) +"'";
			dbDelete.execSQL(deleteQuery);	
		}
	}
	
	public void maxRowDB(){
		
		dbHelper = new SQLHelper(this);
		SQLiteDatabase dbRead = dbHelper.getReadableDatabase();		
		
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
		
		// return
		__global_maxRow0 = (max_node_db+1);
		__global_maxRow1 = (max_node_db+2);
	}

}
