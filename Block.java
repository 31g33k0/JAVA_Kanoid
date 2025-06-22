package arkanoid2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Block extends Rectangle{
	public int x,y;
	public static int size_X = 60;
	public static int size_Y = 30;
	public Color c = Color.BLUE;

	public Block(int x,int y,Color c ) {
		this.c = c;
		this.x = x;
		this.y = y;
//		return Block(x, y, c);
	}
	public void Draw(Graphics2D g2) {
		int margin = 2;
		g2.setColor(c);
		g2.fillRect(x + margin, y + margin, size_X - 2*margin, size_Y - 2*margin);
	}
}
