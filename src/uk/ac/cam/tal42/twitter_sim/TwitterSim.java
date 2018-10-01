package uk.ac.cam.tal42.twitter_sim;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Tokenizer;

public class TwitterSim {

	public static void main(String[] args) throws TwitterException, IOException {
		// TODO Auto-generated method stub
		
		List<Status> tweetList = getTweetList("Master_Obvious");
		System.out.println("Loaded Tweets");
		System.out.println(tweetList.size());
		
		for (Status s : tweetList) {
			System.out.println(s.getText());
		}
		
		/*
		HMMModel trumpHMM = generateSecondOrderHMMJSON(tweetList);
		System.out.println("Generated HMM");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		while(true) {"
			System.out.println(trumpHMM.generateSecondOrderTweet());
			in.readLine();
		}
		*/
		
	}
	
	public static List<Status> getTweetList(String user) throws TwitterException {
		
		//set up access tokens
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("***REMOVED***")
		  .setOAuthConsumerSecret("***REMOVED***")
		  .setOAuthAccessToken("***REMOVED***")
		  .setOAuthAccessTokenSecret("***REMOVED***");
		
		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		
		
		
		List<Status> tweetList = new ArrayList<>();
		
		
		//get tweets from user
		int size = -1;
		int pageNo = 1;
		
		//while loop continues to grab tweets from pages until there are no more pages (the number of tweets on a page is 0)
		while (size != tweetList.size()) {
			size = tweetList.size();
			Paging page = new Paging(pageNo++, 100);
	        tweetList.addAll(twitter.getUserTimeline(user, page));
		}
		//return the tweet list without retweets
		return tweetList = tweetList.stream().filter(tweet -> !(tweet.isRetweet())).collect(Collectors.toList());
	}
	
	public static HMMModel generateHMM(List<Status> tweetList) {
		
		Map<String, Map<String,Double>> transitions = new HashMap<>();
		Map<String, Integer> transitionTotals = new HashMap<>();
		
		transitions.put("START_STATE", new HashMap<>());
		transitionTotals.put("START_STATE",tweetList.size());
		
		for (Status tweet : tweetList) {
			
			
			List<String> words = Tokenizer.tokenize(tweet.getText());
			
			
			Map<String,Double> initProbs = transitions.get("START_STATE");
			String firstWord = words.get(0);
			
			if (initProbs.containsKey(firstWord)) {
				initProbs.put(firstWord, initProbs.get(firstWord) + 1);
			}else{
				initProbs.put(firstWord, 1.0);
			}
			
			
			
			
			
			
			for (int i = 0; i < words.size(); i++) {
				
				
				String from = words.get(i);

				String to;
				
				if (i == words.size() - 1) {
					to = "END_STATE";
				}else{
					to = words.get(i+1);
				}
				
				Map<String, Double> transitionRow;
				if (!transitions.containsKey(from)) {
					transitionRow = new HashMap<>();
					transitions.put(from, transitionRow);
					transitionTotals.put(from, 0);
				}
				
				transitionTotals.put(from, transitionTotals.get(from) + 1);
				
				transitionRow = transitions.get(from);
				
				if (transitionRow.containsKey(to)) {
					transitionRow.put(to, transitionRow.get(to) + 1);
				}else{
					transitionRow.put(to, 1.0);
				}
			}
			
			
		}
		
		
		
		for (String from : transitions.keySet()) {
			
			Map<String,Double> row = transitions.get(from);
			
			for (String s : row.keySet()) {
				row.put(s, row.get(s) / transitionTotals.get(from));
			}
			
		}
		return new HMMModel(transitions);
	}
	
	public static HMMModel generateSecondOrderHMM(List<Status> tweetList) {
		
		
		Map<String, Map<String,Double>> transitions = new HashMap<>();
		Map<String, Integer> transitionTotals = new HashMap<>();
		
		transitions.put("START_STATE", new HashMap<>());
		transitionTotals.put("START_STATE",tweetList.size());
		
		for (Status tweet : tweetList) {
			
			
			List<String> words = Tokenizer.tokenize(tweet.getText());
			
			
			Map<String,Double> initProbs = transitions.get("START_STATE");
			String firstWord = words.get(0);
			
			if (initProbs.containsKey(firstWord)) {
				initProbs.put(firstWord, initProbs.get(firstWord) + 1);
			}else{
				initProbs.put(firstWord, 1.0);
			}
			
			
			
			
			
			
			for (int i = 0; i < words.size(); i++) {
				
				
				String from = words.get(i);
				
				if (i == 0) {
					from += "START_STATE";
				}else{
					from += words.get(i-1);
				}
				
				String to;
				
				if (i == words.size() - 1) {
					to = "END_STATE";
				}else{
					to = words.get(i+1);
				}
				
				Map<String, Double> transitionRow;
				if (!transitions.containsKey(from)) {
					transitionRow = new HashMap<>();
					transitions.put(from, transitionRow);
					transitionTotals.put(from, 0);
				}
				
				transitionTotals.put(from, transitionTotals.get(from) + 1);
				
				transitionRow = transitions.get(from);
				
				if (transitionRow.containsKey(to)) {
					transitionRow.put(to, transitionRow.get(to) + 1);
				}else{
					transitionRow.put(to, 1.0);
				}
			}
			
			
		}
		
		
		
		for (String from : transitions.keySet()) {
			
			Map<String,Double> row = transitions.get(from);
			
			for (String s : row.keySet()) {
				row.put(s, row.get(s) / transitionTotals.get(from));
			}
			
		}
		return new HMMModel(transitions);
	}
	
	
	public static List<Tweet> loadTweetsFromJSON() throws FileNotFoundException {
		String genericFileLocation = "data/Trump Tweets/trump_";
		
		List<Tweet> tweetList = new ArrayList<>();
		
		for (int i = 2009;i <=2017; i++) {
			String fileLocation = genericFileLocation + i + ".json";
			Gson gson = new Gson();
			
			BufferedReader br = new BufferedReader(new FileReader(Paths.get(fileLocation).toFile()));
			JsonReader reader = new JsonReader(br);
			//reader.setLenient(true);
			
			Type type = new TypeToken<List<Tweet>>(){}.getType();
			
	        tweetList.addAll(gson.fromJson(reader, type));
		}
		System.out.println(tweetList.size());
		return tweetList;
	}
	
