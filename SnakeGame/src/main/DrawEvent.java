package main;

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
	 * @param color color to draw with
	 * @param lifeTime time to draw for (-1 = forever)
	 * @param delay time to draw after
	 */
	public DrawEvent(Color color, float lifeTime, float delay) {
		this.color = color;
		this.lifeTime = lifeTime;
		this.delay = delay;
		birth = System.currentTimeMillis();
		startMillis = birth+(long)delay;
		deathMillis = startMillis+(long)lifeTime;
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
