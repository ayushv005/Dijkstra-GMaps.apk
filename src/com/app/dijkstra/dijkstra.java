package com.app.dijkstra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class dijkstra {
	
	SQLHelper dbHelper;
	Cursor cursor;
	
	String[][] graph = new String[100][100];
	String shortestPathString1 = "";
	String status = "none";

	void shortestPath(String[][] arg_graph, int nodeStart, int nodeEnd){

		System.out.println("sa : " + nodeStart + " & st : " + nodeEnd);
		if(nodeStart == nodeEnd){
			status = "die";
			return;
		}

		
		graph = arg_graph;
        int node_start = nodeStart;
        int node_forward = nodeStart;
        int node_end = nodeEnd;  		
        
        if(node_forward != node_end){
        	//COUNTING NO OF NODES
            int jml_nodes = 0;
            for(String[] array : graph){
                if(array[0] != null){
                    jml_nodes += 1;
                }
            }          
            
            List<Integer> nodeVisited = new ArrayList<Integer>(); 
            
          //STORED VALUES ARE MARKED *
            List<Integer> nodeAlreadyDoneUnder = new ArrayList<Integer>();

            double node_value_marked = 0;
            double fixed_node_value_marked = 0;
            
            //RECCURENCE HANDLE
            for(int itr = 0; itr < 1; itr++){
            	//ASSIGN 1 FOR MOST MINIMUM WEIGHT OF EACH NODE
                List<Double> comparisonWeights = new ArrayList<Double>();
                
                if(!nodeVisited.contains(node_forward)){
                    nodeVisited.add(node_forward);
                }
                
              //RECCURENCE NODES ARE MARKED
                for(int itrNode = 0; itrNode < nodeVisited.size(); itrNode++){
                	//CALC NODES
                    int jml_line_fix = 0;
                        for(int min_boundary_line = 0; min_boundary_line < 100; min_boundary_line++){
                            if(graph[nodeVisited.get(itrNode)][min_boundary_line] != null){
                                jml_line_fix += 1;
                            } 
                        }

                      //SEARCH MINIMUM WEIGHT in one node based on the lines scr sequence[0][0],[0][1] dst
                        List<Double> weight = new ArrayList<Double>();                        
                        int status_line = 0;

                      //looping WEIGHT2 SEARCH IN NODE 1
                        for(int min_boundary_line_fix = 0; min_boundary_line_fix < jml_line_fix; min_boundary_line_fix++){
                            String weights_and_segments = graph[nodeVisited.get(itrNode)][min_boundary_line_fix];
                            //[0][0],[0][1],[0][2] dst
                            String[] explode;
                            explode = weights_and_segments.split("->");                 
                            if(explode.length == 2){
                                status_line += 1;
                                               
                                if(!nodeAlreadyDoneUnder.isEmpty()){                                    
                                    if(nodeAlreadyDoneUnder.contains(nodeVisited.get(itrNode))){
                                       node_value_marked = 0;                                            
                                    }else{
                                      node_value_marked = fixed_node_value_marked;
                                    }
                                }
                                                                
                                weight.add((Double.parseDouble(explode[1])+node_value_marked)); 
                               graph[nodeVisited.get(itrNode)][min_boundary_line_fix] = 
                               String.valueOf(explode[0]+"->"+(Double.parseDouble(explode[1])+node_value_marked));                                
                            }
                        }

                      //if the line in the column - > y FOR all , then do below :
                        if(status_line > 0){

                        	//GET THE MINIMUM WEIGHT
                            for(int index_weight = 0; index_weight < weight.size(); index_weight++){
                               if(weight.get(index_weight) <= weight.get(0)){
                                   weight.set(0, weight.get(index_weight));
                               }
                            } 

                        comparisonWeights.add(weight.get(0));                            
                        }//end if when ->y or else ->t not all done
                        else{
                        }   
                        
                      //Sign that Nodes completed (start = dist)
                        if(!nodeAlreadyDoneUnder.contains(nodeVisited.get(itrNode))){
                            nodeAlreadyDoneUnder.add(nodeVisited.get(itrNode));
                        }
                }//end for loopingNode                         
                
              //get the minimum marked weight
                for(int min_index_weight = 0; min_index_weight < comparisonWeights.size(); min_index_weight++){
                    if(comparisonWeights.get(min_index_weight) <= comparisonWeights.get(0)){
                        comparisonWeights.set(0, comparisonWeights.get(min_index_weight));
                    }
                }

              //GET INDEX NODE + weighs 
                int firstInitialStart = 0; 
                int status_line1 = 0;                
                int firstIndexWeights = 0;
                int node_old = 0;
                for(Integer indexFirst_weight : nodeVisited){
                    for(int line1 = 0; line1 < 100; line1++){
                        if(graph[nodeVisited.get(firstInitialStart)][line1] != null){
                            String weights_and_segments1 = graph[nodeVisited.get(firstInitialStart)][line1];                            
                            String[] explode1;
                            explode1 = weights_and_segments1.split("->");
                            if(explode1.length == 2){                         
                                if(comparisonWeights.get(0) == Double.parseDouble(explode1[1])){
                                    firstIndexWeights = line1;
                                    node_old = nodeVisited.get(firstInitialStart);
                                    node_forward = Integer.parseInt(explode1[0]);
                                    status_line1 += 1;                   
                                }                                         
                            }//end if check ->y or else ->t
                        }//end if check line != null    
                    }//end for limit line = 100         
                    firstInitialStart++; //node index + 1
                }
                       
                if(status_line1 > 0){                    
                    graph[node_old][firstIndexWeights] = graph[node_old][firstIndexWeights]+"->y";
                  
                    for(int min_column = 0; min_column < jml_nodes; min_column++){
                        for(int min_lines = 0; min_lines < 100; min_lines++){
                                    
                            if(graph[min_column][min_lines] != null){
                                String roadBeDeletd = graph[min_column][min_lines];
                                String[] explode3 = roadBeDeletd.split("->");                                  
                                if(explode3.length == 2){
                                    if(explode3[0].equals(String.valueOf(node_forward))){
                                        graph[min_column][min_lines] = graph[min_column][min_lines]+"->t";                                        
                                    }
                                }//end if check ->y or else ->t
                            }//end if check line != null
                        }//end for column
                    }//end for column
                }//end if check status_line is already ->y or else ->t 
       
                // Values ​​marked *
                fixed_node_value_marked = comparisonWeights.get(0);              
                if(node_forward != node_end){
                  --itr; 
                }
                else{
                    break; //end loop
                }
            }//end for looping handle
        
          //put a combined node to the array; eg node 6-10
            List<String> joinNodesList = new ArrayList<String>();
            for(int h = 0; h < jml_nodes; h++){
                for(int n = 0; n < 100; n++){
                    if(graph[h][n] != null){
                        String str_graph = graph[h][n];
                        if(str_graph.substring(str_graph.length()-1, str_graph.length()).equals("y")){
                            String[] explode4 = graph[h][n].split("->");
                            String joinNodeStr = h+"-"+explode4[0];
                            
                            joinNodesList.add(joinNodeStr);
                        }
                    }//end if contents of the graph ! = null
                }//end for looping line
            }//end looping column (nodes)
                 
            List<Integer> nodeFix_finish = new ArrayList<Integer>();  
            nodeFix_finish.add(node_end);         
            int node_explode = node_end;  
            
            for(int v = 0; v < 1; v++){
                for(int w = 0; w < joinNodesList.size(); w++){
                    String explode_Nodes = joinNodesList.get(w);
                    String[] explode5 = explode_Nodes.split("-");
                    if(node_explode == Integer.parseInt(explode5[1])){
                        nodeFix_finish.add(Integer.parseInt(explode5[0]));
                        node_explode = Integer.parseInt(explode5[0]);
                    }
                    if(node_explode == node_start){
                        break;
                    }
                }             
                if(node_start != node_explode){
                    --v;
                }else{
                    break;
                }
            }            
            Collections.reverse(nodeFix_finish);
            String shortestPathStr = "";
            for(int x = 0; x < nodeFix_finish.size(); x++){
                if(x == nodeFix_finish.size()-1){
                    shortestPathStr += nodeFix_finish.get(x);
                }else{
                    shortestPathStr += nodeFix_finish.get(x)+"->";
                }
            }
            
            shortestPathString1 = shortestPathStr;
        }//end if start != finish		
        
	}
}