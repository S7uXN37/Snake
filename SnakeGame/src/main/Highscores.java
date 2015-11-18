package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Highscores {
	
	public static final int HIGHSCORE_Y_INCREMENT = 20;
	private static final int HIGHSCORE_DISPLAY_LENGTH = 40;
	public static final int MAX_NAME_LENGTH = 25;
	public static int HIGHSCORE_X_OFFSET = 50;
	public static int HIGHSCORE_Y_OFFSET = 50;
	public static int MAX_HIGHSCORE_COUNT = 15;
	
	private static File highscoreFile = new File("highscores.dat");
	private static String highscores;
	
	public static void add(String name, int score) {
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
		}
		
		shorten();
		save();
	}
	
	public static void save() {
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
		
		shorten();
	}
	
	private static void shorten() {
		String[] scores = highscores.split("\\|");
		if(scores.length > MAX_HIGHSCORE_COUNT) {
			String[] newScores = new String[MAX_HIGHSCORE_COUNT];
			for(int i=0; i<newScores.length; i++) {
				newScores[i] = scores[i];
			}
			highscores = Util.joinInclusive(newScores, 0, newScores.length-1, "|");
		}
	}
	
	public static TextDrawEvent[] getDrawEvents() {
		load();
		
		String[] keys = highscores.split("\\|");
		TextDrawEvent[] draws = new TextDrawEvent[keys.length];
		
		for(int i=0; i<keys.length ; i++) {
			String key = keys[i];
			
			String text = key.split("#")[0];
			String score = key.split("#")[1];
			
			while(text.length() + score.length() < HIGHSCORE_DISPLAY_LENGTH ) {
				text += " ";
			}
			
			text += score;
			
			TextDrawEvent render = new TextDrawEvent(Game.TEXT_COLOR, -1L, 0L, text, Game.HIGHSCORE_FONT,
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
