package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import game_objects.Ball;
import utils.*;

@SuppressWarnings("serial")
public class Board extends JPanel implements MouseListener {

	// Constants
	private final int DELAY = 17;
	private final int COUNTDOWN = 1000;
	private final int RADIUS = 100;
	private final int MAX_SPEED = 100;

	// Initial Values
	private final int INITAL_SPEED = 10;
	private final int INITAL_SCORE = 0;
	private final int INITIAL_COUNTDOWN = 30;

	// Score
	private int score;
	private int multiplicator;

	// Display
	private Rectangle bounds;
	private BufferedImage bg;
	private BufferedImage play;
	private Random rand;
	private Sounds sounds;
	private Highscore highscore;
	
	//Sounds
	private File hit = new File("C:\\Users\\luis_\\git\\Bounce\\sounds\\boing.wav");
	private File lost = new File("C:\\Users\\luis_\\git\\Bounce\\sounds\\Sad_Trombone.wav");
	// suitable sound for TICKTOCK not found yet

	// JLabels
	private JLabel displayScore;
	private JLabel displayHighscore;
	private JLabel remainingTime;
	private JLabel showMultiplicator;

	// Animation
	private Timer animation;
	private Timer countdown;

	// Game
	private Ball ball;
	private int speed;
	private int angle;
	private int gameTime;

