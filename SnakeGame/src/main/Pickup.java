package main;


public class Pickup {
	public static final int SIZE_UP = 0;
	public static final int SCORE_UP = 1;
	public static final int[] TYPES = new int[]{SIZE_UP, SCORE_UP};
	
	public int type;
	public int field;
	public DrawEvent draw;
	
	public Pickup(int type, int field) {
		this.type = type;
		this.field = field;
		switch(type){
		case SIZE_UP:
			this.draw = new FieldDrawEvent(field, Game.PICKUP_SIZEUP_COLOR, -1, 0);
			break;
		case SCORE_UP:
			this.draw = new FieldDrawEvent(field, Game.PICKUP_SCOREUP_COLOR, 20, 0);
			break;
		}
	}

	public void pickup(Snake snake) {
		switch(type) {
			case SIZE_UP:
				snake.growQueued = true;
				break;
			case SCORE_UP:
				if(!draw.hasExpired())
				snake.score += 10;
				break;
		}
	}

	public static Pickup newPickup(Game game, int[] selection) {
		int type = selection[Util.randomIntInRange(0, selection.length-1)];
		int field;
		do {
			int x = Util.randomIntInRange(0, Game.GRID_SIZE_X-1);
			int y = Util.randomIntInRange(0, Game.GRID_SIZE_Y-1);
			field = Util.coordsToField(x, y);
		} while (game.isOccupied(field));
		return new Pickup(type, field);
	}
}
