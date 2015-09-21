package net.crocodile_productions.main;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.util.Log;

public class Game extends BasicGame
{
	public static String name = "TheNamelessHero";
	public static int GRID_SIZE_X = 30;
	public static int GRID_SIZE_Y = 30;
	public static int PX_PER_GRID = 20;
	public static int BORDER_SIZE = 10;
	public static final Color ENV_COLOR = Color.white;
	public static final Color GRID_COLOR = new Color(1F, 1F, 1F, 0.5F);
	public static final Color SNAKE_COLOR = Color.blue;
	public static final Color BCKG_COLOR = Color.black;
	public static final Color COLLISION_COLOR = Color.red;
	public static final Color PICKUP_SIZEUP_COLOR = Color.green;
	public static final Color PICKUP_SCOREUP_COLOR = Color.orange;
	public static final Color GAMEOVER_COLOR = Color.black;
	
	public static boolean GROW_MODE = false;
	public static boolean SPAWN_PICKUP_ONLY_ON_CONSUME = false;
	
	private static float GROW_RATE = 0.5F;
	private static float PICKUP_RATE = 0.2F;
	
	public GameContainer gameContainer;
	public StatsDisplay scoreWindow;
	
	private Snake snake;
	private ArrayList<DrawEvent> drawQueue = new ArrayList<DrawEvent>();
	private ArrayList<Pickup> pickups = new ArrayList<Pickup>();
	private boolean closeRequested = false;
	protected static int WIDTH;
	protected static int HEIGHT;
	
	public Game(String gamename)
	{
		super(gamename);
		scoreWindow = new StatsDisplay();
	}

	@Override
	public void init(GameContainer gc) throws SlickException {
		gameContainer = gc;
		gameContainer.setAlwaysRender(true);
		reset();
	}
	
	public void reset() {
		int startX = (GRID_SIZE_X-1)/2;
		int startY = (GRID_SIZE_Y-1)/2;
		int startPos = Util.coordsToField(startX, startY);
		
		snake = new Snake(startPos, this);
		
		drawQueue.clear();
		pickups.clear();
		closeRequested = false;
		
		scoreWindow.reset(this);
		
		gameContainer.getInput().removeAllKeyListeners();
		gameContainer.getInput().addKeyListener(new InputInterface(snake, this));
	}

	long lastUpdateTime = System.currentTimeMillis();
	long lastGrowTime = System.currentTimeMillis();
	long lastPickupTime = System.currentTimeMillis();
	
	@Override
	public void update(GameContainer gc, int i) throws SlickException {
		scoreWindow.score = snake.score;
		scoreWindow.redraw();
		
		long moveDelta = System.currentTimeMillis() - lastUpdateTime;
		long pickupDelta = System.currentTimeMillis() - lastPickupTime;
		long growDelta = System.currentTimeMillis() - lastGrowTime;
		
		if( moveDelta > 1000 / snake.getSpeed()){
			boolean grow = false;
			if(GROW_MODE) {
				grow = growDelta > 1000/GROW_RATE;
			} else if (snake.growQueued){
				grow = true;
				snake.growQueued = false;
			}
			// Snake
			if(snake.isAlive) {
				snake.move(grow);
				if(grow) {
					lastGrowTime = System.currentTimeMillis();
				}
			}
			lastUpdateTime=System.currentTimeMillis();
		}
		if(SPAWN_PICKUP_ONLY_ON_CONSUME) {
			if(pickups.size()==0) {
				if(GROW_MODE){
					pickups.add(Pickup.newPickup(this, new int[]{Pickup.SCORE_UP}));
				} else {
					pickups.add(Pickup.newPickup(this, Pickup.TYPES));
				}
			}
		} else {
			if( pickupDelta > 1000 / PICKUP_RATE ){
				if(GROW_MODE) {
					pickups.add(Pickup.newPickup(this, new int[]{Pickup.SCORE_UP}));
				} else {
					pickups.add(Pickup.newPickup(this, Pickup.TYPES));
				}
				lastPickupTime = System.currentTimeMillis();
			}
		}
		
		if(closeRequested) {
			// Save game data here TODO
			Log.info("Close has been requested, exiting...");
			gc.exit();
		}
	}

