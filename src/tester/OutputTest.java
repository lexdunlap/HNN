/**
 * 
 */
package tester;

/**
 * @author dunla
 *
 */
public class OutputTest {
	public String outputResponse;
	
	public OutputTest(double[] v, int kNum){
		outputResponse = testOutput(v, kNum); 
	}
	
	
	public String getOutputResults(){
		return outputResponse;
	}
	
	public String testOutput(double[] output, int k){
		int nonDigitalNumber = 0;
		int kNumber = 0;
		int kActual = k;
		
		for(int i = 0; i < output.length; i++){
			if ((output[i] != 1) && (output[i] != 0)){
				nonDigitalNumber++;
			}
			if(output[i] == 1){
				kNumber++;
			}
		}
		
		if ((nonDigitalNumber == 0) && (kNumber == kActual))
			outputResponse = "Outputs Test: Pass";
		else{
			outputResponse = "Outputs Test: Fail";
			System.out.println("Number of failed digital states: " + nonDigitalNumber);
			System.out.println("Number of active neurons: " + kNumber + "\nNumber of desired active neurons: " + kActual);
		}
		return outputResponse;
	}

}
