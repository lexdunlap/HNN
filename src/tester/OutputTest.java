/**
 * 
 */
package tester;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author dunla
 * @version
 * @since SDK1.8
 */
public class OutputTest {
	private String outputResponse;
	
	public OutputTest(double[] v, ArrayList<Integer> kNum, ArrayList<ArrayList> cat){
		outputResponse = testOutput(v, kNum, cat, 0.01);
	}
	
	
	public String getOutputResults(){
		return outputResponse;
	}
	
	private String testOutput(double[] output, ArrayList<Integer> k, ArrayList<ArrayList> cat, double epsilon){
        int[] kVal = new int[k.size()];
        for (int i = 0; i < k.size(); i++)
        {
            kVal[i] = k.get(i);
        }
        int nonDigitalNumber = 0;
		int[] kNumber = new int[k.size()];

		for(int i = 0; i < output.length; i++){
			if ((output[i] <= 1 - epsilon) && (output[i] >= 0 + epsilon)){
				nonDigitalNumber++;
			}
			if(output[i] >= 1 - epsilon){
                for (int j = 0; j < cat.get(i).size(); j++)
                    kNumber[(int) cat.get(i).get(j)]++;
			}
		}
		
		if ((nonDigitalNumber == 0) && (Arrays.equals(kNumber, kVal)))
			outputResponse = "Outputs Test: Pass";
		else{
			outputResponse = "Outputs Test: Fail";
			System.out.println("Number of failed digital states: " + nonDigitalNumber);
			for (int i = 0; i < k.size(); i++)
				System.out.printf("Number of active neurons: %d\n" +
						"Number of desired active neurons: %d\n", kNumber[i], kVal[i]);
		}
		return outputResponse;
	}

}
