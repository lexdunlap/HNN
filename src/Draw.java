import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.JFrame;

public class Draw extends JFrame {
	Draw(String title, int w, int h, int rows, int columns, double[][] queens) {
        
        //getContentPane().add(new Grids(w, h, rows, columns, queens), BorderLayout.CENTER);
        Grids grid = new Grids(w, h, rows, columns, queens);
        add(grid, BorderLayout.CENTER);
                                   
		setSize(new Dimension(615, 650));
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(title);
		setVisible(true);

      }

}
