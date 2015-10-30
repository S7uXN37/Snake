package main;

import java.awt.Font;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.Log;

public class Game extends BasicGame
{
	public static String name = "TheNamelessHero";
	public static int GRID_SIZE_X = 30;
	public static int GRID_SIZE_Y = 30;
	public static int PX_PER_GRID = 20;
	public static int BORDER_SIZE = 10;
	public static int HIGHSCORE_Y_OFFSET = 50;
	public static TrueTypeFont HIGHSCORE_FONT;
	public static final int HIGHSCORE_X_OFFSET = 100;
	public static final int HIGHSCORE_Y_INCREMENT = 15;
	public static final Color ENV_COLOR = Color.white;
	public static final Color GRID_COLOR = new Color(1F, 1F, 1F, 0.5F);
	public static final Color SNAKE_COLOR = Color.blue;
	public static final Color BCKG_COLOR = Color.black;
	public static final Color COLLISION_COLOR = Color.red;
	public static final Color PICKUP_SIZEUP_COLOR = Color.green;
	public static final Color PICKUP_SCOREUP_COLOR = Color.orange;
	public static final Color GAMEOVER_COLOR = Color.black;
	public static final Color TEXT_COLOR = Color.white;
	
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
		Font awtFont = new Font("Times New Roman", Font.PLAIN, 20);
		HIGHSCORE_FONT = new TrueTypeFont(awtFont, true);
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
		scoreWindow.update();
		
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
			saveHighscores();
			Log.info("Close has been requested, exiting...");
			gc.exit();
		}
	}

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
					HIGHSCORE_Y_OFFSET += BORDER_SIZE;
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
	
	int MAX_HIGHSCORE_SIZE = 10;
	File highscoreFile = new File("highscores.dat");
	TreeMap<String, Integer> highscores;
	public void addHighscore(String key, int value) {
		loadHighscores();
		try {
			highscores.put(key + "#" + value, new Integer(value));
		} catch (IllegalArgumentException e) {
			// same player - same score
		}
	}
	
	public void saveHighscores() {
		loadHighscores();
		try {
			FileOutputStream fos = new FileOutputStream(highscoreFile, false);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeObject(highscores);
			oos.flush();
			
			oos.close();
			fos.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadHighscores() {
		if(highscores == null) {
			try {
				if(highscoreFile.exists()) {
					FileInputStream fis = new FileInputStream(highscoreFile);
					ObjectInputStream ois = new ObjectInputStream(fis);
					
					Object read;
					try {
						read = ois.readObject();
					} catch (EOFException e) {
						ois.close();
						fis.close();
						highscoreFile.delete();
						loadHighscores();
						return;
					}
					
					ois.close();
					fis.close();
					
					if(read instanceof TreeMap) {
						highscores = (TreeMap<String, Integer>)read;
					} else {
						throw new ClassCastException("Highscores file invalid");
					}
				} else {
					highscores = new TreeMap<String, Integer>(new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							int i1 = Integer.parseInt(o1.substring(o1.indexOf('#')+1));
							int i2 = Integer.parseInt(o2.substring(o2.indexOf('#')+1));
							
							if(i1<i2) {
								return -1;
							} else if(i1>i2) {
								return 1;
							} else {
								return 0;
							}
						}
					});
				}
			} catch(IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		shortenHighscores();
	}
	
	private void shortenHighscores() {
		if(highscores.size()>MAX_HIGHSCORE_SIZE) {
			highscores.remove(highscores.descendingMap().lastKey());
		}
	}
	
	public void showHighscores() {
		loadHighscores();
		String[] keys = (String[]) highscores.descendingKeySet().toArray();
		for(int i=0; i<keys.length ; i++) {
			String key = keys[i];
			String text = key.substring(0, key.indexOf('#'));
			DrawEvent render = new TextDrawEvent(TEXT_COLOR, -1L, 0L, text, HIGHSCORE_FONT,
					HIGHSCORE_X_OFFSET,
					HIGHSCORE_Y_OFFSET + i*HIGHSCORE_Y_INCREMENT
				);
			drawQueue.add(render);
		}
	}
}