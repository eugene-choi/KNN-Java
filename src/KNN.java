import static java.util.stream.Collectors.toMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import edu.stanford.nlp.simple.Sentence;

public class KNN  {
	
	static List<String> documents = new ArrayList<String>();
	static List<Integer> totalTerms = new ArrayList<Integer>();
	static List<Map<String, Integer>> termCounter = new ArrayList<Map<String, Integer>>();
	static List<Map<String, Integer>> common = new ArrayList<Map<String, Integer>>();
	
	static int unknownCnt = 0; 
	
	public KNN(File file) throws IOException {
		for(File document : file.listFiles()) {
			if(document.getName().endsWith(".txt")) {
				documents.add(new String(Files.readAllBytes(Paths.get(document.getPath()))));
				termCounter.add(new HashMap<String, Integer>());
				unknownCnt++;
			}
		}
		for(int i = 0; i < unknownCnt; i++) {
			String[] info = preprocess(documents.get(i), termCounter.get(i));
			String document = info[0];
			int total = Integer.parseInt(info[1]);
			totalTerms.add(total);
			// Generating n-grams of 2-words length. Only taking in the ones that occurs more than 3 times.
			// Then, the ones that occurs more than 3 times is doubled its occurences.
			termCounter.get(i).putAll(Main.threshold(Main.ngram_creator(document, 2), 1));
			totalTerms.set(i, totalTerms.get(i) + Main.curNgram);
			// Generating n-grams of 3-words length. Only taking in the ones that occurs more than 3 times.
			// Then, the ones that occurs more than 3 times is tripled its occurences.
			termCounter.get(i).putAll(Main.threshold(Main.ngram_creator(document, 3), 1));
			totalTerms.set(i, totalTerms.get(i) + Main.curNgram);
		}
		common = Main.common;
		amplify();
		recompute();
		for(int i = 0; i < unknownCnt; i++) {
			termCounter.set(i, Main.sortByValue(termCounter.get(i)));
//			TestOneGroup.storeTextFile("unknown_article" + (unknownCnt - i), TestOneGroup.printMap(termCounter.get(i)));
		}
	}
	
