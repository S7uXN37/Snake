package main;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

public class TextDrawEvent extends DrawEvent {
	String text;
	Font font;
	int x,y;
	
	public TextDrawEvent(Color color, long lifeTime, long delay, String text, Font font, int x, int y) {
		super(color, lifeTime, delay);
		this.text = text;
		this.font = font;
		this.x = x;
		this.y = y;
	}

	@Override
	public void draw(GameContainer gc, Graphics g) {
		g.setFont(font);
		g.setColor(color);
		g.drawString(text, x, y);
	}

}
