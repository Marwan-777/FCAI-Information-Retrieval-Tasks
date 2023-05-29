import java.io.IOException;

public class main {

	/*
	 * Marwan Yasser Mostafa
	 * 20180270
	 * */
	public static void main(String[] args) throws IOException {
		
		/*
		 * 
		 * To change the collection all you need to do is to add a folder of new docs to the project folder
		 * and pass the name of the collection folder to the Inverted_index constructor
		 * instead of "collection"
		 * 
		 * */
		String query[] = {
				"sleep",
				"programming language"
		};
        jaccard_similarity sim = new jaccard_similarity("collection2",query);
	}

}
