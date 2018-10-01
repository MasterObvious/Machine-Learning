package uk.ac.cam.tal42.mlrwd.exercises.Exercise12;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import uk.ac.cam.cl.mlrwd.exercises.social_networks.IExercise12;

public class Exercise12 implements IExercise12 {

	@Override
	public List<Set<Integer>> GirvanNewman(Map<Integer, Set<Integer>> graph, int minimumComponents) {
		// TODO Auto-generated method stub
		
		List<Set<Integer>> subGraphs = getComponents(graph);
		
		
		while (subGraphs.size() < minimumComponents) {
			Map<Integer,Map<Integer,Double>> edgeBetweenness = getEdgeBetweenness(graph);
			double maxBetweenness = Double.NEGATIVE_INFINITY;
			
			for (Map<Integer,Double> row : edgeBetweenness.values()){
				for (double b : row.values()) {
					if (b > maxBetweenness) {
						maxBetweenness = b;
					}
				}
			}
			
			for (int u: graph.keySet()) {
				for (int v : graph.keySet()) {
					if (edgeBetweenness.get(u).get(v) == maxBetweenness) {
						graph.get(u).remove(v);
					}
				}
			}
			
			subGraphs = getComponents(graph);
			
			
			
		}
		
		return subGraphs;
	}

	@Override
	public int getNumberOfEdges(Map<Integer, Set<Integer>> graph) {
		// TODO Auto-generated method stub
		int edgeNumber = 0;
		
		for (int v : graph.keySet()) {
			edgeNumber += graph.get(v).size();
		}
		
		return edgeNumber / 2;
	}

	@Override
	public List<Set<Integer>> getComponents(Map<Integer, Set<Integer>> graph) {
		// TODO Auto-generated method stub
		
		List<Set<Integer>> components = new ArrayList<>();
		Set<Integer> totalSeen = new HashSet<>();
		
		for (int v : graph.keySet()) {
			if (!totalSeen.contains(v)){
				Set<Integer> currentComponent = dfs(v,graph);
				totalSeen.addAll(currentComponent);
				components.add(currentComponent);
			}
		}
		
		
		return components;
	}

	@Override
	public Map<Integer, Map<Integer, Double>> getEdgeBetweenness(Map<Integer, Set<Integer>> graph) {
		// TODO Auto-generated method stub
		
		
		Queue<Integer> Q = new ArrayDeque<>();
		Stack<Integer> S = new Stack<>();
		
		Map<Integer,Integer> dist = new HashMap<>();
		Map<Integer,Set<Integer>> Pred = new HashMap<>();
		Map<Integer, Double> shortestPathsNumber = new HashMap<>();
		Map<Integer, Double> dependency = new HashMap<>();
		Map<Integer,Map <Integer, Double>> betweeness = new HashMap<>();
		
	
		for (int v : graph.keySet()) {
			Map<Integer, Double> betweenessRow = new HashMap<>();
			for (int u : graph.keySet()) {
				betweenessRow.put(u, 0.0);
			}
			betweeness.put(v, betweenessRow);
		}
		
		for (int s : graph.keySet()) {
			//single-source shortest paths
			
			//init
			Pred = new HashMap<>();
			dist = new HashMap<>();
			shortestPathsNumber = new HashMap<>();
			dependency = new HashMap<>();
			
			
			for (int w : graph.keySet()) {
				
				Pred.put(w, new HashSet<>());
				dist.put(w, -1);
				shortestPathsNumber.put(w, 0.0);
				dependency.put(w, 0.0);
			}
			
			dist.put(s, 0);
			shortestPathsNumber.put(s, 1.0);
			Q.offer(s);
			
			while (!Q.isEmpty()) {
				int v = Q.poll();
				
				
				for (int w : graph.get(v)) {
					//path discovery
					if (dist.get(w) == -1) {
						dist.put(w, dist.get(v) + 1);
						Q.offer(w);
						S.push(w);
					}
					//path counting
					if (dist.get(w) == dist.get(v) + 1) {
						shortestPathsNumber.put(w, shortestPathsNumber.get(w) + shortestPathsNumber.get(v));
						Pred.get(w).add(v);
					}
				}
			}
			
			//accumulation
			while(!S.isEmpty()) {
				int w = S.pop();
				for (int v : Pred.get(w)) {
					double c = shortestPathsNumber.get(v) / shortestPathsNumber.get(w) * (1 + dependency.get(w));
					dependency.put(v, dependency.get(v) + c);
					betweeness.get(v).put(w, betweeness.get(v).get(w) + c);
				}
			}
			
		}
		
		return betweeness;
			
	}
	
	public Set<Integer> dfs(int startNode, Map<Integer, Set<Integer>> graph) {
		
		Stack<Integer> toSearch = new Stack<>();
		Set<Integer> seen = new HashSet<>();
		
		toSearch.add(startNode);
		seen.add(startNode);
		
		while (!toSearch.isEmpty()) {
			int v = toSearch.pop();
			
			for (int neighbour : graph.get(v)) {
				
				if (!seen.contains(neighbour)) {
					toSearch.add(neighbour);
					seen.add(neighbour);
				}
			}
			
			
		}
		
		return seen;
		
	}
	
	

}