	public Board(Rectangle bounds) {
		// init variables
		this.bounds = bounds;
		rand = new Random();
		initValues();

		// load images
		bg = getScaledBufferedImage("images/bg.png", bounds.width, bounds.height);
		play = getScaledBufferedImage("images/play.png", 100, 100);
		BufferedImage ballImg = getScaledBufferedImage("images/egg.png", 2 * RADIUS, 2 * RADIUS);

		// add ball
		ball = new Ball(RADIUS, ballImg);
		
		// add sound
		sounds = new Sounds();

		// JLabels for scores
		displayScore = new JLabel();
		displayScore.setBounds(100, 50, 200, 200);
		displayScore.setText("Score: " + score);
		Font font = new Font("Comic Sans MS", Font.BOLD + Font.ITALIC, 30);
		displayScore.setFont(font);
		add(displayScore);
		
		displayHighscore = new JLabel();
		displayHighscore.setBounds(100, 15, 400, 200);
		highscore = new Highscore();
		displayHighscore.setText("Highscore: " + String.valueOf(highscore.hs));
		displayHighscore.setFont(font);
		add(displayHighscore);

		// init play/pause button
		JLabel pause = new JLabel(new ImageIcon(play));
		pause.setBounds(100, 200, 100, 100);
		add(pause);
		pause.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent arg0) {
				pauseGame();
				int reply = JOptionPane.showConfirmDialog(null, "Weiterspielen?", "Punktestand; " + score,
						JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION) {
					continueGame();
				} else {
					System.exit(0);
				}
			}
		});

		remainingTime = new JLabel();
		remainingTime.setBounds(bounds.width / 2, 0, 200, 200);
		remainingTime.setText(String.valueOf(gameTime));
		remainingTime.setFont(font);
		add(remainingTime);

		// JLabel showMultiplicator()
		showMultiplicator = new JLabel();
		showMultiplicator.setBounds(100, 300, 200, 200);
		showMultiplicator.setVisible(false);
		Font multi = new Font("Comic Sans MS", Font.BOLD, 80);
		showMultiplicator.setFont(multi);
		add(showMultiplicator);

		// set Layout as absoluteLayout
		setLayout(null);

		// add timers
		animation = new Timer(DELAY, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// 1. Handle possible collision
				CollisionType collision = ball.hitsBounds(bounds);
				if (collision != null)
					switch (collision) {
					case HORIZONTAL:
						angle = 180 - angle;
						break;
					case VERTICAL:
						angle = 360 - angle;
						break;
					case BOTH:
						angle = 180 - angle;
						angle = 360 - angle;
						break;
					default:
						break;
					}
				// 2. Move Ball
				ball.move(speed, angle);
				// 3. Update panel
				repaint();
			}
		});
		countdown = new Timer(COUNTDOWN, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				gameTime--;
				remainingTime.setText(String.valueOf(gameTime));
				if (gameTime <= 5) {
					remainingTime.setForeground(Color.RED); // change Color to
															// red when just a
															// few secs are left
				} else {
					remainingTime.setForeground(Color.BLACK); // else set Color
																// to black
				}
				if (gameTime == 0)
					gameOver();
			}
		});

		// add mouselistener and keybindings
		setupKeyBindings();
		addMouseListener(this);

		// start game
		ball.setPosition((int) bounds.getCenterX() - ball.getRadius(), (int) bounds.getCenterY() - ball.getRadius());
		repaint();
		animation.start();
		countdown.start();
	}

	public void initValues() {
		speed = INITAL_SPEED;
		// angle = rand.nextInt(361);
		angle = 0;
		score = INITAL_SCORE;
		gameTime = INITIAL_COUNTDOWN;
		multiplicator = 0;
	}

	private BufferedImage getScaledBufferedImage(String filename, int width, int height) {

		BufferedImage img;
		try {
			img = ImageIO.read(new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Image tmp = img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
		BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		buffered.getGraphics().drawImage(tmp, 0, 0, null);

		return buffered;
	}

	private void setupKeyBindings() {
		// Add space key
//		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char) KeyEvent.VK_SPACE), "pause");
//		getActionMap().put("pause", new AbstractAction() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				pauseGame();
//			}
//		});
		// Add escape key
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char) KeyEvent.VK_ESCAPE), "escape");
		getActionMap().put("escape", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point mouseLocation = e.getLocationOnScreen();
		if (ball.getHitbox().contains(mouseLocation)) {
			ballClicked();
				try {
					sounds.play(hit);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
		}	
		else {
			multiplicator = 0;
			speed = INITAL_SPEED;
		}
	}

	private void pauseGame() {
		animation.stop();
		countdown.stop();
	}

	private void continueGame() {
		animation.start();
		countdown.start();
	}

	// options if game is over
	private void gameOver() {
		pauseGame();
			try {
				sounds.play(lost);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// change textdoc if score is greater than previous highscore
			if(highscore.compareScore(score)) {
				highscore.writeText(score);
			}
			
		int reply = JOptionPane.showConfirmDialog(null, "Nochmal?", "Zeit abgelaufen", JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION) {
			initValues();
			continueGame();
			ball.setPosition((int) bounds.getCenterX() - ball.getRadius(),
					(int) bounds.getCenterY() - ball.getRadius());
			displayScore.setText("Score: " + score);
			remainingTime.setText(String.valueOf(gameTime));
		} else {
			System.exit(0);
		}
	}

	public void ballClicked() {
		multiplicator++;
		angle = rand.nextInt(361);
		
		// change color of score if score is greater than highscore
		if(highscore.reachedHighscore(score)) {
			displayScore.setForeground(Color.YELLOW);
		}
		
		if (speed < MAX_SPEED)
			speed += 5;
		score += multiplicator;
		
		// check if current score is greater than previous highscore
//		if (highscore.compareScore(score)) {
//			displayHighscore.setText("Highscore: " + String.valueOf(score));
//		}
		
		displayScore.setText("Score: " + score);
		if (multiplicator > 1)
			showMultiplicator();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawImage(bg, 0, 0, null);
		ball.draw(g, this);
	}

	private void showMultiplicator() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				showMultiplicator.setText("x" + multiplicator);
				showMultiplicator.setLocation(ball.getPosition());

				// set random color
				float g = rand.nextFloat();
				float r = rand.nextFloat();
				float b = rand.nextFloat();
				Color randColor = new Color(g, b, r);
				showMultiplicator.setForeground(randColor);

				int addOn = (2 * multiplicator); // increase the remaining time
				gameTime += addOn;
				showMultiplicator.setVisible(true);
				remainingTime.setText("+ " + addOn); // show additional time for one sec

				// wait 1s for multiplicator to disappear
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				showMultiplicator.setVisible(false);
				remainingTime.setText(String.valueOf(gameTime)); // change JLabel back to the remaining time
			}
		}).start();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
