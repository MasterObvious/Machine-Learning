package uk.ac.cam.tal42.mlrwd.exercises.Exercise2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.*;

public class Exercise2 implements IExercise2 {
	

	@Override
	public Map<Sentiment, Double> calculateClassProbabilities(Map<Path, Sentiment> trainingSet) throws IOException {
		
		Map<Sentiment,Double> mClassProbabilities = new HashMap<>();
		
		
		int negTotal = 0;
		int posTotal = 0;
		
		for (Path p : trainingSet.keySet()) {
			if (trainingSet.get(p) == Sentiment.POSITIVE) {
				posTotal++;
			}else {
				negTotal++;
			}
		}
		
		mClassProbabilities.put(Sentiment.POSITIVE, (double) posTotal / trainingSet.size());
		mClassProbabilities.put(Sentiment.NEGATIVE, (double) negTotal / trainingSet.size());
		
		
		return mClassProbabilities;
	}

	@Override
	public Map<String, Map<Sentiment, Double>> calculateUnsmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
		
		Map<String, Map<Sentiment,Double>> mUnsmoothedLogProbs = new HashMap<>();
		
		Map<Sentiment,Double> totalWordCount = new HashMap<>();
		Map<String, Map<Sentiment,Double>> individualWordCount = new HashMap<>();
		
		//set init values for totalWordCount
		totalWordCount.put(Sentiment.POSITIVE, 0.0);
		totalWordCount.put(Sentiment.NEGATIVE, 0.0);
		
		for (Path p : trainingSet.keySet()) {
			
			List<String> review = Tokenizer.tokenize(p);
			totalWordCount.put(trainingSet.get(p), totalWordCount.get(trainingSet.get(p)) + review.size());
			
			for (String s : review) {
				Map<Sentiment,Double> wordMap;
				if (individualWordCount.get(s) == null) {
					wordMap = new HashMap<Sentiment,Double>();
					wordMap.put(Sentiment.POSITIVE, 0.0);
					wordMap.put(Sentiment.NEGATIVE, 0.0);
					individualWordCount.put(s, wordMap);
				}
				
				wordMap = individualWordCount.get(s);
				wordMap.put(trainingSet.get(p),wordMap.get(trainingSet.get(p)) + 1);
				
			}
			
		}
		
		for (String s : individualWordCount.keySet()) {
			Map<Sentiment,Double> wordProbabilities = new HashMap<>();
			wordProbabilities.put(Sentiment.POSITIVE, individualWordCount.get(s).get(Sentiment.POSITIVE) / totalWordCount.get(Sentiment.POSITIVE));
			wordProbabilities.put(Sentiment.NEGATIVE, individualWordCount.get(s).get(Sentiment.NEGATIVE) / totalWordCount.get(Sentiment.NEGATIVE));
			mUnsmoothedLogProbs.put(s, wordProbabilities);
			
			
		}
		
		return mUnsmoothedLogProbs;
		
		
	}

	@Override
	public Map<String, Map<Sentiment, Double>> calculateSmoothedLogProbs(Map<Path, Sentiment> trainingSet) throws IOException {
		
		double smoother = 1;
		
		Map<String, Map<Sentiment,Double>> mSmoothedLogProbs = new HashMap<>();
		
		Map<Sentiment,Double> totalWordCount = new HashMap<>();
		Map<String, Map<Sentiment,Double>> individualWordCount = new HashMap<>();
		
		//set init values for totalWordCount
		totalWordCount.put(Sentiment.POSITIVE, 0.0);
		totalWordCount.put(Sentiment.NEGATIVE, 0.0);
		
		for (Path p : trainingSet.keySet()) {
			
			List<String> review = Tokenizer.tokenize(p);
			totalWordCount.put(trainingSet.get(p), totalWordCount.get(trainingSet.get(p)) + review.size());
			
			for (String s : review) {
				Map<Sentiment,Double> wordMap;
				if (individualWordCount.get(s) == null) {
					wordMap = new HashMap<Sentiment,Double>();
					wordMap.put(Sentiment.POSITIVE, smoother);
					wordMap.put(Sentiment.NEGATIVE, smoother);
					individualWordCount.put(s, wordMap);
				}
				
				wordMap = individualWordCount.get(s);
				wordMap.put(trainingSet.get(p),wordMap.get(trainingSet.get(p)) + 1);
				
			}
			
		}
		
		for (String s : individualWordCount.keySet()) {
			Map<Sentiment,Double> wordProbabilities = new HashMap<>();
			wordProbabilities.put(Sentiment.POSITIVE, Math.log(individualWordCount.get(s).get(Sentiment.POSITIVE) / (totalWordCount.get(Sentiment.POSITIVE) + smoother * individualWordCount.size())));
			wordProbabilities.put(Sentiment.NEGATIVE, Math.log(individualWordCount.get(s).get(Sentiment.NEGATIVE) / (totalWordCount.get(Sentiment.NEGATIVE) + smoother * individualWordCount.size())));
			mSmoothedLogProbs.put(s, wordProbabilities);
			
			
			
		}
		
		return mSmoothedLogProbs;
	}

	@Override
	public Map<Path, Sentiment> naiveBayes(Set<Path> testSet, Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) throws IOException {
		
		Map<Path,Sentiment> mPredictedSentiments = new HashMap<>();
		
		for (Path p : testSet) {
			
			double posProb = Math.log(classProbabilities.get(Sentiment.POSITIVE));
			double negProb = Math.log(classProbabilities.get(Sentiment.NEGATIVE));
			
			List<String> review = Tokenizer.tokenize(p);
			
			for (String s : review) {
				
				if (tokenLogProbs.get(s) != null) {
					posProb += tokenLogProbs.get(s).get(Sentiment.POSITIVE);
					negProb += tokenLogProbs.get(s).get(Sentiment.NEGATIVE);
				}
				
			}
			
			if (posProb >= negProb) {
				mPredictedSentiments.put(p, Sentiment.POSITIVE);
			}else{
				mPredictedSentiments.put(p, Sentiment.NEGATIVE);
			}
			
		}
		
		return mPredictedSentiments;

	}
	
	public Sentiment naiveBayesInput(String reviewText ,Map<String, Map<Sentiment, Double>> tokenLogProbs, Map<Sentiment, Double> classProbabilities) {
		
		double posProb = Math.log(classProbabilities.get(Sentiment.POSITIVE));
		double negProb = Math.log(classProbabilities.get(Sentiment.NEGATIVE));
		
		List<String> review = Tokenizer.tokenize(reviewText);
	
		
		
		for (String s : review) {
			
			if (tokenLogProbs.get(s) != null) {
				posProb += tokenLogProbs.get(s).get(Sentiment.POSITIVE);
				
				negProb += tokenLogProbs.get(s).get(Sentiment.NEGATIVE);
			}
			
		}
		
		if (posProb >= negProb) {
			return Sentiment.POSITIVE;
		}else{
			return Sentiment.NEGATIVE;
		}
		
		
	}

}
