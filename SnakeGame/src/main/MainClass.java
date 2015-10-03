package main;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class MainClass extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5311607785028040940L;
	private static final Font TITLE_FONT = new Font("Times New Roman", Font.PLAIN, 25);
	private static final Font MAIN_FONT = new Font("Times New Roman", Font.PLAIN, 20);
	private static final Dimension COMPONENT_SIZE = new Dimension(220, 100);
	
	private JPanel left = new JPanel();
	private JPanel right = new JPanel();
	private JLabel title;
	
	private JTextField name = new JTextField(10);
	private JLabel nameLabel = new JLabel("Nickname:");
	private JTextField gameSize = new JTextField("20x20", 10);
	private JLabel gameSizeLabel = new JLabel("Game size (<40x40):");
	private JTextField pxPerField = new JTextField("20", 4);
	private JLabel pxPerFieldLabel = new JLabel("Px per field:");
	private JTextField borderSize = new JTextField("10", 4);
	private JLabel borderSizeLabel = new JLabel("Border size:");
	private JCheckBox growMode = new JCheckBox();
	private JLabel growModeLabel = new JLabel("Grow automatically:");
	private JCheckBox spawnOnConsume = new JCheckBox();
	private JLabel spawnOnConsumeLabel = new JLabel("Only one pickup:");
	private JTextField growRate = new JTextField("0.5", 4);
	private JLabel growRateLabel = new JLabel("Grow rate (per move):");
	private JTextField spawnRate = new JTextField("0.2", 4);
	private JLabel spawnRateLabel = new JLabel("Spawn rate (per move):");
	private JButton playButton = new JButton("Play");
	
	private JLabel[] labels = new JLabel[]{nameLabel,gameSizeLabel,pxPerFieldLabel,borderSizeLabel,growModeLabel,spawnOnConsumeLabel,growRateLabel,spawnRateLabel};
	private JComponent[] fields = new JComponent[]{name,gameSize,pxPerField,borderSize,growMode,spawnOnConsume,growRate,spawnRate};
	
	public MainClass() {
		super("Main Menu");
		
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		
		title = new JLabel("SNAKE");
		title.setFont(TITLE_FONT);
		
		playButton.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand().equals("Play")) {
					startGame();
				}
			}
		});
		
		for(JLabel l : labels) {
			l.setFont(MAIN_FONT);
			l.setAlignmentX(LEFT_ALIGNMENT);
			l.setPreferredSize(COMPONENT_SIZE);
			left.add(l);
		}
		for(final JComponent c : fields) {
			if(c instanceof JTextField) {
				c.addFocusListener(new FocusListener() {
					public void focusLost(FocusEvent e) {}
					public void focusGained(FocusEvent e) {
						((JTextField) c).setText("");
					}
				});
			}
			c.setFont(MAIN_FONT);
			c.setAlignmentX(LEFT_ALIGNMENT);
			c.setPreferredSize(COMPONENT_SIZE);
			right.add(c);
		}
		
		this.getContentPane().add(title, BorderLayout.NORTH);
		this.getContentPane().add(left, BorderLayout.WEST);
		this.getContentPane().add(right, BorderLayout.EAST);
		this.getContentPane().add(playButton, BorderLayout.SOUTH);
		this.pack();
		this.setSize(500, 500);
		this.setVisible(true);
		
		this.setLocationRelativeTo(null);
		
		this.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {}
			public void windowClosed(WindowEvent e) {}
			public void windowDeactivated(WindowEvent e) {}
			public void windowDeiconified(WindowEvent e) {}
			public void windowIconified(WindowEvent e) {}
			public void windowOpened(WindowEvent e) {}
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
	
	protected void startGame() {
		this.setVisible(false);
		String[] settings = new String[fields.length];
		for(int i=0 ; i<fields.length ; i++) {
			JComponent f = fields[i];
			if(f instanceof JTextField) {
				settings[i] = ((JTextField)f).getText();
			} else if (f instanceof JCheckBox) {
				if(((JCheckBox)f).isSelected()) {
					settings[i] = "1";
				} else {
					settings[i] = "0";
				}
			}
		}
		Game.newGame(settings);
	}

	public static void main(String[] args) {
		new MainClass();
	}
}
