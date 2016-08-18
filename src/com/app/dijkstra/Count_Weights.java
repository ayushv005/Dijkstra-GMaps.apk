package com.app.dijkstra;

import org.json.JSONArray;
import org.json.JSONException;

import android.location.Location;

public class Count_Weights{
	
	double weight = 0;

	public void Count_Weights(int index, int limit, JSONArray jArrCoordinates) throws JSONException{				

		if(index == limit){
			// get JSON coordinate
			JSONArray latlngs = jArrCoordinates.getJSONArray(index);

			double lat_0 = latlngs.getDouble(0);
			double lng_0 = latlngs.getDouble(1);
			
			Location nodeStart = new Location("");
			nodeStart.setLatitude(lat_0);
			nodeStart.setLongitude(lng_0);
			
			// get coordinate again
			JSONArray latlngs1 = jArrCoordinates.getJSONArray(++index);

			double lat_1 = latlngs1.getDouble(0);
			double lng_1 = latlngs1.getDouble(1);

			Location nodeMiddle = new Location("");							
			nodeMiddle.setLatitude(lat_1);
			nodeMiddle.setLongitude(lng_1);
			
			//keep distance
			weight += nodeStart.distanceTo(nodeMiddle);			
		
		}else{
			for(int i = 0; i < 1; i++){

				// get JSON coordinate
				JSONArray latlngs = jArrCoordinates.getJSONArray(index);

				double lat_0 = latlngs.getDouble(0);
				double lng_0 = latlngs.getDouble(1);
				
				Location nodeStart = new Location("");
				nodeStart.setLatitude(lat_0);
				nodeStart.setLongitude(lng_0);
				
				// get coordinate again
				JSONArray latlngs1 = jArrCoordinates.getJSONArray(++index);

				double lat_1 = latlngs1.getDouble(0);
				double lng_1 = latlngs1.getDouble(1);

				Location nodeMiddle = new Location("");							
				nodeMiddle.setLatitude(lat_1);
				nodeMiddle.setLongitude(lng_1);
				
				//keep distance
				weight += nodeStart.distanceTo(nodeMiddle);
				
				if(index == limit) break; //if limit reached, break; example 0-72
				else --i;
			}
		}
	}
}