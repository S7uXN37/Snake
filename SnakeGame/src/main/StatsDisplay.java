package main;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class StatsDisplay extends JFrame {
	private static final long serialVersionUID = -5906940729373074072L;
	private static final String SCORE_PREFIX 	= "Score:      ";
	private static final String TIME_PREFIX 	= "Time:       ";
	private static final Dimension FIELD_DIMENSIONS = new Dimension(175, 30);
	private static final Font MY_FONT = new Font("Times New Roman", Font.PLAIN, 25);
	
	public int score = 0;
	
	private long lastReset;
	private JLabel scoreDisplay;
	private JLabel timeDisplay;
	private boolean updateStopped = false;
	private Thread updateThread;
	private boolean killThread = false;
	private float time = 0;
	
	public StatsDisplay() {
		super();
		
		scoreDisplay = new JLabel(SCORE_PREFIX + score);
		scoreDisplay.setPreferredSize(FIELD_DIMENSIONS);
		scoreDisplay.setFont(MY_FONT);
		
		timeDisplay = new JLabel(TIME_PREFIX + lastReset);
		timeDisplay.setPreferredSize(FIELD_DIMENSIONS);
		timeDisplay.setFont(MY_FONT);
		
		this.getContentPane().add(scoreDisplay, BorderLayout.BEFORE_FIRST_LINE);
		this.getContentPane().add(timeDisplay, BorderLayout.AFTER_LAST_LINE);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) screenSize.getWidth();
		int height = (int) screenSize.getHeight();
		this.setLocation((width+Game.WIDTH)/2+10, (height-Game.HEIGHT)/2-5);
		
		updateThread = new Thread(new Runnable(){
			public void run() {
				try {
					do {
						if(!updateStopped){
							time = System.currentTimeMillis()-lastReset;
							redraw();
						}
						Thread.sleep(100);
					} while(!killThread);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}, "UpdateThread");
		updateThread.start();
		
		this.pack();
		this.setVisible(true);
	}

	public void reset(final Game game) {
		lastReset = System.currentTimeMillis();
		updateStopped = false;
		
		this.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				killThread = true;
				StatsDisplay sd = new StatsDisplay();
				sd.reset(game);
				sd.score = score;
				sd.time = time;
				sd.lastReset = lastReset;
				sd.updateStopped = updateStopped;
				sd.redraw();
				game.scoreWindow = sd;
			}
		});
	}

	protected void redraw() {
		scoreDisplay.setText(SCORE_PREFIX+score);
		timeDisplay.setText(TIME_PREFIX+Util.millisToTime(time));
	}

	public void stopTimer() {
		updateStopped  = true;
	}
}
