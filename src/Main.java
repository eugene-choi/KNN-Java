import static java.util.stream.Collectors.toMap;
import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import edu.stanford.nlp.simple.*;

public class Main {
	
	
	static int[][] target = new int[3][24];
	
	static int folders = 0;
	static List<Integer> docsPerFolder = new ArrayList<Integer>();
	static int totalnumberOfDocuments = 0;

	static Set<String> stopwords = new HashSet<String>();
	
	static List<Map<String, Integer>> common = new ArrayList<Map<String, Integer>>();
	
	/*
	 * Documents:
	 * ArrayList of pre-processed documents.
	 * documents.get(0 ~ 2).get(0 ~ 7); -> fetches the pre-processed document in folder (0 ~ 2) with document number (0 ~ 7).
	 */
	static List<List<String>> documents = new ArrayList<List<String>>();
	
//	########################################## TERM FREQUENCY: ##################################################################
	/*
	 * Total terms:
	 * stores the total number of terms in every document.
	 */
	static List<List<Integer>> totalTerms = new ArrayList<List<Integer>>();
	/*
	 * Term Counter: 
	 * Stores occurences of each term in every document.
	 * termCounter.get(0 ~ 2).get(0 ~ 7).size(); -> number of unique terms in the document.
	 * termCounter.get(0 ~ 2).get(0 ~ 7).get(word); -> number of times that the word occured in the document.
	 */
	static List<List<Map<String, Integer>>> termCounter = new ArrayList<List<Map<String, Integer>>>();
	/*
	 * TF: Term Frequency, which measures how frequently a term occurs in a document. 
	 * Since every document is different in length, 
	 * it is possible that a term would appear much more times in long documents than shorter ones. 
	 * Thus, the term frequency is often divided by the document length (aka. the total number of terms in the document) 
	 * as a way of normalization:
	 * TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
	 */
	static List<List<Map<String, Double>>> TF = new ArrayList<List<Map<String, Double>>>();
//	############################################################################################################################## 
	
	
	
//	########################################## INVERSE TERM FREQUENCY: ###########################################################
	/*
	 * 
	 */
	static Map<String, Integer> metaDataIndex = new HashMap<String, Integer>();
	static List<Integer> numberOfDocumentsWithTheTerm = new ArrayList<Integer>();
	/*
	 * termDocumentOccuerncesCounter:
	 * Stores the number of times each term appears in a group of documents in the designated folder.
	 * The arraylist will have indexes from 0 to n - 1 (the number of folders grouping the documents, which is 2 (since there are 3 folders)).
	 * The value for each term (key) ranges between 1 and 8.
	 */
	static List<Map<String, Integer>> termDocumentOccuerncesCounter = new ArrayList<Map<String, Integer>>();
	/*
	 * IDF: Inverse Document Frequency, which measures how important a term is. 
	 * While computing TF, all terms are considered equally important. 
	 * However it is known that certain terms, such as "is", "of", and "that", may appear a lot of times 
	 * but have little importance. 
	 * Thus we need to weigh down the frequent terms while scale up the rare ones, by computing the following:
	 * IDF(t) = log_e(Total number of documents / Number of documents with term t in it).
	 */
	static List<List<Map<String, Double>>> IDF = new ArrayList<List<Map<String, Double>>>();
//	##############################################################################################################################
	
	
//	########################################################### TFIDF: ###########################################################
	/*
	 * 2D-double array for storing tf-idf scores of each document as a row of vectors.
	 * # of columns = # of unique terms in the given document.
	 * # of rows = # of documents.
	 */
	static double[][] tfidf;
//	##############################################################################################################################
	
	static int value;
	static int curNgram;
	
	static List<Map<String, Integer>> unknowns = new ArrayList<Map<String, Integer>>();
	static List<String> unknownDocuments = new ArrayList<String>();
	static List<Integer> unkownTotalTerms = new ArrayList<Integer>();
	static int unknownCnt = 0;
	
