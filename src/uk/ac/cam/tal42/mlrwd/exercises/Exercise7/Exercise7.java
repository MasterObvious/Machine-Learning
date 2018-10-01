package uk.ac.cam.tal42.mlrwd.exercises.Exercise7;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HMMDataStore;

public class Exercise7 implements IExercise7 {

	@Override
	public HiddenMarkovModel<DiceRoll, DiceType> estimateHMM(Collection<Path> sequenceFiles) throws IOException {
		// TODO Auto-generated method stub
		
		//initialise matrices
		Map<DiceType, Double> initialProbs = new HashMap<>();
		Map<DiceType, Map<DiceType, Double>> transitionMatrix = new HashMap<>();
		Map<DiceType, Map<DiceRoll, Double>> emissionMatrix = new HashMap<>();
		
		
		for (DiceType t : DiceType.values()) {
			
			initialProbs.put(t, 0.0);
			
			Map<DiceType, Double> transitionMatrixRow = new HashMap<>();
			for (DiceType rowT : DiceType.values()) {
				transitionMatrixRow.put(rowT, 0.0);
			}
			transitionMatrix.put(t, transitionMatrixRow);
			
			Map<DiceRoll,Double> emissionMatrixRow = new HashMap<>();
			
			for (DiceRoll roll : DiceRoll.values()) {
				emissionMatrixRow.put(roll, 0.0);
			}
			
			emissionMatrix.put(t, emissionMatrixRow);
			
		}
		
		//initialsie totals
		Map<DiceType, Integer> transitionTotals = new HashMap<>();
		Map<DiceType, Integer> emissionTotals = new HashMap<>();
		
		for (DiceType t : DiceType.values()) {
			transitionTotals.put(t, 0);
			emissionTotals.put(t, 0);
		}
	
		
		
		//load files
		List<HMMDataStore<DiceRoll, DiceType>> dataList = HMMDataStore.loadDiceFiles(sequenceFiles);
		
		
		for(HMMDataStore<DiceRoll, DiceType> store : dataList) {
			List<DiceRoll> observations = store.observedSequence;
			List<DiceType> hidden = store.hiddenSequence;
			
			initialProbs.put(hidden.get(0), initialProbs.get(hidden.get(0)) + 1);
			emissionTotals.put(hidden.get(0), emissionTotals.get(hidden.get(0)) + 1);
			
			Map<DiceRoll, Double> emissionMatrixRow = emissionMatrix.get(hidden.get(0));
			emissionMatrixRow.put(observations.get(0), emissionMatrixRow.get(observations.get(0)) + 1);
			
			for (int i = 1; i < observations.size() ; i++) {
				
				DiceType currentState = hidden.get(i);
				DiceType previousState = hidden.get(i - 1);
				DiceRoll currentObservation = observations.get(i);
				
				
				Map<DiceType,Double> transitionMatrixRow = transitionMatrix.get(previousState);
				transitionMatrixRow.put(currentState, transitionMatrixRow.get(currentState) + 1);
				transitionTotals.put(previousState, transitionTotals.get(previousState) + 1);
				
				emissionMatrixRow = emissionMatrix.get(currentState);
				emissionMatrixRow.put(currentObservation, emissionMatrixRow.get(currentObservation) + 1);
				emissionTotals.put(currentState, emissionTotals.get(currentState) + 1);
				
			}
		}
		
		for (DiceType t : DiceType.values()) {
			initialProbs.put(t, initialProbs.get(t) / dataList.size());
			
			Map<DiceType, Double> transitionMatrixRow = transitionMatrix.get(t);
			
			for (DiceType rowT : DiceType.values()) {
				transitionMatrixRow.put(rowT, transitionMatrixRow.get(rowT) / transitionTotals.get(t));
			}
			
			Map<DiceRoll, Double> emissionMatrixRow = emissionMatrix.get(t);
			for (DiceRoll roll : DiceRoll.values()) {
				emissionMatrixRow.put(roll, emissionMatrixRow.get(roll) / emissionTotals.get(t));
			}
			
		}
		
		
		return new HiddenMarkovModel<DiceRoll,DiceType>(transitionMatrix,emissionMatrix,initialProbs);
	}

}
