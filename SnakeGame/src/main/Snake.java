package main;


public class Snake {
	public static final int UP = 0;
	public static final int RIGHT = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;
	
	private static final float speedMultiplier = 0.7F;
	private static final float speedOffset = 0.8F;
	
	public boolean isAlive = true;
	public int[] occupiedFields;
	protected Game game;
	private int direction = RIGHT;
	public boolean growQueued = false;
	public int score = 0;
	
	public Snake(int startField, Game parentInstance) {
		occupiedFields = new int[]{startField};
		game = parentInstance;
	}
	
	public void setDirection(int newDir) {
		direction = newDir;
	}
	
	/**
	 * @return Speed of the snake in moves/second
	 */
	public float getSpeed() {
		return occupiedFields.length*speedMultiplier + speedOffset;
	}
	
	public void move(boolean grow) {
		int[] workingCopy = occupiedFields.clone();
		if(grow) {
			score++;
			workingCopy = addTail(workingCopy);
		}
		
		// cycle array
		for(int i=1 ; i<workingCopy.length ; i++) {
			workingCopy[i-1] = workingCopy[i];
		}
		// calculate grid-field-id offset
		int offset = 0;
		switch(direction) {
			case LEFT:
				offset = -1;
				break;
			case RIGHT:
				offset = 1;
				break;
			case UP:
				offset = -Game.GRID_SIZE_X;
				break;
			case DOWN:
				offset = Game.GRID_SIZE_X;
				break;
		}
		// set new head to old-head-position plus offset
		int oldHead = workingCopy[workingCopy.length-1];
		workingCopy[workingCopy.length-1] += offset;
		int newHead = workingCopy[workingCopy.length-1];
		
		// if the snake reaches a wall, kill it
		if(
			newHead<0 ||
			newHead>(Game.GRID_SIZE_X*Game.GRID_SIZE_Y)-1 ||
			(newHead%Game.GRID_SIZE_X==0 && direction==RIGHT) ||
			(newHead%Game.GRID_SIZE_X==Game.GRID_SIZE_X-1 && direction==LEFT)
			
			) {
			kill(
					new FieldDrawEvent(
						oldHead,
						Game.COLLISION_COLOR,
						-1,
						0
					)
				);
			workingCopy[workingCopy.length-1] = oldHead; // remove invalid newHead
			newHead = oldHead;
		}
		// if the head-field is already occupied, kill the snake
		if(game.isOccupied(newHead) && this.isAlive) {
			kill(
					new FieldDrawEvent(
						newHead,
						Game.COLLISION_COLOR,
						-1,
						0
					)
				);
		}
		
		// check for pickup
		if(this.isAlive) game.tryPickup(newHead);
		
		// update occupied fields
		occupiedFields = workingCopy;
	}

	private int[] addTail(int[] body) {
		int[] newBody = new int[body.length+1];
		newBody[0] = body[0];
		for(int i=0 ; i<body.length ; i++) {
			newBody[i+1] = body[i];
		}
		return newBody;
	}

	public void kill(DrawEvent drawEvent) {
		this.isAlive = false;
		game.addDrawEvent(drawEvent);
		game.scoreWindow.stopTimer();
		
		float del = (float) (5F/Math.pow((float)Game.GRID_SIZE_Y, 2));
		float delPassed = 0F;
		for(int n=0 ; n<Game.GRID_SIZE_Y ; n++) { // cycles
			for(int y=0 ; y<Game.GRID_SIZE_Y-n ; y++) { // for all lines but each cycle one line less
				for(int x=0 ; x<Game.GRID_SIZE_X ; x++) { // whole line
					FieldDrawEvent d = new FieldDrawEvent(
						Util.coordsToField(x, y),
						Game.GAMEOVER_COLOR,
						del,
						delPassed + y*del
					);
					game.addDrawEvent(d);
				}
			}
			delPassed += (Game.GRID_SIZE_Y-n)*del;
			for(int y=1 ; y<n+2 ; y++) { // for no lines but each cycle one more less
				for(int x=0 ; x<Game.GRID_SIZE_X ; x++) { // whole line
					FieldDrawEvent d = new FieldDrawEvent(
						Util.coordsToField(x, Game.GRID_SIZE_Y-y),
						Game.GAMEOVER_COLOR,
						-1,
						delPassed
					);
					game.addDrawEvent(d);
				}
			}
		}
	}
}
