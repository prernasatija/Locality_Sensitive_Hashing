package Phase3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

public class Task1 {
	static ArrayList<HashSet<Word>> files = new ArrayList<HashSet<Word>>();
	static ArrayList<Word> dimensions = new ArrayList<Word>();
	static ArrayList<Integer> lexiMapping = new ArrayList<>();

	private static String wordFilePath = "../WordFiles/epidemic_word_file.csv";
	private static String queryWordFilePath = "../WordFileQuery/epidemic_word_file_query.csv";
	public static void main(String[] args) {
		readCsvWordFile(wordFilePath);
		System.out.println("We have "+dimensions.size()+" dimensions");
		
		LSH lsh = new LSH();
		System.out.println("Enter the value of k:");
		Scanner s = new Scanner(System.in);
		lsh.kHashFunctions = s.nextInt();
		System.out.println("Enter the number of layers:");
		lsh.layers = s.nextInt();
		lsh.createGfunctions();
		lsh.createLSHIndex();
		System.out.println("Index created and used "+ lsh.memoryUsedByIndex +" bytes");
		
		/*System.out.println("Enter query file number");
		String query;
		while(!(query = s.next()).equals("quit")) {
			ArrayList<Map.Entry<Integer,Integer>> results = lsh.search(files.get(Integer.parseInt(query)-1));
			System.out.println("Input the results you want");
			int t = s.nextInt();
			for(int i = 0; i < t && i < results.size(); i++) {
				Map.Entry<Integer, Integer> r = results.get(i);
				System.out.println((r.getKey()+1)+"  sim: "+r.getValue());
			}
			System.out.println("Number of Overall Vectors Considered: "+lsh.overallVectors);
			System.out.println("Number of Unique Vectors Considered: "+lsh.vectorsConsidered);
			System.out.println("Memory Accessed: "+lsh.memoryAccessed + " bytes");
			System.out.println("Enter query file number or quit");
		}
		s.close();*/
		
		
		// actually code using matlab
		System.out.println();
		System.out.println("Ready to search? (yes/no)");
		while(!(s.next().equals("no"))) {
			try {
				QueryFileCreationMatlab.main(null);
			} catch (MatlabConnectionException e) {
				System.out.println("ERROR: Unable to connect to Matlab");
				break;
				//e.printStackTrace();
			} catch (MatlabInvocationException e) {
				System.out.println("ERROR: Matlab invocated an exception");
				break;
				//e.printStackTrace();
			}
			HashSet<Word> queryFile = readQueryCsvWordFile(queryWordFilePath);
			ArrayList<Map.Entry<Integer,Integer>> results = lsh.search(queryFile);
			System.out.println("Input the num of results you want");
			int t = s.nextInt();
			for(int i = 0; i < t && i < results.size(); i++) {
				Map.Entry<Integer, Integer> r = results.get(i);
				int fileNo = r.getKey() + 1;
				System.out.println(fileNo + "  sim: " + r.getValue());
			}
			System.out.println("Number of Overall Vectors Considered: "+lsh.overallVectors);
			System.out.println("Number of Unique Vectors Considered: "+lsh.vectorsConsidered);
			System.out.println("Memory Accessed: "+lsh.memoryAccessed + " bytes");
			System.out.println();
			System.out.println("Ready to search again? (yes/no)");
		}
		s.close();
		
	}
	
	//read query epidemic word file
	public static HashSet<Word> readQueryCsvWordFile(String wordFile) {
		BufferedReader br = null;
		String line = "";
		HashSet<Word> queryFile = new HashSet<>();
		try {
			br = new BufferedReader(new FileReader(wordFile));
			while ((line = br.readLine()) != null) {
	 				String[] windows = line.split(",");
	 				Word word = new Word(windows, 3);
	 				queryFile.add(word);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return queryFile;
	}

	//read epidemic word file for all simulations
	public static void readCsvWordFile(String wordFile) {
		BufferedReader br = null;
		String line = "";
		try {
			HashSet<Word> dimensionSet = new HashSet<Word>();
			br = new BufferedReader(new FileReader(wordFile));
			// path 01
			int fileCount = 0;
			while((line = br.readLine()) != null) {
 				String[] windows = line.split(",");
 				fileCount = Integer.parseInt(windows[0]);
			}
			br.close();
			
			// Creating hashsets in the files ds
			for(int i=0; i < fileCount; i++)
			{
				HashSet<Word> words = new HashSet<>();
				files.add(words);
			}
			createLexiMapping(0, fileCount);
			
			// patch end
			br = new BufferedReader(new FileReader(wordFile));
			while ((line = br.readLine()) != null) {
	 				String[] windows = line.split(",");
	 				int indexNo = Integer.parseInt(windows[0]);
	 				Word word = new Word(windows, 3);
	 				addToFileList(lexiMapping.indexOf(indexNo), word);
	 				addNewDimensions(word, dimensionSet);
			}
			dimensions.addAll(dimensionSet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void addToFileList(int fileNo, Word word) {
		HashSet<Word> words;
		if(files.size() < fileNo) {
			words = new HashSet<>();
			files.add(words);
			System.out.println("SHOULD NOT HAPPEN");
		}
		else {
			words = files.get(fileNo);
		}
		words.add(word);
	}
	
	//add new dimensions to the unique dimension set
	public static void addNewDimensions(Word word, HashSet<Word> dimensionSet) {
		if(!dimensionSet.contains(word))
			dimensionSet.add(word);
	}
	
	//lexicographic ordering in epidemic word file
	public static void createLexiMapping(int num, int input) {
		if(num > input) {
			return;
		}
		int i = 1;
		if(num != 0) {
			i = 0;
		}
		for(; i<10; i++) {
			if(num+i > input) {
				return;
			}
			lexiMapping.add(num + i);
			createLexiMapping((num+i)*10, input);
		}
	}
}
