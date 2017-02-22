import tester.OutputTest;
import tester.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 *
 * @author
 * @version
 * @since SDK1.8
 */
public class Main {
	private int rows = 0;	            // instantiate rows columns and T Matrix variables
    private int columns = 0;            // instantiate rows columns and T Matrix variables
	private int numNeurons;             // Number of total neurons the network will have
	private int numVectors;             // Number of input vectors in total - each network iteration utilises one vector
    private int numCat;
    private double[] output;
    private int[][] transition_table;   // Weight matrix from the HopfieldNetwork class
    private int[][] inputs;             // inputs[i][j] gets element j from input vector i
    private int[][][] weights;          // weights[i][j][k] gets element (j,k) from weight matrix i

    // TODO: Add loop for automatic testing.

    /**
     * Constructor initialising inputs and weights for automatic runs.
     *
     * @param inFile File in which the input vectors for all desired runs are stored.
     * @param weightFile File in which weight matrices for all desired runs are stored.
     * @param numVectors Number of input vectors (also, number of total runs).
     * @param numNeurons Number of neurons - note, this is constant across all runs.
     */
	public Main(String inFile, String weightFile, int numVectors, int numNeurons, int numCat)
	{
		this.numNeurons = numNeurons;
		this.numVectors = numVectors;
        this.numCat = numCat;

        try {
            inputs = readInputs(inFile, numVectors, numNeurons);
            weights = readWeights(weightFile, numVectors, numNeurons);

            System.out.print("Initialisation done. Beginning testing now.\n");
            testNetwork();
        } catch (IOException e) { e.printStackTrace(); }
	}

    /**
     *
     * @throws IOException
     */
	private void testNetwork() throws IOException
	{
        ArrayList<Integer> k = initK();
        ArrayList<ArrayList> categories = categoriesNTQP();

        // Loops through and stabilises each input/weight pair.
		for (int i = 0; i < numVectors; i++) {
            /*
                TODO: Pull k/categories from a file. Alternatively, generate categories based on weight matrix.
             */
//            System.out.println(Arrays.toString(weights[i][0]));
            // Initial Hopfield stabilisation
            HopfieldNetwork hn = new HopfieldNetwork(
                    k, numNeurons, 1, .01, inputs[i], weights[i], .1, categories);
			this.transition_table = hn.getTransitionTable();

            // Transition table testing
			Test r = new Test(this.transition_table);
			String results = r.getResults();
			System.out.println(results);
			PrintMatrix("e", i);
            output = hn.getOuput();
			ArrayList<Integer> kNum = hn.getKValue();

            // Output vector testing
			OutputTest ot = new OutputTest(output, kNum, categories);
			String outputResults = ot.getOutputResults();
			System.out.println(outputResults);
			PrintMatrix("o", i);
		}
	}

    /**
     * Used to initialise the 'k' value for the 'k-out-of-n rule'.
     *
     * Currently sets all k-values in the ArrayList to 1 - this denotes a desire to have a singular
     * neuron from each category active at the time of stabilisation.
     *
     * @return ArrayList k detailing the desired number of active neurons from each category.
     */
	private ArrayList<Integer> initK()
    {
        ArrayList<Integer> k = new ArrayList<Integer>();
        for (int i = 0; i < numCat; i++)
        {
            k.add(1);
        }
        return k;
    }

    /**
     * Generates an ArrayList of ArrayLists, with each inner ArrayList representing a container holding
     * all categories a given neuron. The index of the encapsulating ArrayList represents the index of a
     * given neuron, and all values within the inner ArrayList are the categories in which the given
     * neuron is a member of. Neurons with a single value within this inner ArrayList are members of a
     * single set whereas neurons with multiple values are members of multiple sets.
     *
     * @return A fully instantiated ArrayList holding an ArrayList of Integers, representing neural categories.
     */
	private ArrayList<ArrayList> categoriesNTQP()
	{
		ArrayList<ArrayList> categories = new ArrayList<ArrayList>();
		for (int i = 0; i <= 3; i++)
		{
			for (int j = 4; j <= 7; j++)
			{
				ArrayList<Integer> cat = new ArrayList<Integer>();
				cat.add(i);
				cat.add(j);
				cat.toArray();
				categories.add(cat);
			}
		}
		return categories;
	}

	/**
	 * Generates the categories of each neuron based on the given weight matrix.
	 *
	 * @param tt Weight matrix (transition table) to create categories from.
	 */
	private void genCategories(int[][] tt)
	{
	}

    /**
     * Allows for automatic testing. Given a file with each row representing a different
     *  input vector, separates all given input vectors into a matrix with each component
     *  row vector corresponding to a different input vector.
     *
     * @param filename: Name of file where the input vectors are stored.
     * @param num_in: Number of total input vectors in the file.
     * @param in_len: Length of each input vector in the file. NOTE: All vectors must be the same length.
     * @return Returns matrix of input vectors.
     * @throws IOException From Scanner/File class.
     */
	private int[][] readInputs(String filename, int num_in, int in_len) throws IOException
	{
        int[][] inputs = new int[num_in][in_len];
        int lc = 0;
		Scanner file = new Scanner(new File(filename));
        while (file.hasNextLine())
        {
            inputs[lc] = Stream.of(file.nextLine().split("\\s+")).mapToInt(Integer::parseInt).toArray();
            lc++;
        }
        return inputs;
	}

