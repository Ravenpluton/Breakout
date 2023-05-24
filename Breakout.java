/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.text.AttributeSet.ColorAttribute;

public class Breakout extends GraphicsProgram {

	/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

	/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

	/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

	/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

	/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

	/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

	/** Separation between bricks */
	private static final int BRICK_SEP = 4;

	/** Width of a brick */
	private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

	/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

	/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

	/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

	/** Number of turns */
	private static final int NTURNS = 3;

	// sleep time to see ball movement
	private static final double SLEEP_TIME = 6.5;

	private RandomGenerator rgen = RandomGenerator.getInstance();
	private GRect paddle;
	private GOval ball;
	private double vx, vy;
	private int brickCount;

	/* Method: run() */
	/**
	 * Runs the Breakout program. it starts with initialization and then moves into
	 * for cycle and repeats it until you either win by destroying bricks or use up
	 * all of your turns and lose game.
	 * 
	 * 
	 */
	public void run() {
		this.setSize(WIDTH, HEIGHT); // sets window size
		addMouseListeners(); // add mouse listeners for program to respond to mouse events

		/*
		 * game initialization happens once because every time you lose the ball and go
		 * on next turn you need to start from the same amount of bricks you lost ball
		 * at
		 * 
		 */
		gameInitialisation();
		/*
		 * first thing happening inside of a for cycle is creation of ball since it
		 * needs to happen every time player starts a new turn. game process only starts
		 * after click.
		 * 
		 */
		for (int i = 0; i < NTURNS; i++) {
			createBall();
			waitForClick();
			gameProcess();

			/*
			 * if brick count = 0, player wins, ball disappears, you win label appears on
			 * screen, and for cycle breaks
			 * 
			 */
			if (brickCount == 0) {
				remove(ball);
				String win = ("You Win!");
				putUpLabel(win, Color.orange);
				break;
			} else
				remove(ball);

		}

		/*
		 * if player uses up all turns and brick count is still more than 0, it means
		 * player lost the game and game over label appears.
		 * 
		 */
		if (brickCount > 0) {
			String gameOver = ("Game Over!");
			putUpLabel(gameOver, Color.red);
		}
	}

	/*
	 * everything that needs to happen before game starts is in the
	 * gameInitialisation. this sets up brick count, bricks and paddle.
	 * 
	 */

	private void gameInitialisation() {
		brickCount = NBRICKS_PER_ROW * NBRICK_ROWS;
		createBricks();
		createPaddle();
	}

	/*
	 * bricks are created with two for cycles one which creates multiple rows and
	 * other which creates multiple bricks in the row. this method creates rows.
	 */
	private void createBricks() {
		int colorNum = 5;

		for (int i = 0; i < NBRICK_ROWS; i++) {
			oneRowOfBricks(i, colorNum);
		}
	}

	/*
	 * this method creates multiple bricks in row.
	 * 
	 */

	private void oneRowOfBricks(int i, int colorNum) {
		for (int j = 0; j < NBRICKS_PER_ROW; j++) {
			// finding coordinates for brick
			double y = BRICK_Y_OFFSET + (BRICK_HEIGHT + BRICK_SEP) * i;
			double rowWidth = NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP; // row width is used to
																									// center bricks
			double x = (WIDTH - rowWidth) / 2 + (BRICK_WIDTH + BRICK_SEP) * j;

			// creates one filled brick
			GRect brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
			add(brick);
			brick.setFilled(true);

			// gets brick color
			setRectColor(brick, i, colorNum);
		}
	}

	// brick colors are depended on the row and number of colors used in whole
	// program
	private void setRectColor(GRect brick, int i, int colorNum) {
		if (i % (colorNum * 2) < 2) {
			brick.setColor(Color.RED);
		} else if (i % (colorNum * 2) == 2 || i % (colorNum * 2) == 3) {
			brick.setColor(Color.ORANGE);
		} else if (i % (colorNum * 2) == 4 || i % (colorNum * 2) == 5) {
			brick.setColor(Color.YELLOW);
		} else if (i % (colorNum * 2) == 6 || i % (colorNum * 2) == 7) {
			brick.setColor(Color.GREEN);
		} else if (i % (colorNum * 2) == 8 || i % (colorNum * 2) == 9) {
			brick.setColor(Color.CYAN);
		}
	}

