//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.*;
//import edu.stanford.nlp.simple.*;
//import java.util.regex.Pattern;
//
//// A class that contains all the methods required while preprocessing the input documents to classify.
//public class PreProcessing {
//	
//	static Set<String> stopwords = new HashSet<String>();
//	
//	// Method for loading stop words.
//	public static void loadStopwords(File stopwordsFile) throws IOException {
//		StringTokenizer stopWords = new StringTokenizer(new String(Files.readAllBytes(Paths.get(stopwordsFile.getPath()))));
//    	while(stopWords.hasMoreTokens()) stopwords.add(stopWords.nextToken());
//	}
//	
//	// Method for pre-processing the text documents. 
//	public static String[] preprocess(String input, Map<String, Integer> termCounter) {
//		StringBuilder processed = new StringBuilder();
//		StringTokenizer document = new StringTokenizer(input); 
//		int total = 0;
//		while(document.hasMoreTokens()) {
//			String word = document.nextToken().trim();
//			word = word.replaceAll("\\W", "");
//			word = word.replaceAll("[0-9]", "");
//			if(word.isEmpty()) continue;
//			word = (new Sentence(word)).lemmas().toString().replaceAll("\\W", "");
//			if(!stopwords.contains(word.toLowerCase())) {
//				total++;
//				processed.append(word + " ");
//				if(termCounter.containsKey(word)) termCounter.replace(word, termCounter.get(word) + 1);
//				else termCounter.put(word, 1); 
//			}
//		}
//		termCounter = threshold(termCounter, 0);
//		total = 0;
//		for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.entrySet().iterator(); iterator.hasNext();) {
//			Map.Entry<String, Integer> entry = iterator.next();
//			int value = entry.getValue();
//			total += value;
//        }
//		processed.deleteCharAt(processed.length() - 1);
//		String[] output = new String[2];
//		output[0] = processed.toString();
//		output[1] = Integer.toString(total);
//		return output;
//	}
////	
////	// Method for loading all the text documents in a folder.
////	public static void loadDocuments(File file) throws IOException {
////		int foldersCount = 0;
////		for(File folder : file.listFiles()) {
////			if(folder.isDirectory()) {
////				// Creating folders for each 2D array lists.
////				documents.add(new ArrayList<String>());
////				totalTerms.add(new ArrayList<Integer>());
////				termCounter.add(new ArrayList<Map<String, Integer>>());
////				TF.add(new ArrayList<Map<String, Double>>());
////				IDF.add(new ArrayList<Map<String, Double>>());
////				int documentsCounter = 0;
////				for(File doc : folder.listFiles()) {
////					if(doc.getName().endsWith(".txt")) {
////						documents.get(foldersCount).add(new String(Files.readAllBytes(Paths.get(doc.getPath()))));
////						termCounter.get(foldersCount).add(new HashMap<String, Integer>());
////						TF.get(foldersCount).add(new HashMap<String, Double>());
////						IDF.get(foldersCount).add(new HashMap<String, Double>());
////						documentsCounter++;
////					}
////				}
////				docsPerFolder.add(documentsCounter);
////				totalnumberOfDocuments += documentsCounter;
////				foldersCount++;
////			}
////		}
////		folders = foldersCount;
////	}
//	
//	// Method for pre-processing the text documents. 
////	public static String[] preprocess(String input, Map<String, Integer> termCounter) {
////		StringBuilder processed = new StringBuilder();
////		StringTokenizer document = new StringTokenizer(input); 
////		int total = 0;
////		while(document.hasMoreTokens()) {
////			String word = document.nextToken().trim();
////			word = word.replaceAll("\\W", "");
////			word = word.replaceAll("[0-9]", "");
////			if(word.isEmpty()) continue;
////			word = (new Sentence(word)).lemmas().toString().replaceAll("\\W", "");
////			if(!stopwords.contains(word.toLowerCase())) {
////				total++;
////				processed.append(word + " ");
////				if(termCounter.containsKey(word)) termCounter.replace(word, termCounter.get(word) + 1);
////				else termCounter.put(word, 1); 
////			}
////		}
////		termCounter = threshold(termCounter, 0);
////		total = 0;
////		for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.entrySet().iterator(); iterator.hasNext();) {
////			Map.Entry<String, Integer> entry = iterator.next();
////			int value = entry.getValue();
////			total += value;
////        }
////		processed.deleteCharAt(processed.length() - 1);
////		String[] output = new String[2];
////		output[0] = processed.toString();
////		output[1] = Integer.toString(total);
////		return output;
////	}
//	
//	// Method for loading n-grams.
//	public static Map<String, Integer> ngram(String document, int n) {
//		Map<String, Integer> ngrams = new HashMap<String, Integer>();
//		for(Iterator<Map.Entry<String, Integer>> iterator = ngram_creator(document, n).entrySet().iterator(); iterator.hasNext();) {
//			Map.Entry<String, Integer> entry = iterator.next();
//			String curKey = entry.getKey();
//			int value = entry.getValue();
//			if(ngrams.containsKey(curKey)) ngrams.replace(curKey, ngrams.get(curKey) + value);
//			else ngrams.put(curKey, value);
//        }
//		return ngrams;
//	}
//	// Method for removing n-grams with occurences less than the given threshold.
//	public static Map<String, Integer> threshold(Map<String, Integer> ngram, int threshold) {
//		curNgram = 0;
//		for(Iterator<Map.Entry<String, Integer>> iterator = ngram.entrySet().iterator(); iterator.hasNext();) {
//			Map.Entry<String, Integer> entry = iterator.next();
//			int value = entry.getValue();
//			if(value <= threshold) iterator.remove();
//			else curNgram += value;
//        }
//		return ngram;
//	}
//}