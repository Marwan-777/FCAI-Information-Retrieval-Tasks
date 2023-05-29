import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

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
    
      
    
//----------------------------------------------------------------------------  
    

    HashSet<Integer> intersect(HashSet<Integer> pL1, HashSet<Integer> pL2) {
        HashSet<Integer> answer = new HashSet<Integer>();
        Iterator<Integer> itP1 = pL1.iterator();
        Iterator<Integer> itP2 = pL2.iterator();
        int docId1 = 0, docId2 = 0;

        if (itP1.hasNext()) 
            docId1 = itP1.next();        
        if (itP2.hasNext()) 
            docId2 = itP2.next();
        
        while (itP1.hasNext() || itP2.hasNext()) {
            if (docId1 == docId2) {

                answer.add(docId1);
                if( !(itP1.hasNext() && itP2.hasNext())) {		// if one of them is finished
                	break;
                }
                docId1 = itP1.next();
                docId2 = itP2.next();
            } 
            else if (docId1 < docId2) {               
                if (itP1.hasNext()) 
                    docId1 = itP1.next();
                 else return answer;
                
            } else {
                if (itP2.hasNext()) 
                    docId2 = itP2.next();
                else return answer;
            }
        }
        if (docId1 == docId2) {
            answer.add(docId1);
        }

        return answer;
    }
    
   
//-----------------------------------------------------------------------   

    HashSet<Integer> disjunction (HashSet<Integer> pl1 , HashSet<Integer> pl2){
    	HashSet<Integer> answer = new HashSet<Integer>();
        Iterator<Integer> itP1 = pl1.iterator();
        Iterator<Integer> itP2 = pl2.iterator();
        int docId1 = 0, docId2 = 0;

        if (itP1.hasNext()) 
            docId1 = itP1.next();        
        if (itP2.hasNext()) 
            docId2 = itP2.next();
        
        while (itP1.hasNext() || itP2.hasNext()) {
            if (docId1 == docId2) {

                answer.add(docId1);
                if(itP1.hasNext()) {
                	docId1 = itP1.next();
                }
                if(itP2.hasNext()) {
                	docId2 = itP2.next();
                }
            }
            else {
            	answer.add(docId1);
            	answer.add(docId2);
            	if(itP1.hasNext()) {
                	docId1 = itP1.next();
                }
                if(itP2.hasNext()) {
                	docId2 = itP2.next();
                }
            }
            
        }
        answer.add(docId1);
        answer.add(docId2);
        
        TreeSet mysorted = new TreeSet();
        mysorted.addAll(answer);
        answer = new HashSet<Integer>(mysorted);		// to make sure it is sorted
        
        return answer;
    }
   
//-----------------------------------------------------------------------  
    
    
    HashSet<Integer> negation (HashSet<Integer> pl){
    	Iterator<Integer> docIds = sources.keySet().iterator();
    	HashSet<Integer> ans = new HashSet<Integer>();
    	int doc = 0;
    	while(docIds.hasNext()) {
    		doc = docIds.next();
    		if(!pl.contains(doc)) {				// if the posting list doesn't contain the docID
    			ans.add(doc);
    		}
    	}
    	
    	return ans;
    }
    
    
//-----------------------------------------------------------------------  

    public void find(String query) {
    	String[] words = query.split("\\W+");
    	String operators[] = {"AND","OR","NOT"};	// all possible operators
    	HashSet<Integer> ans = new HashSet<Integer>();
    	HashSet<Integer> pl ;
    	HashSet<Integer> emptylist = new HashSet<Integer>(); 	// one empty set for any word no in index
    	String operation = "";
    	
    	for(int i =0 ;i<words.length;i++) {
    		
    		if( !Arrays.stream(operators).anyMatch(words[i]::equals)) { 	// check if the word is operator or not
    			words[i] = words[i].toLowerCase();
    			if(!index.containsKey(words[i])) {			// the word is not in the collection
    	    		// skip the word by making empty postinglist
    				pl = emptylist;
    	    	}
    			else {
    				pl = index.get(words[i]).postingList;

    			}
    			
    			
    			if(!operation.equals("")){			// to apply the operation
    				if(operation.equals("AND")) {
    					ans = intersect(ans,pl);
    				}
    				else {	// operation = OR
    					ans = disjunction(ans,pl);
    				}
    				operation = "";
    			}
    			else {	// this means that the word is the first word in the query (no operations yet)
    				ans = pl;
    			}
    		}
    		
    		if(words[i].equals("NOT")) {
    			if(!index.containsKey(words[i+1])) {
    				pl = emptylist;
    			}
    			else {
    				pl = index.get(words[i+1]).postingList;
    			}
    			
    			
    			if(!operation.equals("")){			// to apply the operation
    				if(operation.equals("AND")) {
    					ans = intersect(ans,negation(pl));
    				}
    				else {	// operation = OR
    					ans = disjunction(ans,negation(pl));
    				}
    				operation = "";
    			}
    			else {								// NOT is at the beginning of the query 
    				ans = negation(pl);
    			}
    			
    			i++ ;                            // skip to the next word
    		}
    		else {								// then it's either OR or AND
    			operation = words[i];
    		}
    	}

    	
    	
    	/*
    	 * 
    	 * Now printing the result
    	 * 
    	 * */
    	
    	
    	if( !ans.isEmpty()) {		// if there is a result
    		for(int dic:ans) {
        		System.out.println("   "+sources.get(dic));
        	}
    	}
    	else {
    		System.out.println("    No Result found !  ");
    	}
    }
    
    
//-----------------------------------------------------------------------         
    
      
}

public class Inverted_index {
	Inverted_index(String doc_location,String [] query){
		
		Index index = new Index();

/*
  the following code allows to add/remove doc files directly without adding their loc to the code
*/
		
		File collection = new File(doc_location);
		String docs[] = collection.list();
		for (int i =0 ; i<docs.length;i++) {
			docs[i] = doc_location+"/"+docs[i]; // add the path to the file name to build the Index
		}
		index.buildIndex(docs);
		
		
		for (int i =0 ; i<query.length;i++) {
			System.out.println("Search ->   "+query[i]);
			System.out.println();
			System.out.println("Result :  ");
			index.find(query[i]);
			System.out.println();
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		
	}

}
