package uk.ac.cam.tal42.mlrwd.exercises.Exercise6;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise6;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.NuancedSentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise1.Exercise1;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise2.Exercise2;

public class Exercise6 implements IExercise6 {

	@Override
	public Map<NuancedSentiment, Double> calculateClassProbabilities(Map<Path, NuancedSentiment> trainingSet) throws IOException {
		
		Map<NuancedSentiment, Double> classProbs = new HashMap<>();
		
		for (Path p : trainingSet.keySet()) {
			NuancedSentiment s = trainingSet.get(p);
			if (classProbs.containsKey(s)) {
				classProbs.put(s,classProbs.get(s) + 1);
			}else{
				classProbs.put(s, 1.0);
			}
			
		}
		
		for (NuancedSentiment s : classProbs.keySet()) {
			classProbs.put(s, classProbs.get(s) / trainingSet.size());
		}
		
		return classProbs;
	}

	@Override
	public Map<String, Map<NuancedSentiment, Double>> calculateNuancedLogProbs(Map<Path, NuancedSentiment> trainingSet) throws IOException {
		
		double smoother = 1;
		
		Map<String, Map<NuancedSentiment,Double>> mSmoothedLogProbs = new HashMap<>();
		
		Map<NuancedSentiment,Double> totalWordCount = new HashMap<>();
		Map<String, Map<NuancedSentiment,Double>> individualWordCount = new HashMap<>();
		
		//set init values for totalWordCount
		
		for (NuancedSentiment s : NuancedSentiment.values()) {
			totalWordCount.put(s, 0.0);
		}
		
		
		for (Path p : trainingSet.keySet()) {
			
			List<String> review = Tokenizer.tokenize(p);
			totalWordCount.put(trainingSet.get(p), totalWordCount.get(trainingSet.get(p)) + review.size());
			
			for (String s : review) {
				Map<NuancedSentiment,Double> wordMap;
				if (individualWordCount.get(s) == null) {
					wordMap = new HashMap<NuancedSentiment,Double>();
					
					for (NuancedSentiment nS : NuancedSentiment.values()) {
						wordMap.put(nS, smoother);
					}
					
					
					individualWordCount.put(s, wordMap);
				}
				
				wordMap = individualWordCount.get(s);
				wordMap.put(trainingSet.get(p),wordMap.get(trainingSet.get(p)) + 1);
				
			}
			
		}
		
		for (String s : individualWordCount.keySet()) {
			Map<NuancedSentiment,Double> wordProbabilities = new HashMap<>();
			
			for (NuancedSentiment nS : NuancedSentiment.values()) {
				wordProbabilities.put(nS, Math.log(individualWordCount.get(s).get(nS) / (totalWordCount.get(nS) + smoother * individualWordCount.size())));
			}
			
			mSmoothedLogProbs.put(s, wordProbabilities);
			
			
			
		}
		
		return mSmoothedLogProbs;
	}

	@Override
	public Map<Path, NuancedSentiment> nuancedClassifier(Set<Path> testSet, Map<String, Map<NuancedSentiment, Double>> tokenLogProbs, Map<NuancedSentiment, Double> classProbabilities) throws IOException {
		
		Map<Path,NuancedSentiment> mPredictedSentiments = new HashMap<>();
		
		for (Path p : testSet) {
			
			double posProb = Math.log(classProbabilities.get(NuancedSentiment.POSITIVE));
			double negProb = Math.log(classProbabilities.get(NuancedSentiment.NEGATIVE));
			double neutProb = Math.log(classProbabilities.get(NuancedSentiment.NEUTRAL));
			
			List<String> review = Tokenizer.tokenize(p);
			
			for (String s : review) {
				
				if (tokenLogProbs.get(s) != null) {
					posProb += tokenLogProbs.get(s).get(NuancedSentiment.POSITIVE);
					negProb += tokenLogProbs.get(s).get(NuancedSentiment.NEGATIVE);
					neutProb += tokenLogProbs.get(s).get(NuancedSentiment.NEUTRAL);
				}
				
			}
			
			if (posProb >= negProb && posProb >= neutProb) {
				mPredictedSentiments.put(p, NuancedSentiment.POSITIVE);
			}else if (negProb >= posProb && negProb >= neutProb){
				mPredictedSentiments.put(p, NuancedSentiment.NEGATIVE);
			}else {
				mPredictedSentiments.put(p, NuancedSentiment.NEUTRAL);
			
		}
		
		

		}
		
		return mPredictedSentiments;
	}

