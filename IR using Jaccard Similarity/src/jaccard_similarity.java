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

class DictEntry{

    public int doc_freq = 0; // number of documents that contain the term
    public int term_freq = 0; //number of times the term is mentioned in the collection
    public HashSet<Integer> postingList;

    DictEntry() {
        postingList = new HashSet<Integer>();
    }
}




//------------------------------------------------------------------------------------------------





class Index{

    //--------------------------------------------
    Map<Integer, String> sources;  // store the doc_id and the file name
    HashMap<String, DictEntry> index; // THe inverted index
    //--------------------------------------------

    Index() {
        sources = new HashMap<Integer, String>();
        index = new HashMap<String, DictEntry>();
    }

    //---------------------------------------------
    
    public void printPostingList(HashSet<Integer> hset) {
        Iterator<Integer> it2 = hset.iterator();
        while (it2.hasNext()) {
            System.out.print(it2.next() + ", ");
        }
        System.out.println("");
    }
    
    public void printDictionary() {
   
        System.out.println("------------------------------------------------------");
        System.out.println("              Number of terms = " + index.size());
        System.out.println("              Number of docs = " + sources.size());
        System.out.println("------------------------------------------------------");

    }

    
 //-----------------------------------------------------------------------   
    
    
    public void buildIndex(String[] files) {
        int i = 0;
        for (String fileName : files) {
            try ( BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                sources.put(i, fileName);
                String ln;
                while ((ln = file.readLine()) != null) {
                    String[] words = ln.split("\\W+");
                    for (String word : words) {
                        word = word.toLowerCase();
                        // check to see if the word is not in the dictionary
                        if (!index.containsKey(word)) {
                            index.put(word, new DictEntry());
                        }
                        // add document id to the posting list
                        if (!index.get(word).postingList.contains(i)) {
                            index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term 
                            index.get(word).postingList.add(i); // add the posting to the posting:ist
                        }
                        //set the term_fteq in the collection
                        index.get(word).term_freq += 1;
                    }
                }

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            i++;
        }
         printDictionary();
    }

}
    
//----------------------------------------------------------------------------  





public class jaccard_similarity {
	HashMap<Integer,float[]> docs_score = new HashMap<Integer,float[]>();	
	//DocID maps to intersection,union,score
	
	Index index;
	jaccard_similarity(String doc_location,String [] query) throws IOException{
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
			score(query);
	}
	
	public void get_intersection(String query) {
		String[] words = query.split("\\W+") ;
		
		for( String word : words) {				// to get the intersection 
			if(index.index.containsKey(word)) {		// if the word is in the index(has a posting list)
				for(Integer docID : index.index.get(word).postingList) {	// iterate the posting list 
					if(docs_score.containsKey(docID)) {	
						float intersect = docs_score.get(docID)[0];
						docs_score.get(docID)[0] = intersect+1;
					}else {
						docs_score.put(docID, new float[]{1,0});
					}
				}
			}
		}
	}
	
	//---------------------------------------------------------------
	
	
	public void get_union(String query) throws IOException {
		String ln;
		HashSet<String> distinctWords = new HashSet<String>();	
		// to calculate distinct words number of query U each doc
		
		String[] words = query.split("\\W+");
		for (String word:words) {						// add all query words in the set
			distinctWords.add(word);
		}
		
		for(Integer docID: docs_score.keySet()) {		// iterate docs that have an intersection
			
			BufferedReader doc = new BufferedReader(new FileReader(index.sources.get(docID)));
			
			while ((ln = doc.readLine()) != null) {
                String[] ln_words = ln.split("\\W+");
                for (String word : ln_words) {
                	distinctWords.add(word);
                }
			}
			Integer union = distinctWords.size();		// setting union equal to distinct combined words
			docs_score.get(docID)[1] =  union;		// setting union value to the array 		
			distinctWords.clear();						// reset distinct words for the next doc
		}
	
	}
	
	
	//---------------------------------------------------------------
	
	
	public void get_score() {
		HashMap<Integer,Float> docs_jscore = new HashMap<Integer,Float>();	// store each doc with its score
		
		for(Integer docid: docs_score.keySet()) {							// calculate the score
			Float score = docs_score.get(docid)[0]/docs_score.get(docid)[1];
			docs_jscore.put(docid, score);
		}
		
		// now sort the docs in descending order based on values
		
		Map<Integer, Float> sorted = docs_jscore ;
		sorted = docs_jscore
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(
		            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                LinkedHashMap::new));
		
		// Now print the ordered doc name with its jaccard Score 
		for(Integer docid : sorted.keySet()) {
			System.out.print("   "+index.sources.get(docid)+": ");
			System.out.println(sorted.get(docid));
		}
	}

	
	//---------------------------------------------------------------
	
	
	public void score(String[] queries) throws IOException {
		for(String query : queries) {
			System.out.println("Query->     "+query);
			System.out.println("Result: ");
			get_intersection(query);		// Now docs_score contains the number of intersection
			get_union(query);		    // Now it contains the score
			System.out.println();
			get_score();
			System.out.println();
			System.out.println();
			docs_score.clear();   			// reset scores for the next query
		}
	}
}
