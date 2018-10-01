package uk.ac.cam.tal42.mlrwd.exercises.Exercise10;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import uk.ac.cam.cl.mlrwd.exercises.social_networks.IExercise10;

public class Exercise10 implements IExercise10 {

	@Override
	public Map<Integer, Set<Integer>> loadGraph(Path graphFile) throws IOException {
		// TODO Auto-generated method stub
		
		Map<Integer, Set<Integer>> graph = new HashMap<>();
		
		BufferedReader br = new BufferedReader(new FileReader(graphFile.toFile()));
		
		String fileLine;
		String[] splitData;
		
		while((fileLine = br.readLine()) != null) {
			splitData = fileLine.split(" ");
			int currentNode = Integer.valueOf(splitData[0]);
			int connectedNode = Integer.valueOf(splitData[1]);
			
			if (graph.containsKey(currentNode)) {
				Set<Integer> connectedNodes = graph.get(currentNode);
				connectedNodes.add(connectedNode);
			}else {
				Set<Integer> connectedNodes = new HashSet<>();
				connectedNodes.add(connectedNode);
				graph.put(currentNode, connectedNodes);
			}
			
			if (graph.containsKey(connectedNode)) {
				Set<Integer> reverseConnectedNodes = graph.get(connectedNode);
				reverseConnectedNodes.add(currentNode);
			}else{
				Set<Integer> reverseConnectedNodes = new HashSet<>();
				reverseConnectedNodes.add(currentNode);
				graph.put(connectedNode, reverseConnectedNodes);
			}
			
		}
		
		return graph;
	}

	@Override
	public Map<Integer, Integer> getConnectivities(Map<Integer, Set<Integer>> graph) {
		// TODO Auto-generated method stub
		
		Map<Integer,Integer> connectivities = new HashMap<>();
		
		for (int n : graph.keySet()) {
			int degree = graph.get(n).size();
			connectivities.put(n,degree);
			
		}
		
		return connectivities;
	}

	@Override
	public int getDiameter(Map<Integer, Set<Integer>> graph) {
		// TODO Auto-generated method stub
		int maxPath = 0;
		
		
		for (int n : graph.keySet()) {
			int currentPathLength = bfsLength(n,graph);
			
			//System.out.println("Processed node " + n + "/" + graph.size() );
			
			if (maxPath < currentPathLength) {
				maxPath = currentPathLength;
				
			}
		}
		
		
		
		return maxPath;
	}
	
	private int bfsLength(int node, Map<Integer,Set<Integer>> graph) {
		
		int breadthLength = 0;
		Queue<Integer> toSearch = new ArrayDeque<>(); 
		Map<Integer,Integer> distances = new HashMap<>();
		
		
		toSearch.offer(node);
		distances.put(node,0);
		
		while (!toSearch.isEmpty()) {
			int currentNode = toSearch.poll();
			
			for (int neighbour : graph.get(currentNode)) {
				
				if (!distances.containsKey(neighbour)) {
					toSearch.offer(neighbour);
					breadthLength = distances.get(currentNode) + 1;
					distances.put(neighbour, breadthLength);
				}
						
		
				
			}
			
		}
		
		
		return breadthLength;
	}

}
