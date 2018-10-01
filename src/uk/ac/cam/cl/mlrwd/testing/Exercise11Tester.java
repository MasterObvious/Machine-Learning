package uk.ac.cam.cl.mlrwd.testing;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Comparator;

import uk.ac.cam.cl.mlrwd.exercises.social_networks.IExercise11;
import uk.ac.cam.cl.mlrwd.utils.ChartPlotter;
import uk.ac.cam.cl.mlrwd.utils.BestFit.Point;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise11.Exercise11;

//TODO: Replace with your package.

public class Exercise11Tester {
    static final Path graphFile = Paths.get("data/network_files/simple_network.edges");

    public static void main(String[] args) throws IOException {
	IExercise11 implementation = new Exercise11();

	Map<Integer, Double> betweennesses = implementation.getNodeBetweenness(graphFile);
	List<Entry<Integer, Double>> sortedBetweennesses = new ArrayList<>(betweennesses.entrySet());
	sortedBetweennesses.sort(new Comparator<Entry<Integer, Double>>() {
		@Override
		    public int compare(Entry<Integer, Double> o1, Entry<Integer, Double> o2) {
		    return (int) Math.signum(o2.getValue() - o1.getValue());
		}
	    });
	System.out.println("Network betweennesses:");
	System.out.println(sortedBetweennesses);
	System.out.println();
	
	
	List<Point> pointList = new ArrayList<>();
	
	for (int i = 0; i < sortedBetweennesses.size(); i++) {
		double betweennessValue = sortedBetweennesses.get(i).getValue();
		
		pointList.add(new Point(i,betweennessValue));
	}
	
	ChartPlotter.plotLines(pointList);
	
	
    }
}