	public static void countTerms() {
		for(Map<String, Integer> document : termCounter) {
			int total = 0;
			for(Iterator<Map.Entry<String, Integer>> iterator = document.entrySet().iterator();
					iterator.hasNext();) {
				Map.Entry<String, Integer> entry = iterator.next();
				int value = entry.getValue();
				total += value;
	        }
			System.out.println(total);
		}
	}
	// Method for pre-processing the text documents. 
	public static String[] preprocess(String input, Map<String, Integer> termCounter) {
		StringBuilder processed = new StringBuilder();
		StringTokenizer document = new StringTokenizer(input); 
		int total = 0;
		while(document.hasMoreTokens()) {
			String word = document.nextToken().trim();
			word = word.replaceAll("\\W", "");
			word = word.replaceAll("[0-9]", "");
			if(word.isEmpty()) continue;
			word = (new Sentence(word)).lemmas().toString().replaceAll("\\W", "");
			if(!Main.stopwords.contains(word.toLowerCase())) {
				total++;
				processed.append(word + " ");
				if(termCounter.containsKey(word)) termCounter.replace(word, termCounter.get(word) + 1);
				else termCounter.put(word, 1); 
			}
		}
		termCounter = Main.threshold(termCounter, 0);
		total = 0;
		for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.entrySet().iterator();
				iterator.hasNext();) {
			Map.Entry<String, Integer> entry = iterator.next();
			int value = entry.getValue();
			total += value;
        }
		processed.deleteCharAt(processed.length() - 1);
		String[] output = new String[2];
		output[0] = processed.toString();
		output[1] = Integer.toString(total);
		return output;
	}
	
	public static void amplify() {
		for(int i = 0; i < unknownCnt; i++) {
			for(int j = 0; j < common.size(); j++) {
				for(Iterator<Map.Entry<String, Integer>> iterator = common.get(j).entrySet().iterator(); iterator.hasNext();) {
					Map.Entry<String, Integer> entry = iterator.next();
					String curKey = entry.getKey().toLowerCase();
					if(termCounter.get(i).containsKey(curKey)) {
						termCounter.get(i).replace(curKey, 
								termCounter.get(i).get(curKey) << 5 
//								+ entry.getValue()
								);
					}
				}	
			}
		}
	}
	
	public static void recompute() {
		for(int i = 0; i < unknownCnt; i++) {
			int total = 0;
			for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, Integer> entry = iterator.next();
				total += entry.getValue();
			}
			totalTerms.set(i, total);
		}
	}
	
	// The vectors inside PCA results matrix is always in the euclidean (2D) space.
	public static Map<Double, Integer> doKNN(double[][] pcaResult, int unknownIndex) {
		// I will be using euclidean distance as a distance measure.
		DistanceMeasure euclidean = new DistanceMeasure(false);
		// Mapping distance (double) with the index of each vector given in the pcaResult matrix.
		Map<Double, Integer> distance = new HashMap<Double, Integer>();
		double[] unknown = pcaResult[unknownIndex];
		int vectors = pcaResult.length;
		for(int i = 0; i < vectors; i++) {
			if(i == unknownIndex) continue;
			distance.put(euclidean.getSimilarity(pcaResult[i], unknown), i);
		}
		return sortByKey(distance);		
	}
	
	
	// Method for sorting the components of the vector by their values.
	public static Map<Double, Integer> sortByKey(Map<Double, Integer> distance) { 
		return distance.entrySet().stream()
		        .sorted(Map.Entry.comparingByKey())
		        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }
	
	// I will use weighted KNN to report the results.
	public static String getResults(String docName, Map<Double, Integer> distance, int[] Ks, int[][] clusters, String[] labels) throws IOException {
		StringBuilder output = new StringBuilder();
		output.append("############# KNN started for " + docName + ".txt ################\n");
		
		Arrays.sort(Ks);
		int maxK = Ks[Ks.length - 1];
		int length = Ks.length;
		
		double[] distances = new double[length];
		int[] groups = new int[length];
		
		
		int iteration = 0;
		for(Iterator<Map.Entry<Double, Integer>> iterator = distance.entrySet().iterator(); iterator.hasNext();) {
			if(iteration == maxK) break;
			Map.Entry<Double, Integer> entry = iterator.next();
			double curDistance = entry.getKey();
			int index = entry.getValue();
			distances[iteration] = curDistance;
			int group = -1;
			for(int i = 0; i < clusters.length; i++) {
				if(clusters[i][index] == 1) group = i;
			}
			groups[iteration] = group;
			iteration++;
        }
		output.append(Arrays.toString(distances));
		Map<Integer, Double> grouping = new HashMap<Integer, Double>();
		HashMap<Integer, String> report = new HashMap<Integer, String>();
		
		for(int i = 0; i < Ks.length; i++) {
			int k = Ks[i];
			output.append("\n################################################");
			output.append("\n >>> Summary of k = " + k + " points selected: \nResults: \n");
			
			double curDistance = distances[i];
			int curGroup = groups[i];
			
			double weightedKnn = 1 / Math.pow(curDistance, 2);
			
			if(grouping.containsKey(curGroup)) {
				grouping.replace(curGroup, grouping.get(curGroup) + weightedKnn);
				String weighted = " + [1 / (" + String.format("%.5f", Math.pow(curDistance, 2)) + ")]";
				report.replace(curGroup, report.get(curGroup) + weighted);
			}
			else {
				grouping.put(curGroup, weightedKnn);
				String summary = ">>> Group with keywords: \n " + labels[curGroup] + " :\n[1 / (" 
				+ String.format("%.5f", curDistance) + ")]";
				report.put(curGroup, summary);
			}
			grouping = sortByValue(grouping);
			boolean first = true;
			int bestGroup = -1;
			double bestValue = -1;
			ArrayList<Double> values = new ArrayList<Double>();
			ArrayList<Integer> group = new ArrayList<Integer>();
			double total = 0.0;
			for(Iterator<Map.Entry<Integer, Double>> iterator = grouping.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<Integer, Double> entry = iterator.next();
				int curKey = entry.getKey();
				double value = entry.getValue();
				if(first) {
					bestGroup = curKey;
					bestValue = value;
					first = false;
				}
				total += value;
				values.add(value);
				group.add(curKey);
				output.append("\n" + report.get(curKey));
				output.append(" = " + String.format("%.5f", value) + "\n");
	        }
			output.append("\n\n*** The unknown document is classified into group with keywords: " + labels[bestGroup]);
			output.append("\n*** The fuzzy KNN report is as follows: \n");
			for(int o = 0; o < values.size(); o++) {
				output.append("(" + (o + 1) + ") " + labels[group.get(o)] + ": " + String.format("%.2f%%", (values.get(o) / total) * 100));
				output.append("\n");
			}			
		}
		Main.storeTextFile(docName+"Summary", output.toString());
		return output.toString();
	}
	
	// Method for sorting the components of the vector by their values.
	public static Map<Integer, Double> sortByValue(Map<Integer, Double> component) { 
		return component.entrySet().stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }
	
}