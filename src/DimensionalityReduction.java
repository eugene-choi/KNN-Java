import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import smile.projection.PCA;

public class DimensionalityReduction {
	
	
	public static double[][] PCA(double[][] vectors, int newDimension, String title) throws IOException, FileNotFoundException {
		File CSVpath = new File("./");
		String CSVfileName = (title.endsWith(".csv")) ? title : "PCA.csv";
		double[][] vectorsCopy = Arrays.stream(vectors).map(double[]::clone).toArray(double[][]::new);
		
		System.out.println("\n- Applying Principal Component Analysis(PCA):\n");
		PCA pca = new PCA(vectorsCopy);
		pca.setProjection(newDimension);
		double[][] pcaData = pca.project(vectorsCopy);
		
		
		System.out.println("SUCCESS: Principal Component Analysis(PCA) applied!");
		pcaData = checkDuplicatePoints(pcaData);
		
		printToCSV(CSVpath, CSVfileName, pcaData);
		System.out.println("\t(details about PCA can be found in the csv file decribed below)");
		System.out.println("File name: " + CSVfileName);
		System.out.println("File location: " + CSVpath.getAbsolutePath());
		
		return pcaData;
	}
	
	public static double[][] checkDuplicatePoints(double[][] pcaData) {
		HashMap<String, String> dupChecker = new HashMap<String, String>();
		double epsilon = 0.01; 
		boolean add = true;
		for(int i = 0; i < pcaData.length; i++) {
			double[] points = pcaData[i];
			String x = String.format("%.5f", points[0]);
			String y = String.format("%.5f", points[1]);
			if(dupChecker.containsKey(x) && dupChecker.get(x).equals(y)) {
//				if(add) {
					pcaData[i][0] += epsilon;
					pcaData[i][1] += epsilon;
//					add = false; 
//				}
//				else {
//					pcaData[i][0] -= epsilon;
//					pcaData[i][1] -= epsilon;
//					add = true; 
//				}
				epsilon += 0.005;
			}
			else dupChecker.put(x, y);
		}
		return pcaData;
	}
	
	
	public static void printToCSV(File csvLocation, String fileName, double[][] pca) throws IOException, FileNotFoundException {
		File csv = new File(csvLocation, fileName);
		
		try(FileWriter writer = new FileWriter(csv);
			BufferedWriter bw = new BufferedWriter(writer)) {
			
			int rows = pca.length;
			int cols = pca[0].length;

			// Headers:
			for(int i = 1; i <= cols; i++) {
				bw.write("Principal Component " + i);
				bw.write((i == cols) ? "\n" : ",");
			}
			
			// Values:
			for(int i = 0; i < rows; i++) {
				for(int j = 0; j < cols; j++) {
					bw.write(Double.toString(pca[i][j]));
					bw.write((j == (cols - 1)) ? "\n" : ",");
				}
			}
 		}
		catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
	}
	
	public static void printToCSV(File csvLocation, String fileName, int[][] pca) throws IOException, FileNotFoundException {
		File csv = new File(csvLocation, fileName);
		
		try(FileWriter writer = new FileWriter(csv);
			BufferedWriter bw = new BufferedWriter(writer)) {
			
			int rows = pca.length;
			int cols = pca[0].length;

			// Headers:
			for(int i = 1; i <= cols; i++) {
				bw.write("Principal Component " + i);
				bw.write((i == cols) ? "\n" : ",");
			}
			
			// Values:
			for(int i = 0; i < rows; i++) {
				for(int j = 0; j < cols; j++) {
					bw.write(Double.toString(pca[i][j]));
					bw.write((j == (cols - 1)) ? "\n" : ",");
				}
			}
 		}
		catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		}
	}
}