	public static void main(String[] args) throws IOException {
		
		System.out.println(">>>>>>>>>>> Preprocessing started for original 24 documents...\n");
		loadDocuments(new File("./dataset_3/data"));
		
		loadStopwords(new File("./dataset_3/stopwords.txt"));
		
		
		for(int i = 0; i < folders; i++) {
			int docCnt = documents.get(i).size();
			for(int j = 0; j < docCnt; j++) {
				String[] info = preprocess(documents.get(i).get(j), termCounter.get(i).get(j));
				String document = info[0];
				int total = Integer.parseInt(info[1]);
				totalTerms.get(i).add(total);
				// Generating n-grams of 2-words length. Only taking in the ones that occurs more than 3 times.
				// Then, the ones that occurs more than 3 times is doubled its occurences.
				termCounter.get(i).get(j).putAll(ngram_creator(document, 2));
				totalTerms.get(i).set(j, totalTerms.get(i).get(j) + curNgram);
				// Generating n-grams of 3-words length. Only taking in the ones that occurs more than 3 times.
				// Then, the ones that occurs more than 3 times is tripled its occurences.
				termCounter.get(i).get(j).putAll(ngram_creator(document, 3));
				totalTerms.get(i).set(j, totalTerms.get(i).get(j) + curNgram);
			}
		}
		getCommons();
		amplify();
		recompute();
		for(int i = 0; i < folders; i++) {
			for(int j = 0; j < docsPerFolder.get(i); j++) {
				int total = totalTerms.get(i).get(j);
				System.out.println(total);
				int limit = (int) (total * 0.03);
				Map<String, Integer> temp = threshold(termCounter.get(i).get(j), limit);
				termCounter.get(i).set(j, temp);
			}
		}
		recompute();
		
		for(int i = 0; i < termCounter.size(); i++) {
			for(int j = 0; j < termCounter.get(i).size(); j++) {
				termCounter.get(i).set(j, sortByValue(termCounter.get(i).get(j)));
//				storeTextFile("C" + (i + 1) + "article0" + (j + 1), printMap(termCounter.get(i).get(j)));
			}
		}
		
		for(int i = 0; i < common.size(); i++) {
			storeTextFile("Group" + (i + 1) + "Commons", printMap(common.get(i)));
		}
		
		// Now calculating TF.
		for(int i = 0; i < folders; i++) {
			for(int j = 0; j < docsPerFolder.get(i); j++) {
				Map<String, Double> tf = new HashMap<String, Double>();
				int currentTotalTerms = totalTerms.get(i).get(j);
				for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).get(j).entrySet().iterator();
						iterator.hasNext();) {
					Map.Entry<String, Integer> entry = iterator.next();
					String curKey = entry.getKey().toLowerCase();
					int value = entry.getValue();
					tf.put(curKey, (double) value / currentTotalTerms);
		        }
				TF.get(i).get(j).putAll(tf);
			}
		}
		for(int i = 0; i < folders; i++) {
			termDocumentOccuerncesCounter.add(new HashMap<String, Integer>());
			for(int j = 0; j < docsPerFolder.get(i); j++) {
				for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).get(j).entrySet().iterator();
						iterator.hasNext();) {
					Map.Entry<String, Integer> entry = iterator.next();
					String curKey = entry.getKey().toLowerCase();
					if(termDocumentOccuerncesCounter.get(i).containsKey(curKey)) termDocumentOccuerncesCounter.get(i).replace(curKey, termDocumentOccuerncesCounter.get(i).get(curKey) + 1);
					else termDocumentOccuerncesCounter.get(i).put(curKey, 1);
		        }
			}
		}
		// Now calculating IDF.
		// First iterate through all of the HashMaps in termCounter and add every HashMaps for each document into a single HashMap to provide all the terms with
		// unique indexes in the numberOfDocumentsWithTheTerm arraylist. We can verify each time we encounter a new term, by keeping all the terms with their indexes
		// stored in the hashmap and increment the number of times we encounter same word in a different document.
		for(int i = 0; i < folders; i++) {
			for(int j = 0; j < docsPerFolder.get(i); j++) {
				for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).get(j).entrySet().iterator(); 
						iterator.hasNext();) {
					Map.Entry<String, Integer> entry = iterator.next();
					String curKey = entry.getKey().toLowerCase();
					if(metaDataIndex.containsKey(curKey)) {
						int index = metaDataIndex.get(curKey);
						numberOfDocumentsWithTheTerm.set(index, numberOfDocumentsWithTheTerm.get(index) + 1);
					}
					else {
						metaDataIndex.put(curKey, numberOfDocumentsWithTheTerm.size());
						numberOfDocumentsWithTheTerm.add(1);
					}
		        }
			}
		}
		tfidf = new double[totalnumberOfDocuments][numberOfDocumentsWithTheTerm.size()];
		for(int i = 0; i < folders; i++) {
			for(int j = 0; j < docsPerFolder.get(i); j++) {
				int tfidfIndex = (i == 0) ? j : docsPerFolder.get(i - 1) + j;
				for(Iterator<Map.Entry<String, Double>> iterator = TF.get(i).get(j).entrySet().iterator();
						iterator.hasNext();) {
					Map.Entry<String, Double> entry = iterator.next();
					String curKey = entry.getKey().toLowerCase();
					int index = metaDataIndex.get(curKey);
					double idf = Math.log((double) totalnumberOfDocuments / numberOfDocumentsWithTheTerm.get(index));
					double tf = entry.getValue();
					tfidf[tfidfIndex][index] = tf * idf;
		        }
			}
		}
		
		DimensionalityReduction.PCA(tfidf, 3, "PCA_Data_KMeans_3D.csv");
		
		double[][] pcaResults = DimensionalityReduction.PCA(tfidf, 2, "PCA_Data_KMeans.csv");
		
	    // Implementing K-Means Plus Plus to get centers.
        KMeansPlusPlus kMeansPP = new KMeansPlusPlus(3, pcaResults);
        kMeansPP.doKMeansPlusPlus();
        
        
        // Instantiate K-means class to perform clustering. Consine distance was used as a distance measure
        // for clustering stage.
        KMeans kmeans = new KMeans(3, pcaResults, kMeansPP.getCenters(), "cosine", 10);
        int[][] clusters = kmeans.perfromKMeans();
		
		String[] test = new String[3];
		for(int i = 0; i < folders; i++) {
			StringBuilder output = new StringBuilder();
			for(Iterator<Map.Entry<String, Integer>> iterator = common.get(i).entrySet().iterator();
					iterator.hasNext();) {
				Map.Entry<String, Integer> entry = iterator.next();
				output.append(entry.getKey() + "/");
	        }
			test[i] = output.deleteCharAt(output.length() - 1).toString();
		}

		System.out.println("\n>>>>>>>>>>> Confusion Matrix:\n" + String.format("%50s", ""));
        ConfusionMatrix evaluation = new ConfusionMatrix();
        // Generating target(goal) matrix.
        makeTargetMatrix();
        
        // Now building confusion matrix....
        evaluation.getConfusionMatrix(clusters, target, test);
        System.out.println(Arrays.toString(evaluation.getFinalClusterLabels()));
        for(int[] arr : target) System.out.println(Arrays.toString(arr));
        System.out.println(Arrays.toString(test));
        for(int[] arr : clusters) System.out.println(Arrays.toString(arr));
		
		System.out.println("Creating visual images of the clusters created: ");
        new Visualization("TargetClusters.png", "Target Clusters", evaluation.getFinalClusterLabels(), target, pcaResults);
        new Visualization("MyClusters.png", "My Clusters", evaluation.getFinalClusterLabels(), clusters, pcaResults);