	@Override
	public void render(GameContainer gc, Graphics g) throws SlickException
	{
		// draw borders
		g.setColor(ENV_COLOR);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(BCKG_COLOR);
		g.fillRect(BORDER_SIZE,
			BORDER_SIZE,
			WIDTH-2*BORDER_SIZE,
			HEIGHT-2*BORDER_SIZE
		);
		
		// draw grid
		g.setColor(GRID_COLOR);
		for(int i=1 ; i<GRID_SIZE_X ; i++) { // vertical lines
			g.drawLine(i*PX_PER_GRID + BORDER_SIZE,
				BORDER_SIZE,
				i*PX_PER_GRID + BORDER_SIZE,
				HEIGHT - BORDER_SIZE
			);
		}
		for(int i=1 ; i<GRID_SIZE_Y ; i++) { // horizontal lines
			g.drawLine(BORDER_SIZE,
				i*PX_PER_GRID + BORDER_SIZE,
				WIDTH - BORDER_SIZE,
				i*PX_PER_GRID + BORDER_SIZE
			);
		}
		
		// draw pickups
		for(Pickup p : pickups) {
			p.draw.draw(gc, g);
		}
		
		// draw snake
		g.setColor(SNAKE_COLOR);
		for(int field : snake.occupiedFields) {
			fillField(gc, g, field);
		}
		
		// draw queue
		for(int i=0 ; i<drawQueue.size() ; i++) {
			DrawEvent d = drawQueue.get(i);
			if(d.hasExpired()) {
				drawQueue.remove(i);
			} else if(d.shouldDraw()) {
				d.draw(gc, g);
			}
		}
	}

	public static void fillField(GameContainer gc, Graphics g, int field) {
		int[] c = Util.fieldToCoords(field);
		g.fillRect(
			PX_PER_GRID * c[0] + BORDER_SIZE,
			PX_PER_GRID * c[1] + BORDER_SIZE,
			PX_PER_GRID,
			PX_PER_GRID
		);
	}

	public static void newGame(String[] settings)
	{
		for(int i=0 ; i<settings.length ; i++) {
			String s = settings[i];
			switch(i) {
				case 0://name
					name = s;
					break;
				case 1://gameSize
					String[] dims = s.split("x");
					GRID_SIZE_X = Integer.parseInt(dims[0]);
					GRID_SIZE_Y = Integer.parseInt(dims[1]);
					break;
				case 2://pxPerField
					PX_PER_GRID = Integer.parseInt(s);
					break;
				case 3://borderSize
					BORDER_SIZE = Integer.parseInt(s);
					break;
				case 4://growMode
					GROW_MODE = Integer.parseInt(s)==1;
					break;
				case 5://spawnMode
					SPAWN_PICKUP_ONLY_ON_CONSUME = Integer.parseInt(s)==1;
					break;
				case 6://growRate
					GROW_RATE = Float.parseFloat(s);
					break;
				case 7://spawnRate
					PICKUP_RATE = Float.parseFloat(s);
					break;
			}
		}
		
		try
		{
			WIDTH = PX_PER_GRID * GRID_SIZE_X + BORDER_SIZE*2;
			HEIGHT = PX_PER_GRID * GRID_SIZE_Y + BORDER_SIZE*2;
			AppGameContainer appgc;
			appgc = new AppGameContainer(new Game("Snake Game"));
			appgc.setDisplayMode(WIDTH, HEIGHT, false);
			appgc.setShowFPS(false);
			appgc.start();
		}
		catch (SlickException ex)
		{
			Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean isOccupied(int i) {
		boolean[] fieldsOccupied = new boolean[GRID_SIZE_X*GRID_SIZE_Y];
		for(int field : snake.occupiedFields) {
			fieldsOccupied[field] = true;
		}
		return fieldsOccupied[i];
	}

	public void addDrawEvent(DrawEvent drawEvent) {
		drawQueue.add(drawEvent);
	}

	public void close() {
		closeRequested = true;
	}

	public boolean tryPickup(int field) {
		for(Pickup p : pickups) {
			if(p.field==field) {
				pickups.remove(p);
				p.pickup(snake);
				return true;
			}
		}
		return false;
	}
}