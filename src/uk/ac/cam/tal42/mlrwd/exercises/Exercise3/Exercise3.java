package uk.ac.cam.tal42.mlrwd.exercises.Exercise3;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.cl.mlrwd.utils.ChartPlotter;
import uk.ac.cam.cl.mlrwd.utils.BestFit;



public class Exercise3 {
	
	private double mGradient;
	private double mIntercept;
	
	

	public static void main(String[] args) throws IOException {
		Path dataSetDirectory = Paths.get("data/sentiment_dataset/large_dataset");
		
		Set<Path> mPathSet = null;
		List<WordEntry> mWordList = null;
		List<BestFit.Point> mPointList = new ArrayList<>();
		Map<BestFit.Point,Double> bestLineMap = new HashMap<>();
		
		Exercise3 implementation = new Exercise3();
		
		try {
			mPathSet = implementation.getPathList(dataSetDirectory);
			System.out.println("Generated path set");
			mWordList = implementation.getWordFrequencies(mPathSet);
			System.out.println("Got word list");
		}catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
		
		for (int i = 0; i < 10000; i++) {
			WordEntry w = mWordList.get(i);
			BestFit.Point p = new BestFit.Point(Math.log10(w.getRank()),Math.log10(w.getCount()));
			
			mPointList.add(p);
			bestLineMap.put(p, (double) w.getCount());
		}
		
		List<BestFit.Point> bestFitPoints = new ArrayList<>();
		BestFit.Line bestFitLine = BestFit.leastSquares(bestLineMap);
		bestFitPoints.add(new BestFit.Point(0, bestFitLine.yIntercept));
		bestFitPoints.add(new BestFit.Point(4,bestFitLine.yIntercept + 4 * bestFitLine.gradient)); 
		
		implementation.setLineValues(bestFitLine);
		ChartPlotter.plotLines(mPointList, bestFitPoints);
		implementation.estimateValues();
		
		int[] myRanks = {62,78,160,226,258,676,867,1872,3508,19024};
		
		for(int i : myRanks) {
			System.out.println(implementation.estimateFrequency(i));
		}
		
		implementation.generateHeapGraph(mPathSet);
		
		
	}
	
	
	public void setLineValues(BestFit.Line bestLine) {
		mGradient = bestLine.gradient;
		mIntercept = bestLine.yIntercept;
	}
	
	public double estimateFrequency(int rank) {
		
		double estimate = 0.0;
		estimate = mIntercept + mGradient * Math.log10(rank);
		estimate = Math.pow(10, estimate);
		
		return estimate;
	}
	
	public void estimateValues() {
		System.out.println("k is approximately " + Math.pow(10, mIntercept));
		System.out.println("alpha is approximately " + mGradient * -1);
	}
	
	
	public Set<Path> getPathList(Path datasetDirectory) throws IOException {
		//Get a set of all files in the datasetDirectory
		
		Set<Path> dataset = new HashSet<>();
		
		try (DirectoryStream<Path> files = Files.newDirectoryStream(datasetDirectory)) {
			for (Path item : files) {
				dataset.add(item);
			}
		} catch (IOException e) {
			throw new IOException("Can't read the reviews.", e);
		}
		
		return dataset;
	}
	
	public List<WordEntry> getWordFrequencies(Set<Path> dataSet) throws IOException {
		
		Map<String,Integer> wordMap = new HashMap<>();
		List<WordEntry> entryList = new ArrayList<>();
		
		
		for (Path p : dataSet) {
			
			//System.out.println("Read: " + p);
			
			
			List<String> wordList = Tokenizer.tokenize(p);
			
			for (String s : wordList) {
				if (!wordMap.containsKey(s)) {
					wordMap.put(s, 1);
				}else{
					wordMap.put(s, wordMap.get(s) + 1);
				}
					
			}
			
		}
		
		
		
		for (String s : wordMap.keySet()) {
			entryList.add(new WordEntry(s,wordMap.get(s)));
		}
		
		entryList = entryList.stream().sorted().collect(Collectors.toList());
		
		for (int i = 0; i < entryList.size(); i++) {
			WordEntry w = entryList.get(i);
			w.setRank(i + 1);
		}
		
		return entryList;
	}
	
	
	public void generateHeapGraph(Set<Path> pathSet) throws IOException {
		int tokenCount = 0;
		Set<String> typeSet = new HashSet<>();
		List<BestFit.Point> heapPoints = new ArrayList<>();
		
		for (Path p : pathSet) {
			List<String> tokens = Tokenizer.tokenize(p);
			for (String s : tokens) {
				typeSet.add(s);
				tokenCount++;
				
				if ((tokenCount & (tokenCount - 1)) == 0) {
					heapPoints.add(new BestFit.Point(Math.log10((double) tokenCount), Math.log10((double) typeSet.size())));
				}
				
			}
		}
		
		heapPoints.add(new BestFit.Point(Math.log10((double) tokenCount), Math.log10((double) typeSet.size())));
		ChartPlotter.plotLines(heapPoints);
	}
	
	

	

}
