package uk.ac.cam.tal42.mlrwd.exercises.Exercise5;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise5;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise1.Exercise1;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise2.Exercise2;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;

public class Exercise5 implements IExercise5 {

	@Override
	public List<Map<Path, Sentiment>> splitCVRandom(Map<Path, Sentiment> dataSet, int seed) {
		// TODO Auto-generated method stub
		
		List<Path> pathList = dataSet.keySet().stream().collect(Collectors.toList());
		List<Map<Path,Sentiment>> dataSplit = new ArrayList<>();
		
		Collections.shuffle(pathList, new Random(seed));
		Map<Path,Sentiment> currentSplit = new HashMap<>();
		int pathCount = 0;
		
		for (Path p : pathList) {
			
			currentSplit.put(p, dataSet.get(p));
			pathCount++;
			
			if (pathCount >= pathList.size() / 10) {
				pathCount = 0;
				dataSplit.add(currentSplit);
				currentSplit = new HashMap<>();
			}
			
		}
		
		return dataSplit;
		
	}

	@Override
	public List<Map<Path, Sentiment>> splitCVStratifiedRandom(Map<Path, Sentiment> dataSet, int seed) {
		// TODO Auto-generated method stub
		
		List<Path> posPathList = dataSet.keySet().stream().filter(p -> dataSet.get(p).equals(Sentiment.POSITIVE)).collect(Collectors.toList());
		List<Path> negPathList = dataSet.keySet().stream().filter(p -> dataSet.get(p).equals(Sentiment.NEGATIVE)).collect(Collectors.toList());
		List<Map<Path,Sentiment>> dataSplit = new ArrayList<>();
		
		Collections.shuffle(posPathList, new Random(seed));
		Collections.shuffle(negPathList, new Random(seed));
		
		Map<Path,Sentiment> currentSplit = new HashMap<>();
		int pathCount = 0;
		
		for (int i = 0; i < dataSet.keySet().size() / 2; i++) {
			currentSplit.put(posPathList.get(i), Sentiment.POSITIVE);
			currentSplit.put(negPathList.get(i), Sentiment.NEGATIVE);
			pathCount += 2;
			
			if (pathCount >= dataSet.keySet().size() / 10) {
				pathCount = 0;
				dataSplit.add(currentSplit);
				currentSplit = new HashMap<>();
			}
		}
		
		return dataSplit;
		
	}


	@Override
	public double[] crossValidate(List<Map<Path, Sentiment>> folds) throws IOException {
		// TODO Auto-generated method stub
		
		double[] accuracies = new double[10];
		
		for (int currentTestSetIndex = 0; currentTestSetIndex < 10; currentTestSetIndex++) {
			
			Map<Path,Sentiment> trainingSet = new HashMap<>();
			Map<Path,Sentiment> testSet = new HashMap<>();
			
			for (int i = 0; i < 10; i++) {
				if(i == currentTestSetIndex) {
					testSet = folds.get(i);
				}else{
					trainingSet.putAll(folds.get(i));
				}
			}
			
			Exercise2 NaiveBayes = new Exercise2();
			
			Map<Sentiment,Double> classProbs = NaiveBayes.calculateClassProbabilities(trainingSet);
			Map<String,Map<Sentiment,Double>> logProbs = NaiveBayes.calculateSmoothedLogProbs(trainingSet);
			
			Map<Path,Sentiment> predictions = NaiveBayes.naiveBayes(testSet.keySet(), logProbs, classProbs);
			
			Exercise1 simpleStuff = new Exercise1();
			
			accuracies[currentTestSetIndex] = simpleStuff.calculateAccuracy(testSet, predictions);
			
		}
		
		
		return accuracies;
	}

	@Override
	public double cvAccuracy(double[] scores) {
		// TODO Auto-generated method stub
		double total = 0;
		for (double d : scores) {
			total += d;
		}
		
		return total / scores.length;
	}

	@Override
	public double cvVariance(double[] scores) {
		// TODO Auto-generated method stub
		
		double average = cvAccuracy(scores);
		double total = 0;
		
		for (double d : scores) {
			total += Math.pow((d - average), 2);
		}
		
		return total / scores.length;
	}

}
