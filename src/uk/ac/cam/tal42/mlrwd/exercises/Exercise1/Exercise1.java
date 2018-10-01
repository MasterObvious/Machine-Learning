package uk.ac.cam.tal42.mlrwd.exercises.Exercise1;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise1;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;

import java.io.*;

public class Exercise1 implements IExercise1 {
	
	private HashSet<String> mPositiveWords = new HashSet<>();
	private HashSet<String> mNegativeWords = new HashSet<>();
	
	private HashMap<String,Integer> mWords = new HashMap<>();

	@Override
	public Map<Path, Sentiment> simpleClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
		// TODO Auto-generated method stub
		// Load Positive and Negative Words from lexicon
		
		Map<Path,Sentiment> reviewSentiments = new HashMap<Path,Sentiment>();
		
		BufferedReader br = new BufferedReader(new FileReader(lexiconFile.toFile()));
		String line;
		
		while ((line = br.readLine()) != null) {
			addToSet(line);
		}
		br.close();
		
		for(Path p : testSet) {
			
			int sentimentScore = 0;
			
			List<String> reviewWords = Tokenizer.tokenize(p);
			
			for(String s : reviewWords) {
				if (mWords.containsKey(s)){
					sentimentScore += mWords.get(s);
				}
			}
			
			if (sentimentScore < 0) {
				reviewSentiments.put(p, Sentiment.NEGATIVE);
			}else{
				reviewSentiments.put(p, Sentiment.POSITIVE);
			}
		}
		
		return reviewSentiments;
	}

	@Override
	public double calculateAccuracy(Map<Path, Sentiment> trueSentiments, Map<Path, Sentiment> predictedSentiments) {
		// TODO Auto-generated method stub
		
		int correctValues = 0;
		
		for (Path p : predictedSentiments.keySet()){
			if (trueSentiments.get(p) == predictedSentiments.get(p)) {
				correctValues++;
			}
		}
		
		return (double) correctValues /  predictedSentiments.size();
	}

	@Override
	public Map<Path, Sentiment> improvedClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
		// TODO Auto-generated method stub
				// Load Positive and Negative Words from lexicon
				
				Map<Path,Sentiment> reviewSentiments = new HashMap<Path,Sentiment>();
				
				BufferedReader br = new BufferedReader(new FileReader(lexiconFile.toFile()));
				String line;
				
				while ((line = br.readLine()) != null) {
					addToImprovedSet(line);
				}
				br.close();
				
				for(Path p : testSet) {
					
					double sentimentScore = 0;
					
					List<String> reviewWords = Tokenizer.tokenize(p);
					
					for(int i = 0; i < reviewWords.size() ; i++) {
						
						String s = reviewWords.get(i);
						
						
						int startScore = reviewWords.size() / 25;
						int endScore = reviewWords.size() / 30;
						double deltaScore = 0;
						int notFinder = 5;
						
						if (mPositiveWords.contains(s)) {
							deltaScore = 1.25; //1.25
						} else if (mNegativeWords.contains(s) ) {
							deltaScore = -1.71; //1.71
							
						}
				
						
						if (mPositiveWords.contains(s) && mNegativeWords.contains(s)) {
							deltaScore = 0;
						}
						
						if(i>0 && (reviewWords.get(i-1).equals("very") || reviewWords.get(i-1).equals("really") || reviewWords.get(i-1).equals("extremely") || reviewWords.get(i-1).equals("terribly"))) {
							deltaScore *=10.65;
						}
						
						if(i>1 && (reviewWords.get(i-2).equals("really") || reviewWords.get(i-2).equals("very"))) {
							deltaScore *=1.7;
						}
						
						
						int notCount = 0;
						
						for (int j = i-1; (j>=0 && j >= i-notFinder) ; j--) {
							if ((reviewWords.get(j).equals("not") || reviewWords.get(j).contains("n't"))) {
								notCount++;
							}
						}
						
						if (notCount % 2 == 1) {
							deltaScore *= -1;
						}
						
						if ((i < startScore || i > reviewWords.size() - endScore)) {
							deltaScore *=2;
						}
						sentimentScore += deltaScore;
						
					}
					
					
				
					
						if (sentimentScore < 0) {
							reviewSentiments.put(p, Sentiment.NEGATIVE);
						}else{
							reviewSentiments.put(p, Sentiment.POSITIVE);
						}
				}
				
				return reviewSentiments;
	}
	
	
	private void addToSet(String lexiconLine) {
		if (lexiconLine.startsWith("type=")) {
			int position;
			position = lexiconLine.indexOf("word1=");
			lexiconLine = lexiconLine.substring(position + 6);
			position = lexiconLine.indexOf(" ");
			String word = lexiconLine.substring(0, position);
			if (lexiconLine.contains("priorpolarity=positive")) {
				mWords.put(word,1);
			}else if(lexiconLine.contains("priorpolarity=negative")) {
				mWords.put(word,-1);
			}
		}
		
	}
	
	private void addToImprovedSet(String lexiconLine) {
		if (lexiconLine.startsWith("type=")) {
			int position;
			position = lexiconLine.indexOf("word1=");
			lexiconLine = lexiconLine.substring(position + 6);
			position = lexiconLine.indexOf(" ");
			String word = lexiconLine.substring(0, position);
			if (lexiconLine.contains("priorpolarity=positive")) {
				mPositiveWords.add(word);
			}else if(lexiconLine.contains("priorpolarity=negative")) {
				mNegativeWords.add(word);
			}
		}
		
	}
	


}
