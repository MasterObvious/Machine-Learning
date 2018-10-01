package uk.ac.cam.tal42.mlrwd.exercises.Exercise9;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.AminoAcid;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.Feature;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.IExercise9;

public class Exercise9 implements IExercise9 {

	@Override
	public Map<Feature, Double> getFinalProbs(List<HMMDataStore<AminoAcid, Feature>> trainingPairs) throws IOException {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		
				Map<Feature, Double> finalProbs = new HashMap<>();
				
				for (Feature t : Feature.values()) {
					finalProbs.put(t, 0.0);
				}
				
				List<HMMDataStore<AminoAcid,Feature>> dataList = trainingPairs;
				
				for (HMMDataStore<AminoAcid,Feature> hmm : dataList) {
					Feature lastElement = hmm.hiddenSequence.get(hmm.hiddenSequence.size() - 1);
					finalProbs.put(lastElement, finalProbs.get(lastElement) + 1);
				}
				
				for (Feature t : Feature.values()) {
					finalProbs.put(t, finalProbs.get(t) / dataList.size());
					
					if (finalProbs.get(t) == 0.0) {
						finalProbs.remove(t);
					}
				}
				
				
				return finalProbs;
	}

	@Override
	public HiddenMarkovModel<AminoAcid, Feature> estimateHMM(List<HMMDataStore<AminoAcid, Feature>> sequencePairs)
			throws IOException {
		// TODO Auto-generated method stub
		
				//initialise matrices
				Map<Feature, Double> initialProbs = new HashMap<>();
				Map<Feature, Map<Feature, Double>> transitionMatrix = new HashMap<>();
				Map<Feature, Map<AminoAcid, Double>> emissionMatrix = new HashMap<>();
				
				
				for (Feature t : Feature.values()) {
					
					initialProbs.put(t, 0.0);
					
					Map<Feature, Double> transitionMatrixRow = new HashMap<>();
					for (Feature rowT : Feature.values()) {
						transitionMatrixRow.put(rowT, 0.0);
					}
					transitionMatrix.put(t, transitionMatrixRow);
					
					Map<AminoAcid,Double> emissionMatrixRow = new HashMap<>();
					
					for (AminoAcid roll : AminoAcid.values()) {
						emissionMatrixRow.put(roll, 0.0);
					}
					
					emissionMatrix.put(t, emissionMatrixRow);
					
				}
				
			
				
				//initialsie totals
				Map<Feature, Integer> transitionTotals = new HashMap<>();
				Map<Feature, Integer> emissionTotals = new HashMap<>();
				
				for (Feature t : Feature.values()) {
					transitionTotals.put(t, 0);
					emissionTotals.put(t, 0);
				}
			
				
				
				//load files
				List<HMMDataStore<AminoAcid, Feature>> dataList = sequencePairs;
				
				
				for(HMMDataStore<AminoAcid, Feature> store : dataList) {
					List<AminoAcid> observations = store.observedSequence;
					List<Feature> hidden = store.hiddenSequence;
					
					initialProbs.put(hidden.get(0), initialProbs.get(hidden.get(0)) + 1);
					emissionTotals.put(hidden.get(0), emissionTotals.get(hidden.get(0)) + 1);
					
					Map<AminoAcid, Double> emissionMatrixRow = emissionMatrix.get(hidden.get(0));
					emissionMatrixRow.put(observations.get(0), emissionMatrixRow.get(observations.get(0)) + 1);
					
					for (int i = 1; i < observations.size() ; i++) {
						
						Feature currentState = hidden.get(i);
						Feature previousState = hidden.get(i - 1);
						AminoAcid currentObservation = observations.get(i);
						
						
						Map<Feature,Double> transitionMatrixRow = transitionMatrix.get(previousState);
						transitionMatrixRow.put(currentState, transitionMatrixRow.get(currentState) + 1);
						transitionTotals.put(previousState, transitionTotals.get(previousState) + 1);
						
						emissionMatrixRow = emissionMatrix.get(currentState);
						emissionMatrixRow.put(currentObservation, emissionMatrixRow.get(currentObservation) + 1);
						emissionTotals.put(currentState, emissionTotals.get(currentState) + 1);
						
					}
				}
				
				for (Feature t : Feature.values()) {
					initialProbs.put(t, initialProbs.get(t) / dataList.size());
					
					Map<Feature, Double> transitionMatrixRow = transitionMatrix.get(t);
					
					for (Feature rowT : Feature.values()) {
						transitionMatrixRow.put(rowT, transitionMatrixRow.get(rowT) / transitionTotals.get(t));
					}
					
					Map<AminoAcid, Double> emissionMatrixRow = emissionMatrix.get(t);
					for (AminoAcid roll : AminoAcid.values()) {
						emissionMatrixRow.put(roll, emissionMatrixRow.get(roll) / emissionTotals.get(t));
					}
					
				}
				
				
				
				
				return new HiddenMarkovModel<AminoAcid,Feature>(transitionMatrix,emissionMatrix,initialProbs);
				
	}

