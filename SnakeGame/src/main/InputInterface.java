package main;

import org.newdawn.slick.Color;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;

public class InputInterface implements KeyListener {
	private Snake snake;
	
	public InputInterface(Snake s) {
		snake = s;
	}
	
	public void setInput(Input input) {}
	
	public boolean isAcceptingInput() {
		return true;
	}
	
	public void inputEnded() {}
	
	public void inputStarted() {}
	
	public void keyPressed(int key, char c) {
		switch(key) {
			case Input.KEY_LEFT:
				snake.setDirection(Snake.LEFT);
				break;
			case Input.KEY_RIGHT:
				snake.setDirection(Snake.RIGHT);
				break;
			case Input.KEY_UP:
				snake.setDirection(Snake.UP);
				break;
			case Input.KEY_DOWN:
				snake.setDirection(Snake.DOWN);
				break;
			case Input.KEY_ESCAPE:
				Game.getInstance().close();
				break;
			case Input.KEY_R:
				Game.getInstance().reset();
				break;
			case Input.KEY_K:
				snake.kill(new FieldDrawEvent(0, Color.white, 0, 0));
				break;
			case Input.KEY_DELETE:
				if(!snake.isAlive) { // in highscore screen
					Highscores.reset();
				}
		}
	}
	
	public void keyReleased(int key, char c) {}
}
