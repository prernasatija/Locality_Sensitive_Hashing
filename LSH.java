package Phase3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class LSH {
	
	public int layers;
	public int kHashFunctions;
	public int overallVectors;
	public int vectorsConsidered;
	public int memoryAccessed;
	public int memoryUsedByIndex;
	// list of hashmaps(key,list of buckets) where key is the concatenated output of g functions for a file
	// for each of the k random hash functions (g[i][j]) and bucket is the file index corresponding to the key
	static ArrayList<HashMap<String,ArrayList<Integer>>> indexes = new ArrayList<HashMap<String,ArrayList<Integer>>>();
	
	int[][] gFunctions;
	
	//create g composite hash functions
	public void createGfunctions() {
		gFunctions = new int[layers][kHashFunctions];
		for(int i = 0; i < layers; i++) { // for each layer
			Random rnd = new Random();
			HashSet<Integer> uniqueHash = new HashSet<>();
			for(int j = 0; j < kHashFunctions; ) { // define all the k hash functions
				int nextHash = rnd.nextInt(Task1.dimensions.size());
				if(!uniqueHash.contains(nextHash)) {
					uniqueHash.add(nextHash);
					gFunctions[i][j] = nextHash;
					j++;
				}
			}
		}
	}
	
	//Lsh index structure
	public void createLSHIndex() {
		for(int f = 0; f < Task1.files.size(); f++) {
			HashSet<Word> file = Task1.files.get(f);
			for(int l = 0; l<gFunctions.length; l++) {
				
				// get or create the corresponding hashMap for this g function
				HashMap<String,ArrayList<Integer>> gMap;
				if(indexes.size() > l) {
					gMap = indexes.get(l);
				}
				else {
					gMap = new HashMap<String,ArrayList<Integer>>();
					indexes.add(gMap);
				}
				
				// apply g function to the file
				String key = "";
				for(int k = 0; k<kHashFunctions; k++) {
					int val = hammingHash(gFunctions[l][k], file);
					key = key + String.valueOf(val);
				}
				
				// add this file to the gMap
				ArrayList<Integer> bucket;
				if(gMap.containsKey(key)) {
					bucket = gMap.get(key);
				}
				else {
					bucket = new ArrayList<>();
					gMap.put(key, bucket);
				}
				bucket.add(f);
			}
		}
		setUniqueVectorCount();
		setMemoryUsed();
	}
	//to get the overall unique vectors
	public void setUniqueVectorCount() {
		int size = 0;
		for(HashMap<String,ArrayList<Integer>> gMap : indexes) {
			size = size + gMap.size();
		}
		overallVectors = size;
	}
	
	public void setMemoryUsed() {
		int size = 0;
		for(HashMap<String,ArrayList<Integer>> gMap : indexes) {
			size = size + (gMap.size()*kHashFunctions*4 + Task1.files.size()*8);
		}
		memoryUsedByIndex = size;
	}
	
	// to search for the query file's key
	public ArrayList<Map.Entry<Integer,Integer>> search(HashSet<Word> queryFile) {
		
		vectorsConsidered = 0;
		memoryAccessed = 0;
		ArrayList<Map.Entry<Integer,Integer>> sim = new ArrayList<>();
		HashMap<Integer, Integer> simMap = new HashMap<>();
		for(int l = 0; l<gFunctions.length; l++) {
			
			// get or create the corresponding hashMap for this g function
			HashMap<String,ArrayList<Integer>> gMap = indexes.get(l);
			
			// apply g function to the file
			String key = "";
			for(int k = 0; k<kHashFunctions; k++) {
				int val = hammingHash(gFunctions[l][k], queryFile);
				key = key + String.valueOf(val);
			}

			if(gMap.containsKey(key)) {
				ArrayList<Integer> bucket = gMap.get(key);
				memoryAccessed = memoryAccessed + key.length()*4 + bucket.size()*8;
				for(Integer f : bucket) {
					if(simMap.containsKey(f)) {
						int value = simMap.get(f);
						simMap.put(f, value+1);
					}
					else {
						simMap.put(f,  1);
					}
				}
				vectorsConsidered++;
			}
		}
		sim.addAll(simMap.entrySet());
		Collections.sort(sim, sortByValue);
		return sim;
	}
	
	// comparator to compare the bucket size
	Comparator<Map.Entry<Integer, Integer>> sortByValue = new Comparator<Map.Entry<Integer, Integer>>() {
		public int compare(Map.Entry<Integer, Integer> h1, Map.Entry<Integer, Integer> h2) {
			return h2.getValue().compareTo(h1.getValue());
		}
	};

	//hash function based on hamming distance
	public int hammingHash(int dimId, HashSet<Word> file) {
		if(file.contains(Task1.dimensions.get(dimId))) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
