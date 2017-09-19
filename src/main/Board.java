package main;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import game_objects.Ball;

public class Board extends JPanel implements MouseListener {

	private final int DELAY = 17;
	private final int RADIUS = 100;
	private final int MAX_SPEED = 100;
	private final int INITAL_SPEED = 10;
	private final int INITAL_SCORE = 100;

	// Score
	private int score = 100;

	// Display
	private Rectangle bounds;

	// Animation
	private Timer animation;

	// Ball
	private Ball ball;
	private int speed;
	private int angle;

	BufferedImage bg;

	public Board(Rectangle bounds) {
		// init variables
		this.bounds = bounds;
		animation = new Timer(DELAY, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				// 1. Move Ball
				moveBall(ball, speed, angle);
				// 2. Handle possible collision
				hitsBounds(ball);
				// 3. Update panel
				repaint();
			}
		});

		speed = INITAL_SPEED;
		angle = new Random().nextInt(361);
		
		BufferedImage background = null;
		try {
			background = ImageIO.read(new File("images/bg.png"));
			Image tmp = background.getScaledInstance(bounds.width, bounds.height, Image.SCALE_DEFAULT);
			bg = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
			bg.getGraphics().drawImage(tmp, 0, 0, null);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// load image
		BufferedImage img = null;
		BufferedImage buffered = null;
		try {
			img = ImageIO.read(new File("images/egg.png"));
			Image tmp = img.getScaledInstance(2 * RADIUS, 2 * RADIUS, Image.SCALE_DEFAULT);
			buffered = new BufferedImage(2 * RADIUS, 2 * RADIUS, BufferedImage.TYPE_INT_ARGB);
			buffered.getGraphics().drawImage(tmp, 0, 0, null);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		ball = new Ball(RADIUS, buffered);
		addMouseListener(this);
		
		//Add space key
		 getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char)KeyEvent.VK_SPACE), "pause");
	     getActionMap().put("pause", new AbstractAction() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                animation.stop();
	                JOptionPane.showMessageDialog(null, "Weiterspielen?", "Punktestand; " + score, JOptionPane.INFORMATION_MESSAGE);
	                animation.start();
	            }
	        });
	     //Add escape key
	     getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke((char)KeyEvent.VK_ESCAPE), "escape");
	     getActionMap().put("escape", new AbstractAction() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                System.exit(0);
	            }
	        });

		// start animation
		ball.setPosition((int) bounds.getCenterX() - ball.getRadius(), (int) bounds.getCenterY() - ball.getRadius());
		repaint();
		animation.start();
	}

	private void moveBall(Ball ball, int speed, double directionAngle) {

		Point p = ball.getPosition();
		int x = (int) Math.round(p.x + speed * Math.cos(Math.toRadians(directionAngle)));
		int y = (int) Math.round(p.y + speed * Math.sin(Math.toRadians(directionAngle)));

		ball.setPosition(x, y);
	}

	private boolean hitsBounds(Ball ball) {

		boolean hit = false;
		Rectangle hitbox = ball.getHitbox();
		if (hitbox.x <= bounds.getX() || hitbox.x + hitbox.width >= bounds.getX() + bounds.getWidth()) {
			angle = 180 - angle;
			hit = true;
		}
		if (hitbox.y <= bounds.getY() || hitbox.y + hitbox.height >= bounds.getY() + bounds.getHeight()) {
			angle = 360 - angle;
			hit = true;
		}

		return hit;
	}

	public void draw(Ball ball, Graphics g, JPanel panel) {

		Rectangle hitbox = ball.getHitbox();
		int radius = ball.getRadius();
		BufferedImage img = ball.getImage();

		// draw hitbox (only for debugging)
		// g.drawRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);

		if (img != null)
			g.drawImage(img, hitbox.x, hitbox.y, panel);
		else {
			g.fillOval(hitbox.x, hitbox.y, 2 * radius, 2 * radius);
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		Point mouseLocation = e.getLocationOnScreen();
		if (ball.getHitbox().contains(mouseLocation))
			ballClicked();
		else {
			score -= 5;
			if (score == 0)
				gameOver();
		}
	}

	// options if game is over
	public void gameOver() {
		speed = 0;
		int reply = JOptionPane.showConfirmDialog(null, "Nochmal?", "Verloren", JOptionPane.YES_NO_OPTION);

		if (reply == JOptionPane.YES_OPTION) {
			reset();
		} else {
			System.exit(0);
		}
	}

	public void ballClicked() {
		int randNr = new Random().nextInt(361);
		angle = randNr;

		if (speed < MAX_SPEED)
			speed += 1;
		score += 5;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.drawImage(bg, 0, 0, null);
		draw(ball, g, this);
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

	// method to reset all values
	public void reset() {
		speed = INITAL_SPEED;
		angle = new Random().nextInt(361);
		score = INITAL_SCORE;
	}
}
