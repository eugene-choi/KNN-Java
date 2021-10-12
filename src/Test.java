//import static java.util.stream.Collectors.toMap;
//import java.io.*;
//import java.util.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import edu.stanford.nlp.simple.*;
//
//public class Test {
//	
//	static int groups = 0;
//	static int totalDocs = 0;
//
//	static Set<String> stopwords = new HashSet<String>();
//	
//	/*
//	 * Documents:
//	 * ArrayList of pre-processed documents.
//	 * documents.get(0 ~ 2).get(0 ~ 7); -> fetches the pre-processed document in folder (0 ~ 2) with document number (0 ~ 7).
//	 */
//	static List<List<String>> documents = new ArrayList<List<String>>();
//	/*
//	 * Total terms:
//	 * stores the total number of terms in every document.
//	 */
//	static List<List<Integer>> totalTerms = new ArrayList<List<Integer>>();
//	/*
//	 * Term Counter: 
//	 * Stores occurences of each term in every document.
//	 * termCounter.get(0 ~ 2).get(0 ~ 7).size(); -> number of unique terms in the document.
//	 * termCounter.get(0 ~ 2).get(0 ~ 7).get(word); -> number of times that the word occured in the document.
//	 */
//	static List<List<Map<String, Integer>>> termCounter = new ArrayList<List<Map<String, Integer>>>();
//	/*
//	 * TF: Term Frequency, which measures how frequently a term occurs in a document. 
//	 * Since every document is different in length, 
//	 * it is possible that a term would appear much more times in long documents than shorter ones. 
//	 * Thus, the term frequency is often divided by the document length (aka. the total number of terms in the document) 
//	 * as a way of normalization:
//	 * TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
//	 */
//	static List<List<Map<String, Double>>> TF = new ArrayList<List<Map<String, Double>>>();
//	/*
//	 * termDocumentOccuerncesCounter:
//	 * Stores the number of times each term appears in a group of documents in the designated folder.
//	 * The arraylist will have indexes from 0 to n - 1 (the number of folders grouping the documents, which is 2 (since there are 3 folders)).
//	 * The value for each term (key) ranges between 1 and 8.
//	 */
//	static List<Map<String, Integer>> termDocumentOccuerncesCounter = new ArrayList<Map<String, Integer>>();
//	/*
//	 * IDF: Inverse Document Frequency, which measures how important a term is. 
//	 * While computing TF, all terms are considered equally important. 
//	 * However it is known that certain terms, such as "is", "of", and "that", may appear a lot of times 
//	 * but have little importance. 
//	 * Thus we need to weigh down the frequent terms while scale up the rare ones, by computing the following:
//	 * IDF(t) = log_e(Total number of documents / Number of documents with term t in it).
//	 */
//	static List<List<Map<String, Double>>> IDF = new ArrayList<List<Map<String, Double>>>();
//	
//	/*
//	 * TF-IDF
//	 */
//	static List<List<Map<String, Double>>> TFIDF = new ArrayList<List<Map<String, Double>>>();
//	
//	static int value;
//	static int curNgram;
//	
//	
//	public static void main(String[] args) throws IOException {
//		loadDocuments(new File("./dataset_3/data"));
//		loadStopwords(new File("./dataset_3/stopwords.txt"));
//		
//		for(int i = 0; i < groups; i++) {
//			int docCnt = documents.get(i).size();
//			for(int j = 0; j < docCnt; j++) {
//				String[] info = preprocess(documents.get(i).get(j), termCounter.get(i).get(j));
//				String document = info[0];
//				int total = Integer.parseInt(info[1]);
//				totalTerms.get(i).add(total);
//				// Generating n-grams of 2-words length. Only taking in the ones that occurs more than 3 times.
//				// Then, the ones that occurs more than 3 times is doubled its occurences.
//				termCounter.get(i).get(j).putAll(threshold(ngram_creator(document, 2), 1));
//				totalTerms.get(i).set(j, totalTerms.get(i).get(j) + curNgram);
//				
//				// Generating n-grams of 3-words length. Only taking in the ones that occurs more than 3 times.
//				// Then, the ones that occurs more than 3 times is tripled its occurences.
//				termCounter.get(i).get(j).putAll(threshold(ngram_creator(document, 3), 1));
//				totalTerms.get(i).set(j, totalTerms.get(i).get(j) + curNgram);
//			}
//		}
//		for(int i = 0; i < termCounter.size(); i++) {
//			for(int j = 0; j < termCounter.get(i).size(); j++) {
//				termCounter.get(i).set(j, sortByValue(termCounter.get(i).get(j)));
//				storeTextFile("C" + (i + 1) + "article0" + (j + 1), printMap(termCounter.get(i).get(j)));
////				System.out.println(totalTerms.get(i).get(j));
//			}
//		}
//		// Now calculating TF.
//		for(int i = 0; i < 3; i++) {
//			for(int j = 0; j < 8; j++) {
//				Map<String, Double> tf = new HashMap<String, Double>();
//				int currentTotalTerms = totalTerms.get(i).get(j);
//				for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).get(j).entrySet().iterator();
//						iterator.hasNext();) {
//					Map.Entry<String, Integer> entry = iterator.next();
//					String curKey = entry.getKey().to;
//					int value = entry.getValue();
//					tf.put(curKey, (double) value / currentTotalTerms);
//		        }
//				TF.get(i).get(j).putAll(tf);
//			}
//		}
//		for(int i = 0; i < 3; i++) {
//			termDocumentOccuerncesCounter.add(new HashMap<String, Integer>());
//			for(int j = 0; j < 8; j++) {
//				for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).get(j).entrySet().iterator();
//						iterator.hasNext();) {
//					Map.Entry<String, Integer> entry = iterator.next();
//					String curKey = entry.getKey().toLowerCase();
//					if(termDocumentOccuerncesCounter.get(i).containsKey(curKey)) termDocumentOccuerncesCounter.get(i).replace(curKey, termDocumentOccuerncesCounter.get(i).get(curKey) + 1);
//					else termDocumentOccuerncesCounter.get(i).put(curKey, 1);
//		        }
//			}
//		}
//		
//		// Now calculating IDF.
//		for(int i = 0; i < 3; i++) {
//			for(int j = 0; j < 8; j++) {
//				Map<String, Double> idf = new HashMap<String, Double>();
//				int currentTotalDocuments = totalTerms.get(i).get(j);
//				for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).get(j).entrySet().iterator();
//						iterator.hasNext();) {
//					Map.Entry<String, Integer> entry = iterator.next();
//					String curKey = entry.getKey();
//					int value = entry.getValue();
//					tf.put(curKey, (double) value / currentTotal);
//		        }
//				TF.get(i).get(j).putAll(tf);
//			}
//		}
//		
//	}
//	
//	// Method for loading all the text documents in a folder.
//	public static void loadDocuments(File file) throws IOException {
//		for(File folder : file.listFiles()) {
//			if(folder.isDirectory()) {
//				// Creating folders for each 2D array lists.
//				documents.add(new ArrayList<String>());
//				totalTerms.add(new ArrayList<Integer>());
//				termCounter.add(new ArrayList<Map<String, Integer>>());
//				TF.add(new ArrayList<Map<String, Double>>());
//				IDF.add(new ArrayList<Map<String, Double>>());
//				TFIDF.add(new ArrayList<Map<String, Double>>());
//				for(File doc : folder.listFiles()) {
//					if(doc.getName().endsWith(".txt")) {
//						documents.get(groups).add(new String(Files.readAllBytes(Paths.get(doc.getPath()))));
//						termCounter.get(groups).add(new HashMap<String, Integer>());
//						TF.get(groups).add(new HashMap<String, Double>());
//						IDF.get(groups).add(new HashMap<String, Double>());
//						TFIDF.get(groups).add(new HashMap<String, Double>());
//					}
//				}
//				totalDocs += documents.get(groups++).size();;
//			}
//		}	
//	}
//	
//	// Method for loading stop words.
//	public static void loadStopwords(File stopwordsFile) throws IOException {
//		String document = new String(Files.readAllBytes(Paths.get(stopwordsFile.getPath())));
//		StringTokenizer stopWords = new StringTokenizer(document);
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
//		processed.deleteCharAt(processed.length() - 1);
//		String[] output = new String[2];
//		output[0] = processed.toString();
//		output[1] = Integer.toString(total);
//		return output;
//	}
//	
//	// Method for loading n-grams.
//	public static Map<String, Integer> ngram(String document, int n) {
//		Map<String, Integer> ngrams = new HashMap<String, Integer>();
//		for(Iterator<Map.Entry<String, Integer>> iterator = ngram_creator(document, n).entrySet().iterator();
//				iterator.hasNext();) {
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
//	
////	// Method for loading each word.
////	public static Map<String, Integer> singleWord(String document) {
////		Map<String, Integer> docMap = new HashMap<String, Integer>();
////		for(Iterator<Map.Entry<String, Integer>> iterator = wordCounter(document).entrySet().iterator(); 
////				iterator.hasNext();) {
////			Map.Entry<String, Integer> entry = iterator.next();
////			curKey = entry.getKey();
////			value = entry.getValue();
////			if(onegrams.containsKey(curKey)) onegrams.replace(curKey, onegrams.get(curKey) + value);
////			else onegrams.put(curKey, value);
////        }
////	}
//	// A method that implements sliding window technique to create 
//	// n-grams. It takes the document to process and integer value n
//	// that denotes the length of the window.
//	public static Map<String,Integer> wordCounter(String document) {
//        String[] docArray = document.split(" ");
//		Map<String,Integer> map = new HashMap<String,Integer>();
//		for(String word : docArray) {
//			if(map.containsKey(word)) map.replace(word, map.get(word) + 1);
//			else map.put(word, 1);
//		}
//		return map;
//	}
//	public static String printMap(Map<String, Integer> map) {
//		StringBuilder output = new StringBuilder();
//		for(Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
//			Map.Entry<String, Integer> entry = iterator.next();
//			String curKey = entry.getKey();
//			int value = entry.getValue();
//			output.append(curKey + ": " + value + "\n");
//        }
//		return output.toString();
//	}
//	
//	// Method storing text files.
//	public static void storeTextFile(String title, String content) throws IOException {
//		try(FileWriter writer = new FileWriter(title + ".txt");
//			BufferedWriter bw = new BufferedWriter(writer)) {
//			bw.write(content);
//			bw.close();	
// 		}
//		catch (IOException e) { 
//			System.err.format("IOException: %s%n", e);
//		}	    
//	}
//	
//	// A method that implements sliding window technique to create 
//	// n-grams. It takes the document to process and integer value n
//	// that denotes the length of the window.
//	public static Map<String,Integer> ngram_creator(String document, int n) {
//        String[] grams = document.split(" ");
//		Map<String, Integer> ngrams = new HashMap<String, Integer>();
//		int range = grams.length - n;
//		StringBuilder ngram;
//		String cur;
//		for(int i = 0; i <= range; i++) {
//			ngram = new StringBuilder();
//			for(int j = i; j < i + n; j++) {
//				if(i != j) ngram.append(" ");
//				ngram.append(grams[j]);
//			}
//			cur = ngram.toString();
//			if(ngrams.containsKey(cur)) ngrams.replace(cur, ngrams.get(cur) + 1);
//			else ngrams.put(cur, 1);
//		}
//		return ngrams;
//	}
//	
//	// Method for sorting the components of the vector by their values.
//	public static Map<String, Integer> sortByValue(Map<String, Integer> component) { 
//		return component.entrySet().stream()
//		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
//		        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
//    } 
//}