	@Override
	public List<Feature> viterbi(HiddenMarkovModel<AminoAcid, Feature> model, Map<Feature, Double> finalProbs,
			List<AminoAcid> observedSequence) {
		// TODO Auto-generated method stub
		List<Map<Feature,Double>> delta = new ArrayList<>();
		List<Map<Feature,Feature>> psi = new ArrayList<>();
		
		
		//initalProbs
		Map<Feature,Double> deltaEntry = new HashMap<>();
		
		for (Feature t : Feature.values()) {
			
			
			if (model.getInitialProbs().containsKey(t)) {
				double logInitProb = Math.log(model.getInitialProbs().get(t));
				double logObservationProb = Math.log(model.getEmissionMatrix().get(t).get(observedSequence.get(0)));
				deltaEntry.put(t, logInitProb + logObservationProb);
			}
		}
		
		delta.add(0, deltaEntry);
		
		
		//viterbi for entire sequence
		for (int currentObservation = 1; currentObservation < observedSequence.size(); currentObservation++) {
			
			deltaEntry = new HashMap<>();
			Map<Feature,Feature> psiEntry = new HashMap<>();
			
			for (Feature t : Feature.values()) {
				
				double maxProb = Double.NEGATIVE_INFINITY;
				Feature maxType = Feature.MEMBRANE;
				
				for (Feature i : Feature.values()) {
					
					
					
					if (model.getTransitionMatrix().get(i).containsKey(t) && delta.get(currentObservation - 1).containsKey(i)) {
						double previousProb = delta.get(currentObservation - 1).get(i);
						double transitionProb = Math.log(model.getTransitionMatrix().get(i).get(t));
						double emissionProb = Math.log(model.getEmissionMatrix().get(t).get(observedSequence.get(currentObservation)));
						
						double totalProb = previousProb + transitionProb + emissionProb;
						
						if (totalProb > maxProb) {
							maxProb = totalProb;
							maxType = i;
						}
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
		Feature maxType = Feature.MEMBRANE;
		
		for (Feature i : Feature.values()) {
			double totalProb = Double.NEGATIVE_INFINITY;
			
			if (finalProbs.containsKey(i)) {
				totalProb = delta.get(observedSequence.size() - 1).get(i) + Math.log(finalProbs.get(i));
				if (totalProb > maxProb) {
					maxProb = totalProb;
					maxType = i;
				}
			}
			
			
			
			
		}
		
		Feature[] predictedSequenceArray = new Feature[observedSequence.size()];
		
		predictedSequenceArray[observedSequence.size() - 1] = maxType;
		
		for (int i = observedSequence.size() - 1; i > 0; i--) {
			Feature currentState = predictedSequenceArray[i];
			Feature previousState = psi.get(i-1).get(currentState);
			
			predictedSequenceArray[i-1] = previousState;
		}
		
		
		
		return Arrays.asList(predictedSequenceArray);
	}

	@Override
	public Map<List<Feature>, List<Feature>> predictAll(HiddenMarkovModel<AminoAcid, Feature> model,
			Map<Feature, Double> finalProbs, List<HMMDataStore<AminoAcid, Feature>> testSequencePairs)
			throws IOException {
		
		
		Map<List<Feature>,List<Feature>> true2PredictedMap = new HashMap<>();
		
		List<HMMDataStore<AminoAcid,Feature>> dataList = testSequencePairs;
		
		for (HMMDataStore<AminoAcid,Feature> hmm : dataList) {
			true2PredictedMap.put(hmm.hiddenSequence, viterbi(model,finalProbs,hmm.observedSequence));
			
		}
		return true2PredictedMap;
	}

	@Override
	public double precision(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		// TODO Auto-generated method stub
		double totalL= 0;
		double correctL = 0;
		
		for (List<Feature> trueType : true2PredictedMap.keySet()) {
			List<Feature> predictedType = true2PredictedMap.get(trueType);
			
			for (int i = 0; i < trueType.size(); i++) {
				if (predictedType.get(i) == Feature.MEMBRANE) {
					totalL++;
					if (trueType.get(i) == Feature.MEMBRANE) {
						correctL++;
					}
				}
			}
		}
		
		return correctL / totalL;
	}

	@Override
	public double recall(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		// TODO Auto-generated method stub
		double trueL= 0;
		double correctL = 0;
		
		for (List<Feature> trueType : true2PredictedMap.keySet()) {
			List<Feature> predictedType = true2PredictedMap.get(trueType);
			
			for (int i = 0; i < trueType.size(); i++) {
				if (trueType.get(i) == Feature.MEMBRANE) {
					trueL++;
					if (predictedType.get(i) == Feature.MEMBRANE) {
						correctL++;
					}
				}
			}
		}
		
		return correctL / trueL;
	}

	@Override
	public double fOneMeasure(Map<List<Feature>, List<Feature>> true2PredictedMap) {
		// TODO Auto-generated method stub
		double precisionValue = precision(true2PredictedMap);
		double recallValue = recall(true2PredictedMap);
		
		double f1 = 2 * (precisionValue * recallValue) / (precisionValue + recallValue);
		
		return f1;
	}
	
	public static List<List<HMMDataStore<AminoAcid,Feature>>> splitCVRandom(List<HMMDataStore<AminoAcid,Feature>> dataSet, int seed) {

		List<HMMDataStore<AminoAcid,Feature>> pathList = new ArrayList<>(dataSet);
		
		List<List<HMMDataStore<AminoAcid,Feature>>> dataSplit = new ArrayList<>();
		
		Collections.shuffle(pathList, new Random(seed));
		
		List<HMMDataStore<AminoAcid,Feature>> currentSplit = new ArrayList<>();
		
		for (HMMDataStore<AminoAcid,Feature> p : pathList) {
			
			currentSplit.add(p);
			
			if (currentSplit.size() >= pathList.size() / 10) {
				dataSplit.add(currentSplit);
				currentSplit = new ArrayList<>();
			}
		}
		
		return dataSplit;
	}

}
