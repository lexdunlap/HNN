import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class Grids extends JPanel {
	int width, height, rows, columns;
	double[][] out2D;

    Grids(int w, int h, int r, int c, double[][] outputs) {
      setSize(width = w, height = h);
      out2D = outputs;
      rows = r;
      columns = c;
      
    }
    public void paintComponent(Graphics g) {
    	for (int i = 0; i < 4; i++){
      		for (int j = 0; j < 4; j++){
      			if (out2D[i][j] > 0){
      				if (i == 0 && j == 0)
      					draw(g, 5, 5, 140, 140, true);
      				if (i == 0 && j == 1)
      					draw(g, 150, 5, 140, 140, true);
      				if (i == 0 && j == 2)
      					draw(g, 300, 5, 140, 140, true);
      				if (i == 0 && j == 3)
      					draw(g, 450, 5, 140, 140, true);
      				if (i == 1 && j == 0)
      					draw(g, 5, 150, 140, 140, true);
      				if (i == 1 && j == 1)
      					draw(g, 150, 150, 140, 140, true);
      				if (i == 1 && j == 2)
      					draw(g, 300, 150, 140, 140, true);
      				if (i == 1 && j == 3)
      					draw(g, 450, 150, 140, 140, true);
      				if (i == 2 && j == 0)
      					draw(g, 5, 300, 140, 140, true);
      				if (i == 2 && j == 1)
      					draw(g, 150, 300, 140, 140, true);
      				if (i == 2 && j == 2)
      					draw(g, 300, 300, 140, 140, true);
      				if (i == 2 && j == 3)
      					draw(g, 450, 300, 140, 140, true);
      				if (i == 3 && j == 0)
      					draw(g, 5, 450, 140, 140, true);
      				if (i == 3 && j == 1)
      					draw(g, 150, 450, 140, 140, true);
      				if (i == 3 && j == 2)
      					draw(g, 300, 450, 140, 140, true);
      				
      				if (i == 3 && j == 3)
      					draw(g, 450, 450, 140, 140, true);
      			}
      			else{
      				if (i == 0 && j == 0)
      					draw(g, 5, 5, 140, 140, false);
      				if (i == 0 && j == 1)
      					draw(g, 150, 5, 140, 140, false);
      				if (i == 0 && j == 2)
      					draw(g, 300, 5, 140, 140, false);
      				if (i == 0 && j == 3)
      					draw(g, 450, 5, 140, 140, false);
      				if (i == 1 && j == 0)
      					draw(g, 5, 150, 140, 140, false);
      				if (i == 1 && j == 1)
      					draw(g, 150, 150, 140, 140, false);
      				if (i == 1 && j == 2)
      					draw(g, 300, 150, 140, 140, false);
      				if (i == 1 && j == 3)
      					draw(g, 450, 150, 140, 140, false);
      				if (i == 2 && j == 0)
      					draw(g, 5, 300, 140, 140, false);
      				if (i == 2 && j == 1)
      					draw(g, 150, 300, 140, 140, false);
      				if (i == 2 && j == 2)
      					draw(g, 300, 300, 140, 140, false);
      				if (i == 2 && j == 3)
      					draw(g, 450, 300, 140, 140, false);
      				if (i == 3 && j == 0)
      					draw(g, 5, 450, 140, 140, false);
      				if (i == 3 && j == 1)
      					draw(g, 150, 450, 140, 140, false);
      				if (i == 3 && j == 2)
      					draw(g, 300, 450, 140, 140, false);
      				
      				if (i == 3 && j == 3)
      					draw(g, 450, 450, 140, 140, false);
      			}
      		}
      	}
    	
    	
    }
    
    public void draw(Graphics g, int x, int y, int i, int j, boolean isBlack){
    	if (isBlack){
    		g.setColor(Color.black);
        	g.fillRect(x, y, i, j);
    	}
    	else{
    		g.setColor(Color.white);
    		g.fillRect(x, y, i, j);
    	}
    	
    }

}
