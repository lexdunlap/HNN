import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import tester.OutputTest;
import tester.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.stream.Stream;

/**
 * Main class for testing Hopfield_Network class.
 *
 * TODO: Fill out javadocs.
 */
public class Main {
	//instantiate rows columns and T Matrix variables
	
	private int rows = 0;
	private int columns = 0;
	
	private double[] output;
	private int nValue;
	private int[] input;
	private int[][] transition_table;

    // TODO: Migrate to read_inputs and read_weights methods for automatic testing.
    // TODO: Add loop for automatic testing.
	public Main(String in_file, String weight_file,
                int num_inputs, int num_neurons) throws FileNotFoundException{
//		FileReader(ip, "i");
//		PrintMatrix("i");
//		System.out.print(input);
        try {
            int[][] inputs = read_inputs(in_file, num_inputs, num_neurons);
            int[][][] weights = read_weights(weight_file, num_inputs, num_neurons);

            System.out.print("Initialisation done. Beginning testing now.\n");
            
            for (int i = 0; i < num_inputs; i++) {
                Hopfield_Network hn = new Hopfield_Network(1, num_neurons, 1, inputs[i], weights[i], .1);
                this.transition_table = hn.getTransitionTable();
                Test r = new Test(this.transition_table);
                String results = r.getResults();
                System.out.println(results);
                PrintMatrix("e");
                this.output = hn.getOuput();
                int kNum = hn.getKValue();
                OutputTest ot = new OutputTest(this.output, kNum);
                String outputResults = ot.getOutputResults();
                System.out.println(outputResults);
                PrintMatrix("o");
            }
        } catch (IOException e) {}
	}
	
	public void FileReader(String fileName, String type) throws FileNotFoundException{
		// creates variables for inputting data into an ArrayList
		ArrayList<String> rowArrays = new ArrayList<String>();
		String[] lineArray = null;

		// scanner reads in new file
		Scanner newFile = new Scanner(new File(fileName));

		//counts each new line as a row and adds each complete row as an element to the ArrayList
		while (newFile.hasNextLine()){
			this.rows++;
			String a = newFile.nextLine();
			lineArray = a.split("\n");
			Collections.addAll(rowArrays, lineArray);
		}

		//determines the number of columns by splitting an element of the ArrayList into segments based on commas
		for (int i = 0; i < 1; i++){
			columns = lineArray[i].split("\\,", -1).length;
		}

		if (type == "i"){
			//initializes the external inputs with the read number of rows and columns
			input = new int[rows];

			//srow and scol keep track of the current row and column being added to the external inputs
			int srow = 0;
			int scol = 0;

			//Loop to input values into external inputs
			for (int i = 0; i < rowArrays.size(); i++){
				//extract element of the ArrayList which holds rows of data and split them into an array that holds each element for the row
				String row = rowArrays.get(i);
				String[] columnArray = row.split(",");

				//For each element in the newly created array parse into an int and add to external inputs at the location [srow][scol]
				for (int j = 0; j < columnArray.length; j++){
					if (columnArray[j].length() > 0){
						int num = Integer.parseInt(columnArray[j]);
						input[srow] = num;
					}
					else{
						input[srow] = 0;
					}
					scol++;
				}
				//add to the row value while resetting the column value to zero before next loop iteration
				scol = 0;
				srow++;
				nValue = input.length;
			}
		}
		else{
			System.out.println("Error, incorrect inputs for data type");
		}
		this.rows = 0;
		this.columns = 0;
		newFile.close();

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
	public int[][] read_inputs(String filename, int num_in, int in_len) throws IOException
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
	public int[][][] read_weights(String filename, int num_tables, int in_len) throws IOException
    {
        int[][][] t_tables = new int[num_tables][in_len][in_len];
        int lc = 0, mxc = 0;
        Scanner file = new Scanner(new File(filename));
        boolean valid = true;

        while (file.hasNextLine() && valid)
        {
            String[] line = file.nextLine().split("\\s");
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

        return t_tables;
    }
	
	//method for printing the matrix to the console 
	public void PrintMatrix(String type){
		if (type == "e"){
			System.out.println("T Matrix:");
			for (int i = 0; i < transition_table.length; i++){
				for (int j = 0; j < transition_table[i].length; j++){
					System.out.print(transition_table[i][j]);
					if (j != transition_table[i].length - 1)
						System.out.print(", ");
				}
				System.out.print("\n");
			}
		}
		else if (type == "i"){
			System.out.println("External Inputs:");
			for (int i = 0; i < input.length; i++){
				System.out.print(input[i]);
				if (i != input.length - 1)
					System.out.print(", ");
			}
			System.out.print("\n");
		}
		else if (type == "o"){
			System.out.println("Outputs:");
			for (int i = 0; i < output.length; i++){
				System.out.print(output[i]);
				if (i != output.length - 1)
					System.out.print(", ");
			}
			System.out.print("\n");
		}
		else{
			System.out.println("Print Error: Incorrect file type");
		}
	}
	public static void main(String[] args) throws FileNotFoundException {
		// Create a new instance of the Main
		new Main("inputs.csv", "weights.csv", 3, 10);

	}

}
