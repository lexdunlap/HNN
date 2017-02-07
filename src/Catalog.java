import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * 
 */


/**
 * @author dunla
 *
 */
public class Catalog {
	
	public double[] duringTest;
	public double[] postTest;
	public int[][] tMatrix;
	public int[] inputs;
	public String fileName;
	public FileWriter fw;
	
	public Catalog() throws IOException{
		this.fileName = "log1";
		File file = new File(fileName + ".csv");
		this.fw = new FileWriter(file);
	}
	
	public void setDuringTest(double[] dt){
		this.duringTest = dt;		
	}
	
	public void setPostTest(double[] pt){
		this.postTest = pt;		
	}
	
	public void setTMatrix(int[][] tm){
		this.tMatrix = tm;
	}
	
	public void setInputs(int[] i){
		this.inputs = i;
	}
	
	public void printToFile() throws IOException{
		fw.append("T Matrix\n");
		for (int i = 0; i < tMatrix.length; i++){
			for (int j = 0; j < tMatrix[0].length; j++){
				String value = Integer.toString(tMatrix[i][j]);
				fw.append(value + ",");
			}
			fw.append("\n");
		}
		fw.append("\n\nInputs\n");
		for (int i = 0; i < inputs.length; i++){
			String value = Integer.toString(inputs[i]);
			fw.append(value + ",");
		}
		fw.append("\n\nPre-convergance outputs:\n");
		
		for (int i = 0; i < duringTest.length; i++){
			String value = Double.toString(duringTest[i]);
			fw.append(value + ",");
		}
		fw.append("\n\nPost-convergance outputs:\n");
		
		for (int i = 0; i < postTest.length; i++){
			String value = Double.toString(postTest[i]);
			fw.append(value + ",");
		}
	}
	
	public void closeFileWriter() throws IOException{
		fw.close();
	}

}
