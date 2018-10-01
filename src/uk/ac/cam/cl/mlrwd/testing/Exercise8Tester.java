package uk.ac.cam.cl.mlrwd.testing;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.DiceRoll;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.DiceType;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.IExercise7;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.IExercise8;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise7.Exercise7;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise8.Exercise8;

//TODO: Replace with your package.


public class Exercise8Tester {

	static final Path dataDirectory = Paths.get("data/dice_dataset");

	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

		List<Path> sequenceFiles = new ArrayList<>();
		try (DirectoryStream<Path> files = Files.newDirectoryStream(dataDirectory)) {
			for (Path item : files) {
				sequenceFiles.add(item);
			}
		} catch (IOException e) {
			throw new IOException("Cant access the dataset.", e);
		}
		
		// Use for testing the code
		Collections.shuffle(sequenceFiles, new Random(0));
		List<Path> devSet;
		List<Path> trainingSet;
		
		
		// But:
		// TODO: Replace with cross-validation for the tick.
		
		
		List<List<Path>> dataSplit = Exercise8.splitCVRandom(sequenceFiles, 100000);
		
		double[] precisions = new double[dataSplit.size()];
		double[] recalls = new double[dataSplit.size()];
		double[] fOneMeasures = new double[dataSplit.size()];
		
		for (int currentTestSet = 0; currentTestSet < dataSplit.size(); currentTestSet++) {
			
			devSet = new ArrayList<>();
			trainingSet = new ArrayList<>();
			
			for (int i = 0; i < dataSplit.size(); i++) {
				if(i==currentTestSet) {
					devSet = dataSplit.get(i);
				}else{
					trainingSet.addAll(dataSplit.get(i));
				}
			}
			
			IExercise7 implementation7 = (IExercise7) new Exercise7();
			HiddenMarkovModel<DiceRoll, DiceType> model = implementation7.estimateHMM(trainingSet);

			IExercise8 implementation = (IExercise8) new Exercise8();
			Map<DiceType, Double> finalProbs = implementation.getFinalProbs(trainingSet);
		
			Map<List<DiceType>, List<DiceType>> true2PredictedMap = implementation.predictAll(model, finalProbs, devSet);
			
			precisions[currentTestSet] = implementation.precision(true2PredictedMap);

			recalls[currentTestSet] = implementation.recall(true2PredictedMap);

			fOneMeasures[currentTestSet] = implementation.fOneMeasure(true2PredictedMap);
		}
		
		System.out.println("Prediction precision:");
		System.out.println(average(precisions));
		System.out.println();
		
		System.out.println("Prediction recall:");
		System.out.println(average(recalls));
		System.out.println();
		
		System.out.println("Prediction fOneMeasure:");
		System.out.println(average(fOneMeasures));
		System.out.println();
		
		
		
		
	}
	
	public static double average(double[] array) {
		double total = 0;
		
		for (double d : array) {
			total+= d;
		}
		
		return total / array.length;
	}
}
