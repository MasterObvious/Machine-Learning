package uk.ac.cam.tal42.mlrwd.exercises.Exercise8;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.IExercise8;
import uk.ac.cam.cl.mlrwd.exercises.sentiment_detection.Sentiment;

public class Exercise8 implements IExercise8 {


	@Override
	public double precision(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		// TODO Auto-generated method stub
		double totalL= 0;
		double correctL = 0;
		
		for (List<DiceType> trueType : true2PredictedMap.keySet()) {
			List<DiceType> predictedType = true2PredictedMap.get(trueType);
			
			for (int i = 0; i < trueType.size(); i++) {
				if (predictedType.get(i) == DiceType.WEIGHTED) {
					totalL++;
					if (trueType.get(i) == DiceType.WEIGHTED) {
						correctL++;
					}
				}
			}
		}
		
		return correctL / totalL;
	}

	@Override
	public double recall(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		// TODO Auto-generated method stub
		double trueL= 0;
		double correctL = 0;
		
		for (List<DiceType> trueType : true2PredictedMap.keySet()) {
			List<DiceType> predictedType = true2PredictedMap.get(trueType);
			
			for (int i = 0; i < trueType.size(); i++) {
				if (trueType.get(i) == DiceType.WEIGHTED) {
					trueL++;
					if (predictedType.get(i) == DiceType.WEIGHTED) {
						correctL++;
					}
				}
			}
		}
		
		return correctL / trueL;
	}

	@Override
	public double fOneMeasure(Map<List<DiceType>, List<DiceType>> true2PredictedMap) {
		// TODO Auto-generated method stub
		
		double precisionValue = precision(true2PredictedMap);
		double recallValue = recall(true2PredictedMap);
		
		double f1 = 2 * (precisionValue * recallValue) / (precisionValue + recallValue);
		
		return f1;
	}

	@Override
	public Map<DiceType, Double> getFinalProbs(List<Path> trainingFiles) throws IOException {
		// TODO Auto-generated method stub
		
		Map<DiceType, Double> finalProbs = new HashMap<>();
		
		for (DiceType t : DiceType.values()) {
			finalProbs.put(t, 0.0);
		}
		
		List<HMMDataStore<DiceRoll,DiceType>> dataList = HMMDataStore.loadDiceFiles(trainingFiles);
		
		for (HMMDataStore<DiceRoll,DiceType> hmm : dataList) {
			DiceType lastElement = hmm.hiddenSequence.get(hmm.hiddenSequence.size() - 1);
			finalProbs.put(lastElement, finalProbs.get(lastElement) + 1);
		}
		
		for (DiceType t : DiceType.values()) {
			finalProbs.put(t, finalProbs.get(t) / dataList.size());
		}
		
		
		return finalProbs;
	}

	@Override
	public List<DiceType> viterbi(HiddenMarkovModel<DiceRoll, DiceType> model, Map<DiceType, Double> finalProbs,
			List<DiceRoll> observedSequence) {
			// TODO Auto-generated method stub
				List<Map<DiceType,Double>> delta = new ArrayList<>();
				List<Map<DiceType,DiceType>> psi = new ArrayList<>();
				
				
				//initalProbs
				Map<DiceType,Double> deltaEntry = new HashMap<>();
				
				for (DiceType t : DiceType.values()) {
					double logInitProb = Math.log(model.getInitialProbs().get(t));
					double logObservationProb = Math.log(model.getEmissionMatrix().get(t).get(observedSequence.get(0)));
					deltaEntry.put(t, logInitProb + logObservationProb);
				}
				
				delta.add(0, deltaEntry);
				
				
				//viterbi for entire sequence
				for (int currentObservation = 1; currentObservation < observedSequence.size(); currentObservation++) {
					
					deltaEntry = new HashMap<>();
					Map<DiceType,DiceType> psiEntry = new HashMap<>();
					
					for (DiceType t : DiceType.values()) {
						
						double maxProb = Double.NEGATIVE_INFINITY;
						DiceType maxType = DiceType.WEIGHTED;
						
						for (DiceType i : DiceType.values()) {
							double previousProb = delta.get(currentObservation - 1).get(i);
							double transitionProb = Math.log(model.getTransitionMatrix().get(i).get(t));
							double emissionProb = Math.log(model.getEmissionMatrix().get(t).get(observedSequence.get(currentObservation)));
							
							double totalProb = previousProb + transitionProb + emissionProb;
							
							if (totalProb > maxProb) {
								maxProb = totalProb;
								maxType = i;
							}
						}
						
						deltaEntry.put(t, maxProb);
						psiEntry.put(t, maxType);
					}
					
					
					psi.add(currentObservation - 1, psiEntry);
					delta.add(currentObservation, deltaEntry);
					
				}
				
				//viterbi for final state
				double maxProb = Double.NEGATIVE_INFINITY;
				DiceType maxType = DiceType.WEIGHTED;
				
				for (DiceType i : DiceType.values()) {
					double totalProb = delta.get(observedSequence.size() - 1).get(i) + Math.log(finalProbs.get(i));
					
					if (totalProb > maxProb) {
						maxProb = totalProb;
						maxType = i;
					}
				}
				
				DiceType[] predictedSequenceArray = new DiceType[observedSequence.size()];
				
				predictedSequenceArray[observedSequence.size() - 1] = maxType;
				
				for (int i = observedSequence.size() - 1; i > 0; i--) {
					DiceType currentState = predictedSequenceArray[i];
					DiceType previousState = psi.get(i-1).get(currentState);
					
					predictedSequenceArray[i-1] = previousState;
				}
				
				
				
				return Arrays.asList(predictedSequenceArray);
	}

	@Override
	public Map<List<DiceType>, List<DiceType>> predictAll(HiddenMarkovModel<DiceRoll, DiceType> model,
			Map<DiceType, Double> finalProbs, List<Path> testFiles) throws IOException {
		// TODO Auto-generated method stub
		
		Map<List<DiceType>,List<DiceType>> true2PredictedMap = new HashMap<>();
		
		List<HMMDataStore<DiceRoll,DiceType>> dataList = HMMDataStore.loadDiceFiles(testFiles);
		
		for (HMMDataStore<DiceRoll,DiceType> hmm : dataList) {
			true2PredictedMap.put(hmm.hiddenSequence, viterbi(model,finalProbs,hmm.observedSequence));
			
		}
		
		
		
		return true2PredictedMap;
	}
	
	public static List<List<Path>> splitCVRandom(List<Path> dataSet, int seed) {

		List<Path> pathList = new ArrayList<>(dataSet);
		
		List<List<Path>> dataSplit = new ArrayList<>();
		
		Collections.shuffle(pathList, new Random(seed));
		
		List<Path> currentSplit = new ArrayList<>();
		
		for (Path p : pathList) {
			
			currentSplit.add(p);
			
			if (currentSplit.size() >= pathList.size() / 10) {
				dataSplit.add(currentSplit);
				currentSplit = new ArrayList<>();
			}
		}
		
		return dataSplit;
	}
	

}
