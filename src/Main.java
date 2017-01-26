import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * 
 */

/**
 * @author dunlap
 *
 */
public class Main {
	//instantiate rows columns and T Matrix variables
	
	private int rows = 0;
	private int columns = 0;
	
	private double[] output;
	private int nValue;
	private int[] input;
	private int[][] transition_table;
	
	public Main(String ip, String matrix) throws FileNotFoundException{
		FileReader(ip, "i");
		FileReader(matrix, "e");
		PrintMatrix("e");
		PrintMatrix("i");
		Hopfield_Network hn = new Hopfield_Network(1, nValue, 1, input, 2);
		output = hn.getOuput();
		PrintMatrix("o");
		
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
		nValue = rows;
		
		//determines the number of columns by splitting an element of the ArrayList into segments based on commas
		for (int i = 0; i < 1; i++){
			columns = lineArray[i].split("\\,", -1).length;
		}
		
		if (type == "e"){
			//initializes the T Matrix with the read number of rows and columns
			transition_table = new int[rows][columns];
			
			//srow and scol keep track of the current row and column being added to the T Matrix
			int srow = 0;
			int scol = 0;
			
			//Loop to input values into T Matrix
			for (int i = 0; i < rowArrays.size(); i++){
				//extract element of the ArrayList which holds rows of data and split them into an array that holds each element for the row
				String row = rowArrays.get(i);
				String[] columnArray = row.split(",");
				
				//For each element in the newly created array parse into an int and add to T Matrix at the location [srow][scol]
				for (int j = 0; j < columnArray.length; j++){
					if (columnArray[j].length() > 0){
						int num = Integer.parseInt(columnArray[j]);
						transition_table[srow][scol] = num;
					}
					else{
						transition_table[srow][scol] = 0;
					}
					scol++;
				}
				//add to the row value while resetting the column value to zero before next loop iteration
				scol = 0;
				srow++;
			}
		}
		
		else if (type == "i"){
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
			}
		}
		else{
			System.out.println("Error, incorrect inputs for data type");
		}
		this.rows = 0;
		this.columns = 0;
		newFile.close();
	
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
		new Main("smallInputs.csv", "smallNetwork.csv");

	}

}
