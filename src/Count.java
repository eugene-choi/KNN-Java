import java.io.*;

public class Count {
	public static void main(String[] agrs) throws IOException {
		for(int i = 1; i <= 3; i++) {
			for(int j = 1; j <= 8; j++) {
				System.out.println(count(new File("./C" + i + "article0" + j + ".txt")));
			}
		}
	}
	public static int count(File file) throws IOException {
		int count = 0;
		BufferedReader br = new BufferedReader(new FileReader(file));
        String currentStopword;
    	while((currentStopword = br.readLine()) != null) {
    		String[] line = currentStopword.split(": ");
    		count += Integer.parseInt(line[1]);
    	}
		return count;
	}

}