//      ################################################################################################################################################
//      >>> Now implementing k-nearest neighbor algorithm to classify unknown documents. The codes up until this point
//        were mostly tasks from the first programming assignment (huge modifications/improvements were taken place).
//      ################################################################################################################################################
        
        
        
        // Now implementing K-NN algorithm by taking unknown texts as input documents and classifying each document into 
        // one of the three groups classified previously.
        KNN myKNN = new KNN(new File("./unknown/"));
        
    	unknowns = myKNN.termCounter;
    	unknownDocuments = myKNN.documents;
    	unkownTotalTerms = myKNN.totalTerms;
    	unknownCnt = myKNN.unknownCnt;
        
    	// Populating term frequencies of unknown documents.
    	List<Map<String, Double>> unknownTF = new ArrayList<Map<String, Double>>();
    	for(int i = 0; i < unknownCnt; i++) {
    		unknownTF.add(new HashMap<String, Double>());
    		int total = unkownTotalTerms.get(i);
    		for(Iterator<Map.Entry<String, Integer>> iterator = unknowns.get(i).entrySet().iterator();
    				iterator.hasNext();) {
    			Map.Entry<String, Integer> entry = iterator.next();
    			String curKey = entry.getKey().toLowerCase();
    			int value = entry.getValue();
    			unknownTF.get(i).put(curKey, (double) value / total);
            }
    	}
		double[][] unknownTFIDF = new double[unknownCnt][numberOfDocumentsWithTheTerm.size()];
		for(int i = 0; i < unknownCnt; i++) {
			for(Iterator<Map.Entry<String, Double>> iterator = unknownTF.get(i).entrySet().iterator();
					iterator.hasNext();) {
				Map.Entry<String, Double> entry = iterator.next();
				String curKey = entry.getKey().toLowerCase();
				if(metaDataIndex.containsKey(curKey)) {
					int index = metaDataIndex.get(curKey);
					double idf = Math.log((double) (totalnumberOfDocuments + 1) / (numberOfDocumentsWithTheTerm.get(index) + 1));
					double tf = entry.getValue();
					unknownTFIDF[i][index] = tf * idf;
				}
			}
		}
		double[] curVec = new double[numberOfDocumentsWithTheTerm.size()];
		double[] reducedVec;
		double[][] tfidfCopy = new double[totalnumberOfDocuments + 1][numberOfDocumentsWithTheTerm.size()];
		for(int i = 0; i < totalnumberOfDocuments; i++) {
			for(int j = 0; j < numberOfDocumentsWithTheTerm.size(); j++) {
				tfidfCopy[i][j] = tfidf[i][j];
			}
		}
		changeTargetMatrix();
		String[] labels = new String[4];
		String[] temp = evaluation.getFinalClusterLabels();
		int[][] newClusters = addUnknownToClusterMatrix(clusters);
		for(int i = 0; i < 3; i++) labels[i] = temp[i];
		int[] Ks = new int[10];
		for(int i = 0; i < 10; i++) Ks[i] = (i + 1);
		labels[3] = "Unknown Data";
		for(int i = 0; i < unknownCnt; i++) {
			for(int j = 0; j < numberOfDocumentsWithTheTerm.size(); j++) {
				tfidfCopy[totalnumberOfDocuments][j] = unknownTFIDF[i][j];
			}
			pcaResults = DimensionalityReduction.PCA(tfidfCopy, 2, "UnkownPCA" + (i + 1) + ".csv");
			Map<Double, Integer> results = myKNN.doKNN(pcaResults, totalnumberOfDocuments);
			System.out.println(myKNN.getResults("Unknown" + (unknownCnt - i), results, Ks, newClusters, labels));
			DimensionalityReduction.PCA(tfidfCopy, 3, "UnkownPCA3D" + (i + 1) + ".csv");
			new Visualization("unknown" + (unknownCnt - i) + ".png", "My Clusters", labels, newClusters, pcaResults);
		}
    
	}
	
	// Method for loading all the text documents in a folder.
	public static void loadDocuments(File file) throws IOException {
		int foldersCount = 0;
		for(File folder : file.listFiles()) {
			if(folder.isDirectory()) {
				// Creating folders for each 2D array lists.
				documents.add(new ArrayList<String>());
				totalTerms.add(new ArrayList<Integer>());
				termCounter.add(new ArrayList<Map<String, Integer>>());
				TF.add(new ArrayList<Map<String, Double>>());
				IDF.add(new ArrayList<Map<String, Double>>());
				int documentsCounter = 0;
				for(File doc : folder.listFiles()) {
					if(doc.getName().endsWith(".txt")) {
						documents.get(foldersCount).add(new String(Files.readAllBytes(Paths.get(doc.getPath()))));
						termCounter.get(foldersCount).add(new HashMap<String, Integer>());
						TF.get(foldersCount).add(new HashMap<String, Double>());
						IDF.get(foldersCount).add(new HashMap<String, Double>());
						documentsCounter++;
					}
				}
				docsPerFolder.add(documentsCounter);
				totalnumberOfDocuments += documentsCounter;
				foldersCount++;
			}
		}
		folders = foldersCount;
	}
	
	// Method for loading stop words.
	public static void loadStopwords(File stopwordsFile) throws IOException {
		String document = new String(Files.readAllBytes(Paths.get(stopwordsFile.getPath())));
		StringTokenizer stopWords = new StringTokenizer(document);
    	while(stopWords.hasMoreTokens()) stopwords.add(stopWords.nextToken());
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
			if(!stopwords.contains(word.toLowerCase())) {
				total++;
				processed.append(word + " ");
				if(termCounter.containsKey(word)) termCounter.replace(word, termCounter.get(word) + 1);
				else termCounter.put(word, 1); 
			}
		}
		termCounter = threshold(termCounter, 0);
		total = 0;
		for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.entrySet().iterator(); iterator.hasNext();) {
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
	
	// Method for loading n-grams.
	public static Map<String, Integer> ngram(String document, int n) {
		Map<String, Integer> ngrams = new HashMap<String, Integer>();
		for(Iterator<Map.Entry<String, Integer>> iterator = ngram_creator(document, n).entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Integer> entry = iterator.next();
			String curKey = entry.getKey();
			int value = entry.getValue();
			if(ngrams.containsKey(curKey)) ngrams.replace(curKey, ngrams.get(curKey) + value);
			else ngrams.put(curKey, value);
        }
		return ngrams;
	}
	// Method for removing n-grams with occurences less than the given threshold.
	public static Map<String, Integer> threshold(Map<String, Integer> ngram, int threshold) {
		curNgram = 0;
		for(Iterator<Map.Entry<String, Integer>> iterator = ngram.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Integer> entry = iterator.next();
			int value = entry.getValue();
			if(value <= threshold) iterator.remove();
			else curNgram += value;
        }
		return ngram;
	}
	
	
	
	public static void getCommons() {
		for(int i = 0; i < folders; i++) common.add(new HashMap<String, Integer>());
		Map<String, Integer> temp = new HashMap<String, Integer>();
		Map<String, Integer> temp2 = new HashMap<String, Integer>();
		for(int i = 0; i < folders; i++) {
			for(int j = 0; j < docsPerFolder.get(i); j++) {
				if(j == 0) {
					for(Iterator<Map.Entry<String, Integer>> iterator = sortByValue(termCounter.get(i).get(j)).entrySet().iterator(); iterator.hasNext();) {
						Map.Entry<String, Integer> entry = iterator.next();
						String curKey = entry.getKey().toLowerCase();
						int value = entry.getValue();
						temp.put(curKey, value);
			        }
				}
				else {
					for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).get(j).entrySet().iterator(); iterator.hasNext();) {
						Map.Entry<String, Integer> entry = iterator.next();
						String curKey = entry.getKey().toLowerCase();
						if(temp.containsKey(curKey)) {
							int value = entry.getValue();
							int original = temp.get(curKey);
							temp2.put(curKey, value + original);
						}
			        }
					temp.clear();
					temp.putAll(temp2);
					temp2.clear();
				}
			}
			common.get(i).putAll(temp);
		}
	}
	
	
	public static void amplify() {
		for(int i = 0; i < folders; i++) {
			for(int j = 0; j < docsPerFolder.get(i); j++) {
				for(Iterator<Map.Entry<String, Integer>> iterator = common.get(i).entrySet().iterator(); iterator.hasNext();) {
					Map.Entry<String, Integer> entry = iterator.next();
					String curKey = entry.getKey().toLowerCase();
					int value = entry.getValue() << 3;
					termCounter.get(i).get(j).replace(curKey, 
							termCounter.get(i).get(j).get(curKey) << 5 
//							+ value
							);
				}
			}		
		}
	}
	
	public static void recompute() {
		for(int i = 0; i < folders; i++) {
			for(int j = 0; j < docsPerFolder.get(i); j++) {
				Map<String, Integer> temp = threshold(termCounter.get(i).get(j), 1);
				int total = 0;
				for(Iterator<Map.Entry<String, Integer>> iterator = termCounter.get(i).get(j).entrySet().iterator(); iterator.hasNext();) {
					Map.Entry<String, Integer> entry = iterator.next();
					total += entry.getValue();
				}
				totalTerms.get(i).set(j, total);
			}
		}
	}

	// A method that implements sliding window technique to create 
	// n-grams. It takes the document to process and integer value n
	// that denotes the length of the window.
	public static Map<String,Integer> wordCounter(String document) {
        String[] docArray = document.split(" ");
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(String word : docArray) {
			if(map.containsKey(word)) map.replace(word, map.get(word) + 1);
			else map.put(word, 1);
		}
		return map;
	}
	public static String printMap(Map<String, Integer> map) {
		StringBuilder output = new StringBuilder();
		for(Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Integer> entry = iterator.next();
			String curKey = entry.getKey();
			int value = entry.getValue();
			output.append(curKey + ": " + value + "\n");
        }
		return output.toString();
	}
	
	// Method storing text files.
	public static void storeTextFile(String title, String content) throws IOException {
		try(FileWriter writer = new FileWriter(title + ".txt");
			BufferedWriter bw = new BufferedWriter(writer)) {
			bw.write(content);
			bw.close();	
 		}
		catch (IOException e) { 
			System.err.format("IOException: %s%n", e);
		}	    
	}
	
	// A method that implements sliding window technique to create 
	// n-grams. It takes the document to process and integer value n
	// that denotes the length of the window.
	public static Map<String,Integer> ngram_creator(String document, int n) {
        String[] grams = document.split(" ");
		Map<String, Integer> ngrams = new HashMap<String, Integer>();
		int range = grams.length - n;
		StringBuilder ngram;
		String cur;
		for(int i = 0; i <= range; i++) {
			ngram = new StringBuilder();
			for(int j = i; j < i + n; j++) {
				if(i != j) ngram.append(" ");
				ngram.append(grams[j]);
			}
			cur = ngram.toString();
			if(ngrams.containsKey(cur)) ngrams.replace(cur, ngrams.get(cur) + 1);
			else ngrams.put(cur, 1);
		}
		return ngrams;
	}
	
	// Method for sorting the components of the vector by their values.
	public static Map<String, Integer> sortByValue(Map<String, Integer> component) { 
		return component.entrySet().stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }
	
	// Method for generating target (clustering) matrix.
	public static void makeTargetMatrix() {
		for(int i = 0, j = 0; j < 24; j++) {
        	if(j == 8 || j == 16) i++;
        	target[i][j]++;
        }
	}
	
	public static void changeTargetMatrix() {
		target = new int[4][25];
		for(int i = 0, j = 0; j < 25; j++) {
        	if(j == 8 || j == 16 || j == 24) i++;
        	target[i][j]++;
        }
	}
	
	public static int[][] addUnknownToClusterMatrix(int[][] cluster) {
		int[][] newCluster = new int[4][25];
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 24; j++) {
				newCluster[i][j] = cluster[i][j];
			}
		}
		newCluster[3][24] = 1;
		return newCluster;
	}
}
