package net.crocodile_productions.main;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

public abstract class DrawEvent {
	public Color color;
	public float lifeTime;
	protected long birth;
	protected float delay;
	protected long startMillis;
	protected long deathMillis;
	
	/**
	 * @param field field to draw on
	 * @param color color to draw with
	 * @param lifeTime time to draw for (-1 = forever)
	 */
	public DrawEvent(Color color, long lifeTime, long delay) {
		this.color = color;
		this.lifeTime = lifeTime;
		this.delay = delay;
		birth = System.currentTimeMillis();
		startMillis = birth+delay;
		deathMillis = startMillis+lifeTime;
	}
	
	public boolean hasExpired() {
		if(lifeTime < 0) {
			return false;
		} else {
			return deathMillis < System.currentTimeMillis();
		}
	}

	public abstract void draw(GameContainer gc, Graphics g);

	public boolean shouldDraw() {
		return startMillis < System.currentTimeMillis();
	}
}
