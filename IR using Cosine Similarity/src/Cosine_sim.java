import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Comparator;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;









class Index{

    //--------------------------------------------
    Map<Integer, String> sources;  // store the doc_id and the file name
    HashSet<String> All_words; // store all possible words in all docs
    HashMap<Integer,ArrayList<String>> docs_words;  // store all words for each doc
    //--------------------------------------------

    Index() {
        sources = new HashMap<Integer, String>();
        All_words = new HashSet<String>();
        docs_words = new HashMap<Integer, ArrayList<String>>();
    //---------------------------------------------
    }
    
    public void printDictionary() {
   
        System.out.println("------------------------------------------------------");
        System.out.println("              Number of terms = " + All_words.size());
        System.out.println("              Number of docs = " + sources.size());
        System.out.println("------------------------------------------------------");

    }

    
 //-----------------------------------------------------------------------   
    
    
    public void buildIndex(String[] files) {
        int i = 0;
        ArrayList<String> docWords ;
        for (String fileName : files) {
            try ( BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                sources.put(i, fileName);
                String ln;
                docWords = new ArrayList<String>();
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+");
                    for (String word : words) {
                        word = word.toLowerCase();
                        All_words.add(word);
                        docWords.add(word);
                    }
                }
                docs_words.put(i,docWords);
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            i++;
        }
         printDictionary();
    }
}

    



//----------------------------------------------------------------------------


public class Cosine_sim {
	
	HashMap<Integer,ArrayList<Integer>> docs_words_freq = new HashMap<Integer,ArrayList<Integer>>();
	HashMap<Integer [] , Float> docs_scores = new HashMap<Integer[],Float>();
	Index index;
	Cosine_sim(String doc_location){
		 	index = new Index();

	/*
	  the following code allows to add/remove doc files directly without adding their loc to the code
	*/
			
			File collection = new File(doc_location);
			String docs[] = collection.list();
			for (int i =0 ; i<docs.length;i++) {
				docs[i] = doc_location+"/"+docs[i]; // add the path to the file name to build the Index
			}
			index.buildIndex(docs);
			find_freq();
			get_sim();
			print_sim_desc();
	}
	
	public void find_freq() {
		ArrayList<Integer> words_freq ;
		for(Integer docID : index.docs_words.keySet()) {
			words_freq = new ArrayList<Integer>();
			for (String word : index.All_words) {
				// get the frequency of each word in each document words
				words_freq.add(Collections.frequency(index.docs_words.get(docID), word));
			}
			docs_words_freq.put(docID,words_freq);
		}
	}
	public void get_sim() {
		
		Float dot = 0.0f;
		Float norm1 = 0.0f;
		Float norm2 = 0.0f;
		Float sim = 0.0f;
		Integer [] docpair  ;
		// 2 for loops to find all possible combinations between docs
		for (Integer doc1ID = 0 ; doc1ID<docs_words_freq.keySet().size()-1; doc1ID+=1) {
			for (Integer doc2ID = doc1ID+1; doc2ID<docs_words_freq.keySet().size();doc2ID+=1) {
				
				// find dot product
				for(Integer i = 0 ; i < index.All_words.size() ; i+=1) {
					dot += docs_words_freq.get(doc1ID).get(i)*docs_words_freq.get(doc2ID).get(i);
				}
				
				// find first norm
				for(Integer num : docs_words_freq.get(doc1ID)) {
					norm1+= (float)Math.pow(num,2);
				}
				norm1 =(float) Math.sqrt(norm1);
		
				// find second norm
				for(Integer num : docs_words_freq.get(doc2ID)) {
					norm2+= (float)Math.pow(num,2);
				}
				norm2 =(float) Math.sqrt(norm2);
			
				// find cos(theta)
				sim = dot/(norm1*norm2);

				docpair = new Integer [2];
				docpair[0] = doc1ID;
				docpair[1] = doc2ID;
				
				docs_scores.put( docpair,sim);
				
				dot = 0.0f;
				norm1 = 0.0f;
				norm2 = 0.0f;
		        sim = 0.0f;
			}
		}
	}
	public void print_sim_desc() {
	
		Map<Integer [], Float> sorted = docs_scores ;
		sorted = docs_scores
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(
		            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                LinkedHashMap::new));
		System.out.println(" Similarity between : ");
		for(Integer [] pair : sorted.keySet()) {
			System.out.print(index.sources.get(pair[0])+" AND ");
			System.out.print(index.sources.get(pair[1])+" : ");
			System.out.println(sorted.get(pair));
		}
	}
	
}
	