    /**
     * Allows for automatic testing. Given a file of transition tables (weight matrices),
     *  reads in each matrix row by row. Matrices are separated by blank lines. The 3D int
     *  array used to store the collection of matrices is broken down as such:
     *      1. int[] represents the chosen matrix. Think of the first layer of the array
     *          as an index system for labeling the transition tables stored in the next
     *          two layers of the array.
     *      2. int[][] represents an entire row of the chosen transition table index.
     *      3. int[][][] represents a column element of the aforementioned row vector
     *          for the chosen matrix index.
     *      E.g. [1][s][t] gives element [s][t] of the first [1] matrix.
     *
     * @param filename: Name of file where the transition tables are stored.
     * @param num_tables: Number of complete transition tables in the file.
     * @param in_len: Number of columns (or rows) in the transition table. NOTE: #col == #row
     * @return Returns a 3D int array of transition tables.
     * @throws IOException From Scanner/File class.
     */
	private int[][][] readWeights(String filename, int num_tables, int in_len) throws IOException
    {
        int[][][] t_tables = new int[num_tables][in_len][in_len];
        int lc = 0, mxc = 0;
        Scanner file = new Scanner(new File(filename));
        boolean valid = true;

        while (file.hasNextLine() && valid)
        {
            String[] line = file.nextLine().split("\\s+");
            if (line.length == 1)
            {
                lc = 0;
                mxc++;
                if (mxc > num_tables)
                    valid = false;
            } else {
                t_tables[mxc][lc] = Stream.of(line).mapToInt(Integer::parseInt).toArray();
                lc++;
            }
        }

        for (int i = 0; i < num_tables; i++)
        {
            for (int j = 0; j < numNeurons; j++)
            {
                for (int k = 0; k < numNeurons; k++)
                {
                    t_tables[i][j][k] *= -1;
                }
            }
        }
        return t_tables;
    }

    /**
     * Method for printing all matrices to a console.
     *
     * @param type "e" for weights; "i" for external inputs; "o" for outputs
     * @param idx Current input vector / weight matrix pair (from file)
     */
	private void PrintMatrix(String type, int idx) {
        switch (type) {
            case "e":   System.out.println("T Matrix:");
                        for (int i = 0; i < transition_table.length; i++) {
                            for (int j = 0; j < transition_table[i].length; j++) {
                                System.out.print(transition_table[i][j]);
                                if (j != transition_table[i].length - 1)
                                    System.out.print(", ");
                            }
                            System.out.print("\n");
                        }

            case "i":   System.out.println("External Inputs:");
                        for (int i = 0; i < inputs[idx].length; i++) {
                            System.out.println(Arrays.toString(inputs[idx]));
//                            if (i != inputs[idx].length - 1)
//                                System.out.println(",");
                        }
                        System.out.print("\n");

            case "o":   System.out.println("Outputs:");
                        for (double val : output)
                            System.out.println("" + val);
//                        for (int i = 0; i < output.length; i++) {
//                            System.out.println(output[i]);
//                            if (i != output.length - 1)
//                                System.out.println(",");
//                        }
                        System.out.print("\n");

            default:    System.out.println("Print Error: Incorrect file type");

        }
    }

	public static void main(String[] args) throws FileNotFoundException {
        // Create a new instance of the Main
        new Main("inputs.csv", "perm_weights.txt", 1, 16, 8);

    }

//	public void FileReader(String fileName, String type) throws FileNotFoundException{
//		// creates variables for inputting data into an ArrayList
//		ArrayList<String> rowArrays = new ArrayList<String>();
//		String[] lineArray = null;
//
//		// scanner reads in new file
//		Scanner newFile = new Scanner(new File(fileName));
//
//		//counts each new line as a row and adds each complete row as an element to the ArrayList
//		while (newFile.hasNextLine()){
//			this.rows++;
//			String a = newFile.nextLine();
//			lineArray = a.split("\n");
//			Collections.addAll(rowArrays, lineArray);
//		}
//
//		//determines the number of columns by splitting an element of the ArrayList into segments based on commas
//		for (int i = 0; i < 1; i++){
//			columns = lineArray[i].split("\\,", -1).length;
//		}
//
//		if (type == "i"){
//			//initializes the external inputs with the read number of rows and columns
//			input = new int[rows];
//
//			//srow and scol keep track of the current row and column being added to the external inputs
//			int srow = 0;
//			int scol = 0;
//
//			//Loop to input values into external inputs
//			for (int i = 0; i < rowArrays.size(); i++){
//				//extract element of the ArrayList which holds rows of data and split them into an array that holds each element for the row
//				String row = rowArrays.get(i);
//				String[] columnArray = row.split(",");
//
//				//For each element in the newly created array parse into an int and add to external inputs at the location [srow][scol]
//				for (int j = 0; j < columnArray.length; j++){
//					if (columnArray[j].length() > 0){
//						int num = Integer.parseInt(columnArray[j]);
//						input[srow] = num;
//					}
//					else{
//						input[srow] = 0;
//					}
//					scol++;
//				}
//				//add to the row value while resetting the column value to zero before next loop iteration
//				scol = 0;
//				srow++;
//				numNeurons = input.length;
//			}
//		}
//		else{
//			System.out.println("Error, incorrect inputs for data type");
//		}
//		this.rows = 0;
//		this.columns = 0;
//		newFile.close();
//
//	}
}
