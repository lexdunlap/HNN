

public class ChessBoard {
	
	public ChessBoard(double[][] queens){
		new Draw("Queen Placement", 600, 600, 4, 4, queens).setVisible(true);	
	}

}