	@Override
	public double nuancedAccuracy(Map<Path, NuancedSentiment> trueSentiments,
			Map<Path, NuancedSentiment> predictedSentiments) {
		// TODO Auto-generated method stub
		
		double correctTotal = 0.0;
		
		for (Path p : predictedSentiments.keySet()) {
			if (predictedSentiments.get(p).equals(trueSentiments.get(p))) {
				correctTotal++;
			}
		}
		
		return correctTotal / trueSentiments.size();
	}

	@Override
	public Map<Integer, Map<Sentiment, Integer>> agreementTable(
			Collection<Map<Integer, Sentiment>> predictedSentiments) {
		// TODO Auto-generated method stub
		
		Map<Integer, Map<Sentiment,Integer>> mAgreementTable = new HashMap<>();
		
		
		for (int i = 1 ; i <=4; i++) {
			Map<Sentiment,Integer> tableColumn = new HashMap<>();
			mAgreementTable.put(i, tableColumn);
		}
		
		for (Map<Integer,Sentiment> m : predictedSentiments) {
			
			for (Integer i : m.keySet()) {
				Map<Sentiment,Integer> tableColumn = mAgreementTable.get(i);
				if (tableColumn.containsKey(m.get(i))) {
					tableColumn.put(m.get(i), tableColumn.get(m.get(i)) + 1);
				}else{
					tableColumn.put(m.get(i), 1);
				}
				
			}
			
		}
		
		return mAgreementTable;
	}

	@Override
	public double kappa(Map<Integer, Map<Sentiment, Integer>> agreementTable) {
		// TODO Auto-generated method stub
		
		double Pa = 0;
		double Pe = 0;
		
		Map<Integer, Double> totals = new HashMap<>();
		
		for (Integer i : agreementTable.keySet()) {
			double total = 0;
			
			for (Sentiment s : Sentiment.values()) {
				
				
				if (agreementTable.get(i).containsKey(s)) {
					total += agreementTable.get(i).get(s);
				}
			}
			
			totals.put(i, total);
		}
		
		
		
		
		
		for (Sentiment s : Sentiment.values()) {
			
			double proportions = 0;
			for (Integer i : agreementTable.keySet()) {
				
				double totalPredictions = totals.get(i);
				
				
				if (agreementTable.get(i).containsKey(s)) {
					proportions += agreementTable.get(i).get(s) / totalPredictions;
				}
				
				
			}
			
			Pe += Math.pow(proportions / agreementTable.size(), 2);
			
		}
		
		
		
		for (Integer i : agreementTable.keySet()) {
			
			double summation = 0;
			double totalPredictions = totals.get(i);
			
			for (Sentiment s : Sentiment.values()) {
				
				if (agreementTable.get(i).containsKey(s)) {
					summation += agreementTable.get(i).get(s) * (agreementTable.get(i).get(s) - 1);
				}
				
				
			}
			
			Pa += summation / (totalPredictions * (totalPredictions - 1));
			
		}
		
		Pa = Pa / agreementTable.size();
		
		double kappa = (Pa - Pe) / (1 - Pe);
		
		return kappa;
	}
	
	public List<Map<Path, NuancedSentiment>> splitCVRandom(Map<Path, NuancedSentiment> dataSet, int seed) {
		// TODO Auto-generated method stub
		
		List<Path> pathList = dataSet.keySet().stream().collect(Collectors.toList());
		List<Map<Path,NuancedSentiment>> dataSplit = new ArrayList<>();
		
		Collections.shuffle(pathList, new Random(seed));
		Map<Path,NuancedSentiment> currentSplit = new HashMap<>();
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
	
	public double[] crossValidate(List<Map<Path, NuancedSentiment>> folds) throws IOException {
		// TODO Auto-generated method stub
		
		double[] accuracies = new double[10];
		
		for (int currentTestSetIndex = 0; currentTestSetIndex < 10; currentTestSetIndex++) {
			
			Map<Path,NuancedSentiment> trainingSet = new HashMap<>();
			Map<Path,NuancedSentiment> testSet = new HashMap<>();
			
			for (int i = 0; i < 10; i++) {
				if(i == currentTestSetIndex) {
					testSet = folds.get(i);
				}else{
					trainingSet.putAll(folds.get(i));
				}
			}
			
			
			Map<NuancedSentiment,Double> classProbs = calculateClassProbabilities(trainingSet);
			Map<String,Map<NuancedSentiment,Double>> logProbs = calculateNuancedLogProbs(trainingSet);
			
			Map<Path,NuancedSentiment> predictions = nuancedClassifier(testSet.keySet(), logProbs, classProbs);
			
			accuracies[currentTestSetIndex] = nuancedAccuracy(predictions , testSet);
		}
		
		
		return accuracies;
	}

	public double cvAccuracy(double[] scores) {
		// TODO Auto-generated method stub
		double total = 0;
		for (double d : scores) {
			total += d;
		}
		
		return total / scores.length;
	}

}