	// this method creates paddle
	private void createPaddle() {
		// finding coordinates of the paddle
		double x = (WIDTH - PADDLE_WIDTH) / 2;
		double y = HEIGHT - PADDLE_Y_OFFSET - PADDLE_HEIGHT;

		// create paddle itself using GRect
		paddle = new GRect(x, y, PADDLE_WIDTH, PADDLE_HEIGHT);
		paddle.setFilled(true);
		add(paddle);
	}

	/*
	 * paddle is moving with mouse event. it takes a centered position from the
	 * place mouse is standing. if ensures that paddle stays inside of window.
	 * 
	 */

	public void mouseMoved(MouseEvent e) {
		if ((e.getX() > PADDLE_WIDTH / 2) && e.getX() < getWidth() - PADDLE_WIDTH / 2) {
			paddle.setLocation(e.getX() - PADDLE_WIDTH / 2, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT);
		}
	}

	// this method creates ball in the center of the window

	private void createBall() {
		// finding coordinates
		double x = WIDTH / 2 - BALL_RADIUS;
		double y = HEIGHT / 2 - BALL_RADIUS;

		// creating black ball using GOval
		ball = new GOval(x, y, BALL_RADIUS * 2, BALL_RADIUS * 2);
		ball.setFilled(true);
		ball.setColor(Color.BLACK);
		add(ball);
	}

	// this is a part of the program where actual game begins

	private void gameProcess() {
		moveBall();
	}

	/*
	 * this method moves ball also checks if it collided with something
	 */
	private void moveBall() {
		ballVelocity(); // gets ball velocity/speed

		// ball movement is inside of the while cycle
		while (true) {
			ball.move(vx, vy);
			pause(SLEEP_TIME);

			// ensures that ball reflects from the wall
			reflectFromWall();

			// checking if fall collided with something
			checkCollisions();

			/*
			 * program breaks in two cases. 1. if ball falls through the down border of
			 * window. 2. if all the bricks are destroyed and brick count is 0.
			 * 
			 */
			if (ball.getY() >= getHeight())
				break;

			if (brickCount == 0)
				break;
		}
	}

	/*
	 * ball velocity if fixed according to assignment. vy is 3.0 and vx is randomly
	 * generated between 1.0 and 3.0 vx trajectory is also randomly generated.
	 * 
	 */
	private void ballVelocity() {
		vy = 3.0;
		vx = rgen.nextDouble(1.0, 3.0);
		if (rgen.nextBoolean(0.5))
			vx = -vx;
	}

	// ensures that ball stays inside of the window and reflects ball from walls
	private void reflectFromWall() {
		if ((ball.getX() - vx < 0 && vx < 0) || (ball.getX() + vx >= getWidth() - BALL_RADIUS * 2 && vx > 0)) {
			vx = -vx;
		}

		if ((ball.getY() - vy <= 0 && vy < 0)) {
			vy = -vy;
		}
	}

	// checks collisions
	private void checkCollisions() {
		// to find colliding object there is a different method
		GObject collider = getCollidingObject();

		/*
		 * if collider is a paddle ball is just reflected by reversing its vy if
		 * collider is anything else (brick in this case) collider is removed, brick
		 * count is reduced and ball is reflected.
		 */
		if (collider == paddle) {
			vy = -vy;
		} else if (collider != null) {
			remove(collider);
			brickCount--;
			vy = -vy;
		}
	}

	/*
	 * collision is checked for edges of the GOval and then object on the
	 * coordinates is returned. if there is nothing on the coordinates of edges then
	 * collision hasn't happened.
	 * 
	 */
	private GObject getCollidingObject() {
		if (getElementAt(ball.getX(), ball.getY()) != null) {
			return getElementAt(ball.getX(), ball.getY());
		} else if (getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY()) != null) {
			return getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY());
		} else if (getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS) != null) {
			return getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS);
		} else if (getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS) != null) {
			return getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS);
		} else {
			return null;
		}
	}

	/*
	 * method for labels, it sets font and color of the label and puts label exactly
	 * in the middle of window
	 * 
	 */
	private void putUpLabel(String string, Color color) {
		GLabel label = new GLabel(string);
		Font myFont = new Font("Courier", Font.BOLD, 40);
		label.setFont(myFont);
		label.setColor(color);
		double x = (WIDTH - label.getWidth()) / 2;
		double y = (HEIGHT + label.getAscent()) / 2;
		add(label, x, y);
	}

}
