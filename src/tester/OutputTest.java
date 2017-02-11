/**
 * 
 */
package tester;

import java.util.Arrays;

/**
 * @author dunla
 */
public class OutputTest {
	private String outputResponse;
	
	public OutputTest(double[] v, int[] kNum, int[] cat){
		outputResponse = testOutput(v, kNum, cat, 0.01);
	}
	
	
	public String getOutputResults(){
		return outputResponse;
	}
	
	private String testOutput(double[] output, int[] k, int[] cat, double epsilon){
		int nonDigitalNumber = 0;
		int[] kNumber = new int[k.length];

		for(int i = 0; i < output.length; i++){
			if ((output[i] <= 1 - epsilon) && (output[i] >= 0 + epsilon)){
				nonDigitalNumber++;
			}
			if(output[i] >= 1 - epsilon){
				kNumber[cat[i]]++;
			}
		}
		
		if ((nonDigitalNumber == 0) && (kNumber == k))
			outputResponse = "Outputs Test: Pass";
		else{
			outputResponse = "Outputs Test: Fail";
			System.out.println("Number of failed digital states: " + nonDigitalNumber);
			for (int i = 0; i < k.length; i++)
				System.out.printf("Number of active neurons: %d\n" +
						"Number of desired active neurons: %d\n", kNumber[i], k[i]);
		}
		return outputResponse;
	}

}
