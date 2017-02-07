package tester;

public class Test {
	public String s;
		
	public Test(int[][] t){
		int rows = t.length;
		int col = t[0].length;
		s =TMatrixTest(rows, col, t);
	}
	
	public String TMatrixTest(int rows, int col, int[][] t){
		boolean rowsAndColumns = false;
		int incorrectValues = 0;
		boolean valuesPass = false;
		
		
		
		if (rows == col)
			rowsAndColumns = true;
		
		for (int i = 0; i < rows; i++){
			for (int j = 0; j < col; j++){
				if (i == j){
					int current = t[i][j];
					if (current != 0)
						incorrectValues++;
				}
				else{
					int current = t[i][j];
					int remainder = current % 2;
					if ((remainder != 0) || (current >= 0))
						incorrectValues++;
				}
			}
		}
		if (incorrectValues == 0)
			valuesPass = true;
		if ((valuesPass == true) &&(rowsAndColumns == true)){
			String r = "T Matrix Pass";
			return r;
		}
		else{
			System.out.println(rowsAndColumns + ", " + valuesPass + ", " + incorrectValues);
			String r = "T Matrix Fail";
			return r;
		}
	}
	
	public String getResults(){
		return s;
	}
	
}
