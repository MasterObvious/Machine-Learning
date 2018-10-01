package uk.ac.cam.cl.mlrwd.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import uk.ac.cam.cl.mlrwd.exercises.markov_models.AminoAcid;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.Feature;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HMMDataStore;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.HiddenMarkovModel;
import uk.ac.cam.cl.mlrwd.exercises.markov_models.IExercise9;
import uk.ac.cam.tal42.mlrwd.exercises.Exercise8.Exercise8;
//TODO: Replace with your package.
import uk.ac.cam.tal42.mlrwd.exercises.Exercise9.Exercise9;

public class Exercise9Tester {
	
	static final Path dataFile = Paths.get("data/bio_dataset.txt");

	public static void main(String[] args) throws IOException {

		List<List<AminoAcid>> obsSeqs = new ArrayList<List<AminoAcid>>();
		List<List<Feature>> hiddenSeqs = new ArrayList<List<Feature>>();

		try (BufferedReader reader = Files.newBufferedReader(dataFile)) {
			reader.lines().forEach(new Consumer<String>() {
				@Override
				public void accept(String line) {
					if (!line.isEmpty()) {
						if (line.startsWith("#")) {
							obsSeqs.add(line.substring(1).chars()
									.mapToObj(i -> AminoAcid.valueOf((char)i))
									.collect(Collectors.toList()));
						} else {
							hiddenSeqs.add(line.chars()
									.mapToObj(i -> Feature.valueOf((char)i))
									.collect(Collectors.toList()));
						}
					}
				}
			});
		} catch (IOException e) {
			throw new IOException("Can't access the file " + dataFile, e);
		}
		
		List<HMMDataStore<AminoAcid, Feature>> sequencePairs = IntStream.range(0, obsSeqs.size())
				.mapToObj(i -> new HMMDataStore<AminoAcid, Feature>(obsSeqs.get(i), hiddenSeqs.get(i)))
				.collect(Collectors.toList());

		// Use for testing the code		
		Collections.shuffle(sequencePairs, new Random(0));
		List<HMMDataStore<AminoAcid, Feature>> devSet = new ArrayList<>();
		List<HMMDataStore<AminoAcid, Feature>> trainingSet = new ArrayList<>();
		// But:
		// TODO: Replace with cross-validation for the tick.
		
		List<List<HMMDataStore<AminoAcid,Feature>>> dataSplit = Exercise9.splitCVRandom(sequencePairs,10);
		
		double[] precisions = new double[dataSplit.size()];
		double[] recalls = new double[dataSplit.size()];
		double[] fOneMeasures = new double[dataSplit.size()];
		
		for (int currentTestSet = 0; currentTestSet < dataSplit.size(); currentTestSet++) {
			
			devSet = new ArrayList<>();
			trainingSet = new ArrayList<>();
			
			for (int i = 0; i < dataSplit.size(); i++) {
				
				if (i == currentTestSet) {
					devSet = dataSplit.get(i);
				}else{
					trainingSet.addAll(dataSplit.get(i));
				}
				
			}
			
			
			IExercise9 implementation = (IExercise9) new Exercise9();
			HiddenMarkovModel<AminoAcid, Feature> model = implementation.estimateHMM(trainingSet);
			Map<Feature, Double> finalProbs = implementation.getFinalProbs(trainingSet);
			
			Map<List<Feature>, List<Feature>> true2PredictedSequences = implementation.predictAll(model, finalProbs, devSet);
			precisions[currentTestSet] = implementation.precision(true2PredictedSequences);
			
			recalls[currentTestSet] = implementation.recall(true2PredictedSequences);
			
			fOneMeasures[currentTestSet] = implementation.fOneMeasure(true2PredictedSequences);
			
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