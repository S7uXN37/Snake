package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.util.Log;

public class Highscores {
	
	public static final int HIGHSCORE_X_OFFSET = 100;
	public static final int HIGHSCORE_Y_INCREMENT = 15;
	public static int HIGHSCORE_Y_OFFSET = 50;
	public static TrueTypeFont HIGHSCORE_FONT;
	public static int MAX_HIGHSCORE_SIZE = 10;
	
	private static File highscoreFile = new File("highscores.dat");
	private static String highscores;
	
	public static void add(String name, int score) {
		Log.info("Adding score: name=" + name + " score=" + score);
		load();
		
		String[] scores = highscores.split("\\|");
		
		String entry = name.replaceAll(" ", "") + "#" + score;
		if(highscores == "") {
			highscores = entry;
		} else if(Util.indexInArray(scores, entry) == -1) {
			for(int i=0; i<scores.length+1; i++) { // i-1: previous ; i: next
				if( i==0 ) { // first pass
					if(getScore(scores[i])<=score) {
						highscores = entry + "|" + highscores;
						break;
					}
				} else if( i==scores.length ) { // last pass
					if(getScore(scores[i-1])>=score) {
						highscores +=  "|" + entry;
						break;
					}
				} else if( getScore(scores[i-1])>=score && getScore(scores[i])<=score ) { // intermediate passes
					highscores = Util.joinInclusive(scores, 0, i-1, "|") + "|" + entry + "|" + Util.joinInclusive(scores, i, scores.length-1, "|");
					break;
				}
			}
		} else {
			Log.info("entry not added, already found; entry: " + entry);
		}
		
		Log.info("updated highscores: " + highscores);
	}
	
	public static void save() {
		Log.info("Writing scores: " + highscores);
		load();
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
	
	public static void load() {
		Log.info("Loading highscores");
		if(highscores == null) {
			try {
				if(highscoreFile.exists()) {
					FileInputStream fis = new FileInputStream(highscoreFile);
					ObjectInputStream ois = new ObjectInputStream(fis);
					
					Object read;
					try {
						read = ois.readObject();
					} catch (IOException e) {
						ois.close();
						fis.close();
						highscoreFile.delete();
						load();
						return;
					}
					
					ois.close();
					fis.close();
					
					if(read instanceof String) {
						highscores = (String)read;
					} else {
						throw new ClassCastException("Highscores file corrupted");
					}
				} else {
					highscores = "";
				}
			} catch(IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		shortenHighscores();
	}
	
	private static void shortenHighscores() {
		if(highscores.length()>MAX_HIGHSCORE_SIZE) {
			String[] scores = highscores.split("\\|");
			Util.joinInclusive(scores, 0, scores.length-2, "|");
		}
	}
	
	public static DrawEvent[] getDrawEvents() {
		load();
		
		String[] keys = highscores.split("\\|");
		DrawEvent[] draws = new DrawEvent[keys.length];
		
		for(int i=0; i<keys.length ; i++) {
			String key = keys[i];
			String text = key.replaceAll("#", "\t");
			DrawEvent render = new TextDrawEvent(Game.TEXT_COLOR, -1L, 0L, text, HIGHSCORE_FONT,
					HIGHSCORE_X_OFFSET,
					HIGHSCORE_Y_OFFSET + i*HIGHSCORE_Y_INCREMENT
				);
			draws[i] = render;
		}
		
		return draws;
	}
	
	private static int getScore(String entry) {
		return Integer.parseInt(entry.split("#")[1]);
	}
}
