package uk.ac.cam.tal42.mlrwd.exercises.Exercise11;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import uk.ac.cam.cl.mlrwd.exercises.social_networks.IExercise11;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise10.Exercise10;

public class Exercise11 implements IExercise11 {

	@Override
	public Map<Integer, Double> getNodeBetweenness(Path graphFile) throws IOException {
		// TODO Auto-generated method stub
		Exercise10 graphImplementation = new Exercise10();
		
		Map<Integer, Set<Integer>> graph = graphImplementation.loadGraph(graphFile);
		
		//initilization
		Queue<Integer> Q = new ArrayDeque<>();
		Stack<Integer> S = new Stack<>();
		
		Map<Integer,Integer> dist = new HashMap<>();
		Map<Integer,Set<Integer>> Pred = new HashMap<>();
		Map<Integer, Double> shortestPathsNumber = new HashMap<>();
		Map<Integer, Double> dependency = new HashMap<>();
		Map<Integer, Double> betweeness = new HashMap<>();
		
		
		for (int v : graph.keySet()) {
			betweeness.put(v,0.0);
		}
		
		for (int s : graph.keySet()) {
			//single-source shortest paths
			System.out.println("Processing " +s);
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
			while (!S.isEmpty()) {
				int w = S.pop();
				
				for (int v : Pred.get(w)) {
					dependency.put(v, dependency.get(v) + (shortestPathsNumber.get(v) / shortestPathsNumber.get(w)) * (1 + dependency.get(w)));
				}
				
				if (w != s) {
					betweeness.put(w, betweeness.get(w) + dependency.get(w));
				}
				
			}
			
			S.clear();
			Q.clear();
		}
		
		for (int v : graph.keySet()) {
			betweeness.put(v, betweeness.get(v) / 2.0);
		}
		
		
		return betweeness;
	}

}
