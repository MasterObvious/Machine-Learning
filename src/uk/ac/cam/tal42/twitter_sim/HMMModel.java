package uk.ac.cam.tal42.twitter_sim;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HMMModel {

	Map<String,Map<String, Double>> mTransitionMatrix;
	
	public HMMModel(Map<String,Map<String,Double>> transitionProbs ) {

		mTransitionMatrix = transitionProbs;
	}


	public Map<String, Map<String, Double>> getTransitionMatrix() {
		return mTransitionMatrix;
	}
	
	public String generateTweet() {
		
		String computedTweet = "";
		
		String currentWord = "START_STATE";
		
		while(currentWord != "END_STATE") {
			
			if (currentWord != "START_STATE") {
				computedTweet += currentWord + " ";
			}
			
			currentWord = getNextWord(mTransitionMatrix.get(currentWord));
			
		}
		
		return computedTweet;
	}
	
	public String generateSecondOrderTweet() {
		
		
		String computedTweet = "";
		
		String currentWord = "START_STATE";
		String previousWord = "";
		
		while(currentWord != "END_STATE") {
			
			if (currentWord != "START_STATE") {
				computedTweet += currentWord + " ";
			}
			
			String temp = currentWord;
			currentWord = getNextWord(mTransitionMatrix.get(currentWord + previousWord));
			previousWord = temp;
			
		}
		
		return computedTweet;
	}
	
	public String getNextWord(Map<String,Double> transitionProbs) {
		
		
		List<SimpleEntry<String,Double>> probsList = new ArrayList<>();
		
		int count = 0;
		
		
		
		for (Entry<String,Double> entry : transitionProbs.entrySet()) {
			probsList.add(count, new SimpleEntry<String,Double>(entry));
			
			if (count != 0) {
				probsList.get(count).setValue(probsList.get(count-1).getValue() + probsList.get(count).getValue());
			}
			
			count++;
		}
		
		
		Double randomValue = Math.random();
		String nextWord = probsList.get(0).getKey();
		for (int i = 0; i < probsList.size() - 1; i++) {
			
			if (randomValue > probsList.get(i).getValue() && randomValue < probsList.get(i + 1).getValue()) {
				nextWord = probsList.get(i).getKey();
			}
			
		}
		
		return nextWord;
	}
	
	
	
}
