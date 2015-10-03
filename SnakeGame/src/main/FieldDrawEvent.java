package main;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

public class FieldDrawEvent extends DrawEvent {
	public int field;
	
	public FieldDrawEvent(int field, Color color, float lifeTime, float delay) {
		super(color, (long) (1000*lifeTime), (long) (1000*delay));
		this.field = field;
	}

	@Override
	public void draw(GameContainer gc, Graphics g) {
		g.setColor(color);
		Game.fillField(gc, g, field);
	}

}
