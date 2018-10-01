package uk.ac.cam.tal42.mlrwd.exercises.Exercise4;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.IExercise4;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;

public class Exercise4 implements IExercise4 {
	
	Map<String,Integer> mWords = new HashMap<>();

	@Override
	public Map<Path, Sentiment> magnitudeClassifier(Set<Path> testSet, Path lexiconFile) throws IOException {
		// TODO Auto-generated method stub
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
	public double signTest(Map<Path, Sentiment> actualSentiments, Map<Path, Sentiment> classificationA,
			Map<Path, Sentiment> classificationB) {
		// TODO Auto-generated method stub
		
		BigDecimal pValue = BigDecimal.valueOf(0);
		double q = 0.5;
		
		int plus = 0;
		int minus = 0;
		int tie = 0;
		
		
		for (Path p : actualSentiments.keySet()) {
			Sentiment ASentiment = classificationA.get(p);
			Sentiment BSentiment = classificationB.get(p);
			
			if (ASentiment.equals(BSentiment)) {
				tie++;
			}else if (ASentiment.equals(actualSentiments.get(p))) {
				plus++;
			}else {
				minus++;
			}
		}
		
		double n = 2*Math.ceil(tie / 2.0) + minus + plus;
		double k = Math.ceil( tie / 2.0) + Math.min(minus, plus);
	
		
		BigDecimal power = BigDecimal.valueOf(q);
		power = power.pow((int)n);

		
		
		for (int i = 0; i <= k; i++) {
			
			pValue = pValue.add(binomial((int)n,i).multiply(power));
			
		}
		
		
		return pValue.doubleValue() * 2;
	}
	
	private void addToSet(String lexiconLine) {
		if (lexiconLine.startsWith("type=")) {
			
			int strength = 1;
			if (lexiconLine.contains("type=strongsubj")) {
				strength = 2;
			}
			
			int position;
			position = lexiconLine.indexOf("word1=");
			lexiconLine = lexiconLine.substring(position + 6);
			position = lexiconLine.indexOf(" ");
			String word = lexiconLine.substring(0, position);
			
			
			
						
			if (lexiconLine.contains("priorpolarity=positive")) {
				mWords.put(word,strength);
			}else if(lexiconLine.contains("priorpolarity=negative")) {
				mWords.put(word,-1 * strength);
			}
		}
		
	}
	
	private BigDecimal binomial(final int N, final int K) {
	    BigDecimal nfact = factorial(N);
	    BigDecimal kfact = factorial(K);
	    BigDecimal nkfact = factorial(N-K);
	    
	    BigDecimal result = nfact.divide(nkfact.multiply(kfact));
	    return result;
	}
	
	private BigDecimal factorial(int n) {
		BigDecimal result = BigDecimal.valueOf(1);
		
		for (int i = 1; i <= n ; i++) {
			result = result.multiply(BigDecimal.valueOf(i));
		}
		
		return result;
	}

}