	public static HMMModel generateHMMJSON(List<Tweet> tweetList) {
		
		Map<String, Map<String,Double>> transitions = new HashMap<>();
		Map<String, Integer> transitionTotals = new HashMap<>();
		
		transitions.put("START_STATE", new HashMap<>());
		transitionTotals.put("START_STATE",tweetList.size());
		
		for (Tweet tweet : tweetList) {
			
			
			List<String> words = Tokenizer.tokenize(tweet.getText());
			
			
			Map<String,Double> initProbs = transitions.get("START_STATE");
			String firstWord = words.get(0);
			
			if (initProbs.containsKey(firstWord)) {
				initProbs.put(firstWord, initProbs.get(firstWord) + 1);
			}else{
				initProbs.put(firstWord, 1.0);
			}
			
			
			
			
			
			
			for (int i = 0; i < words.size(); i++) {
				
				
				String from = words.get(i);

				String to;
				
				if (i == words.size() - 1) {
					to = "END_STATE";
				}else{
					to = words.get(i+1);
				}
				
				Map<String, Double> transitionRow;
				if (!transitions.containsKey(from)) {
					transitionRow = new HashMap<>();
					transitions.put(from, transitionRow);
					transitionTotals.put(from, 0);
				}
				
				transitionTotals.put(from, transitionTotals.get(from) + 1);
				
				transitionRow = transitions.get(from);
				
				if (transitionRow.containsKey(to)) {
					transitionRow.put(to, transitionRow.get(to) + 1);
				}else{
					transitionRow.put(to, 1.0);
				}
			}
			
			
		}
		
		
		
		for (String from : transitions.keySet()) {
			
			Map<String,Double> row = transitions.get(from);
			
			for (String s : row.keySet()) {
				row.put(s, row.get(s) / transitionTotals.get(from));
			}
			
		}
		return new HMMModel(transitions);
	}
	
	public static HMMModel generateSecondOrderHMMJSON(List<Tweet> tweetList) {
		
		
		Map<String, Map<String,Double>> transitions = new HashMap<>();
		Map<String, Integer> transitionTotals = new HashMap<>();
		
		transitions.put("START_STATE", new HashMap<>());
		transitionTotals.put("START_STATE",tweetList.size());
		
		for (Tweet tweet : tweetList) {
			
			
			List<String> words = Tokenizer.tokenize(tweet.getText());
			
			
			Map<String,Double> initProbs = transitions.get("START_STATE");
			String firstWord = words.get(0);
			
			if (initProbs.containsKey(firstWord)) {
				initProbs.put(firstWord, initProbs.get(firstWord) + 1);
			}else{
				initProbs.put(firstWord, 1.0);
			}
			
			
			
			
			
			
			for (int i = 0; i < words.size(); i++) {
				
				
				String from = words.get(i);
				
				if (i == 0) {
					from += "START_STATE";
				}else{
					from += words.get(i-1);
				}
				
				String to;
				
				if (i == words.size() - 1) {
					to = "END_STATE";
				}else{
					to = words.get(i+1);
				}
				
				Map<String, Double> transitionRow;
				if (!transitions.containsKey(from)) {
					transitionRow = new HashMap<>();
					transitions.put(from, transitionRow);
					transitionTotals.put(from, 0);
				}
				
				transitionTotals.put(from, transitionTotals.get(from) + 1);
				
				transitionRow = transitions.get(from);
				
				if (transitionRow.containsKey(to)) {
					transitionRow.put(to, transitionRow.get(to) + 1);
				}else{
					transitionRow.put(to, 1.0);
				}
			}
			
			
		}
		
		
		
		for (String from : transitions.keySet()) {
			
			Map<String,Double> row = transitions.get(from);
			
			for (String s : row.keySet()) {
				row.put(s, row.get(s) / transitionTotals.get(from));
			}
			
		}
		return new HMMModel(transitions);
	}

}
