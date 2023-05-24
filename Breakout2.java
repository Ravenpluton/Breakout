
import acm.graphics.*;
import acm.program.*;
import acm.util.*;
import acmx.export.javax.swing.JLabel;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.text.AttributeSet.ColorAttribute;

public class Breakout2 extends GraphicsProgram {

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

	// number of colors used building one layer of bricks
	private static final int COLORNUM = 5;

	// distance between labels when two labels are on window
	private static final int BETWEEN_TWO_LABELS = 40;
	private static final int OTHER_BETWEEN_TWO_LABELS = 50;
	private static final int CENTERED = 0;

	// pause time to for program to look like it has transitions
	private static final double WAIT = 3000;

	// scores player gets when they hit one brick
	private static final int SCORE = 10;

	// pause time to see ball movement
	private static final double SLEEP_TIME = 5;

	// pause time to see rect creations
	private static final double SLEEP_TIME_TWO = 20;

	// font sizes
	private static final int FONT_SIZE_SMALL = 10;
	private static final int FONT_SIZE_MIDDLE = 25;
	private static final int FONT_SIZE_MEDIUM = 30;
	private static final int FONT_SIZE_BIG = 40;
	
	// colors used in program
	private static final Color red = Color.red;
	private static final Color orange = Color.orange;
	private static final Color yellow = Color.yellow;
	private static final Color green = Color.green;
	private static final Color blue = Color.blue;
	public static final Color lightRed = new Color(255, 102, 102);
	public static final Color lightOrange = new Color(255, 153, 0);
	public static final Color lightYellow = new Color(255, 255, 204);
	public static final Color lightGreen = new Color(102, 255, 102);
	public static final Color lightBlue = new Color(51, 204, 255);

	// instance variables
	private GImage heartone, hearttwo, heartthree; // hearts that represent turns left
	private GRect brick, paddle; // bricks and paddle
	private GLabel scoreLabel; // label that shows score
	private GOval ball; // ball
	private double vx, vy; // ball velocities
	private int brickCount, totalScore; // brick count and total score count
	private RandomGenerator rgen = RandomGenerator.getInstance(); // random generator

	GImage background = new GImage("background.png"); // background for whole program which is from famous 80's game
														// galaga

	/* Method: run() */
	/**
	 * Runs the Breakout program.
	 * 
	 * welcome to breakout 2: 80's nostalgia. everything in game from visuals to
	 * audio is from 80's movies, music and games
	 * 
	 * this method runs the program.
	 * 
	 * it has while loop so program is never ending, someone can play a game as much
	 * as they want to.
	 * 
	 */
	public void run() {
		this.setSize(WIDTH, HEIGHT); // sets application size
		addMouseListeners(); // add mouse listeners for program to respond to mouse events
		add(background); // adds background

		welcome(); // welcome labels
		breakout();
		while (true) {
			waitForClick();
			removeAll();
			add(background);
			breakout();
		}

	}

	/*
	 * main method of the game itself. first is initialization and then actual game
	 * process which is inside of the for cycle.
	 * 
	 */
	private void breakout() {
		/*
		 * game initialization happens once because every time you lose the ball and go
		 * on next turn you need to start from the same amount of bricks you lost ball
		 * at
		 * 
		 */
		gameInitialisation();
		/*
		 * for loop only starts after click. first thing happening inside of a for cycle
		 * is creation of ball and hearts since it needs to happen every time player
		 * starts a new turn. game process only starts after click.
		 * 
		 */
		for (int i = 0; i < NTURNS; i++) {
			waitForClick();
			createBall();
			createHearts(i);

			waitForClick();
			gameProcess();

			/*
			 * if brick count = 0, player wins, ball disappears, you win label appears on
			 * screen, and for cycle breaks
			 * 
			 */
			if (brickCount == 0) {
				remove(ball);
				win();
				break;
			} else {
				remove(ball);
				remove(heartthree);
				if (i == 1)
					remove(hearttwo);
			}

		}

		/*
		 * if player uses up all turns and brick count is still more than 0, it means
		 * player lost the game and game over label appears.
		 * 
		 */
		if (brickCount > 0) {
			removeAll();
			add(background);

			gameOver();
		}
	}

	/*
	 * this method creates hearts that represent lives left one by one
	 */
	private void createHearts(int i) {
		heartone = new GImage("heart.png");
		add(heartone, WIDTH - heartone.getWidth(), 0);
		if (i != 1 && i != 2) {
			hearttwo = new GImage("heart.png");
			add(hearttwo, WIDTH - hearttwo.getWidth() * 2, 0);
		}
		if (i != 1 && i != 2) {
			heartthree = new GImage("heart.png");
			add(heartthree, WIDTH - heartthree.getWidth() * 3, 0);
		}
	}

	/*
	 * everything that needs to happen before game starts is in the
	 * gameInitialisation. this sets up brick count, bricks, paddle and score label.
	 * 
	 */
	private void gameInitialisation() {
		brickCount = NBRICKS_PER_ROW * NBRICK_ROWS;
		createBricks(red, orange, yellow, green, blue);
		createPaddle();
		createScoreLabel();
	}

	/*
	 * method creates score label to track of players taken points by breaking
	 * bricks
	 * 
	 */
	private void createScoreLabel() {
		totalScore = 0;
		scoreLabel = new GLabel("your score: " + totalScore);

		// finding coordinates
		double x = (WIDTH - scoreLabel.getWidth()) / 2;
		double y = HEIGHT - PADDLE_Y_OFFSET / 2;

		scoreLabel.setColor(Color.white);
		add(scoreLabel, x, y);
	}

	// method updating label every time
	private void updateScore() {
		scoreLabel.setLabel("your score: " + totalScore);
	}

	/*
	 * bricks are created with two for cycles one which creates multiple rows and
	 * other which creates multiple bricks in the row. this method creates rows.
	 * 
	 */
	private void createBricks(Color one, Color two, Color three, Color four, Color five) {
		for (int i = 0; i < NBRICK_ROWS; i++) {
			oneRowOfBricks(i, one, two, three, four, five);
		}
	}

	// this method creates multiple bricks in row.
	private void oneRowOfBricks(int i, Color one, Color two, Color three, Color four, Color five) {
		for (int j = 0; j < NBRICKS_PER_ROW; j++) {
			// finding coordinates for bricks
			double y = BRICK_Y_OFFSET + (BRICK_HEIGHT + BRICK_SEP) * i;
			double rowWidth = NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP;
			double x = (WIDTH - rowWidth) / 2 + (BRICK_WIDTH + BRICK_SEP) * j;

			// creates one filled brick
			brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
			add(brick);
			brick.setFilled(true);

			// gets rect color
			setRectColor(i, one, two, three, four, five);

			// pauses cycle for a bit to see rects creating one by one
			pause(SLEEP_TIME_TWO);

		}
	}

	// brick colors are depended on the row and number of colors used in whole
	// program
	private void setRectColor(int i, Color one, Color two, Color three, Color four, Color five) {
		if (i % (COLORNUM * 2) < 2) {
			brick.setColor(one);
		} else if (i % (COLORNUM * 2) == 2 || i % (COLORNUM * 2) == 3) {
			brick.setColor(two);
		} else if (i % (COLORNUM * 2) == 4 || i % (COLORNUM * 2) == 5) {
			brick.setColor(three);
		} else if (i % (COLORNUM * 2) == 6 || i % (COLORNUM * 2) == 7) {
			brick.setColor(four);
		} else if (i % (COLORNUM * 2) == 8 || i % (COLORNUM * 2) == 9) {
			brick.setColor(five);
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
		paddle.setColor(Color.white);
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

		// creating white ball using GOval
		ball = new GOval(x, y, BALL_RADIUS * 2, BALL_RADIUS * 2);
		ball.setFilled(true);
		ball.setColor(Color.white);
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
			if (ball.getY() >= getHeight()) {
				String ballFallAudio = (".//fall.wav");
				audio(ballFallAudio); // if ball falls mario falling sound is added

				break;

			}
			if (brickCount == 0) {
				break;
			}
		}
	}

	/*
	 * ball velocity is chosen to be the most comfortable. vy is and vx is randomly
	 * generated. vx's trajectory is also random
	 * 
	 */
	private void ballVelocity() {
		vy = 1.5;
		vx = rgen.nextDouble(0.5, 1.5);
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
		 * if collider is a paddle program is moving to method for paddle collision case
		 * if collider is anything else (brick in this case) program is moving to method
		 * for brick collision
		 * 
		 */
		if (collider == paddle) {
			collisionWithPaddle();
		} else if (collider != null && collider != scoreLabel && collider != background && collider != heartone
				&& collider != hearttwo && collider != heartthree) {
			remove(collider);
			collisionWithBrick();
		}

	}

	/*
	 * if collision is with a brick brick count is reduced, ball is reflected score
	 * audio is played and score is updated
	 * 
	 */
	private void collisionWithBrick() {
		brickCount--;
		vy = -vy;

		String scoreAudio = (".//score.wav");
		audio(scoreAudio); // score audio is also from game mario

		totalScore += SCORE;
		updateScore();
	}

	/*
	 * if collision is with paddle it is reflected and sound is played
	 * 
	 */
	private void collisionWithPaddle() {
		vy = -vy;
		String hitAudio = (".//pacman.wav");
		audio(hitAudio); // hit audio is from game pacman
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
	 * method for labels, it sets font and color of the label and puts label on
	 * window
	 * 
	 */
	private void putUpLabel(String string, Color color, int f, int offsetFromCenter) {
		// creating label
		GLabel label = new GLabel(string);

		// setting font
		Font myFont = new Font("Courier", Font.BOLD, f);

		// setting label
		label.setFont(myFont);
		label.setColor(color);

		// finding coordinates
		double x = (WIDTH - label.getWidth()) / 2;
		double y = (HEIGHT - offsetFromCenter + label.getAscent()) / 2;

		add(label, x, y);
	}

	// method for playing audio, it gets audio name in a form of string
	private void audio(String string) {
		try {
			AudioInputStream win = AudioSystem.getAudioInputStream(new File(string).getAbsoluteFile());
			Clip clip = AudioSystem.getClip();
			clip.open(win);
			clip.start();
		} catch (Exception e) {
		}
	}

	/*
	 * method for stuff game does as soon as program is run
	 */
	private void welcome() {
		String welcomeAudio = (".//welcome.wav");
		audio(welcomeAudio); // welcome audio is from movie Star Wars

		printWelcome();
		printGoodLuck();

		pause(WAIT);
		removeAll();
		add(background);
	}

	// puts up welcome label
	private void printWelcome() {
		String welcome = ("WELCOME TO BREAKOUT");
		int fontSize = FONT_SIZE_MEDIUM;
		int offset = CENTERED;
		putUpLabel(welcome, Color.cyan, fontSize, offset);
	}

	// puts up label to wish player luck
	private void printGoodLuck() {
		String goodluck = ("May The Odds Be Ever In Your Favour");
		int fontSize = FONT_SIZE_SMALL;
		int offset = BETWEEN_TWO_LABELS;
		putUpLabel(goodluck, Color.cyan, fontSize, offset);
	}

	/*
	 * method for everything that happens after player loses game
	 * 
	 */
	private void gameOver() {
		String gameOverAudio = (".//gameover.wav");
		audio(gameOverAudio); // audio is iconic 80's song Never Gonna Give You Up by Rick Astley

		// first game over printed and after a while try again label
		printGameOver();
		pause(WAIT);
		printTryAgain();
	}

	// puts up game over label
	private void printGameOver() {
		String gameOver = ("Game Over!");
		int fontSize = FONT_SIZE_BIG;
		int offset = CENTERED;
		putUpLabel(gameOver, Color.red, fontSize, offset);
	}

	// puts up click to try again label
	private void printTryAgain() {
		String tryAgain = ("Click to try again!");
		int fontSize = FONT_SIZE_MIDDLE;
		int offset = OTHER_BETWEEN_TWO_LABELS;
		putUpLabel(tryAgain, Color.white, fontSize, offset);
	}

	// method for everything that happens after a win
	private void win() {
		String winnerAudio = (".//win.wav");
		audio(winnerAudio); // win audio if from song money money money by abba

		printWin();

		// after winning game can move to level two after click
		waitForClick();
		removeAll();
		add(background);
		levelTwo();
	}

	// tells player to click
	private void printWin() {
		String win = ("click");
		int fontSize = FONT_SIZE_BIG;
		int offset = CENTERED;
		putUpLabel(win, Color.orange, fontSize, offset);
	}

	// level two method same as level one but initialization is different
	private void levelTwo() {
		gameInitialisationLevelTwo();
		for (int i = 0; i < NTURNS; i++) {
			waitForClick();
			createBall();
			createHearts(i);

			waitForClick();
			gameProcess();

			if (brickCount == 0) {
				remove(ball);
				winLevelTwo();
				break;
			} else {
				remove(ball);
				remove(heartthree);
				if (i == 1)
					remove(hearttwo);
			}

		}
		if (brickCount > 0) {
			removeAll();
			add(background);

			gameOver();
		}
	}

	// instead of one layer of bricks two layers is created so game becomes a bit
	// harder and brick count is twice as big
	private void gameInitialisationLevelTwo() {
		brickCount = NBRICKS_PER_ROW * NBRICK_ROWS * 2;
		createBricks(lightRed, lightOrange, lightYellow, lightGreen, lightBlue);
		createBricks(red, orange, yellow, green, blue);
		createPaddle();
		createScoreLabel();
	}

	// same as level one win but this one moves to third level
	private void winLevelTwo() {
		String winnerAudio = (".//win.wav");
		audio(winnerAudio);

		printWin();

		waitForClick();
		removeAll();
		add(background);
		levelThree();
	}

	// same as previous levels but initialization is different
	private void levelThree() {
		gameInitialisationLevelThree();
		for (int i = 0; i < NTURNS; i++) {
			waitForClick();
			createBall();
			createHearts(i);

			waitForClick();
			gameProcess();

			if (brickCount == 0) {
				remove(ball);
				winLevelThree();
				break;
			} else {
				remove(ball);
				remove(heartthree);
				if (i == 1)
					remove(hearttwo);
			}

		}
		if (brickCount > 0) {
			removeAll();
			add(background);

			gameOver();
		}

	}

	/*
	 * this level is not harder. bricks are one big rows but they are layered for
	 * three times
	 * 
	 */
	private void gameInitialisationLevelThree() {
		brickCount = NBRICK_ROWS * 3;

		for (int j = 0; j < 3; j++) {
			createBricksLevelThree(red, orange, yellow, green, blue);
		}

		createPaddle();
		createScoreLabel();
	}

	// creating big bricks
	private void createBricksLevelThree(Color one, Color two, Color three, Color four, Color five) {
		for (int i = 0; i < NBRICK_ROWS; i++) {
			// getting coordinates
			double y = BRICK_Y_OFFSET + (BRICK_HEIGHT + BRICK_SEP) * i;
			double brickWidth = NBRICKS_PER_ROW * BRICK_WIDTH + (NBRICKS_PER_ROW - 1) * BRICK_SEP;
			double x = (WIDTH - brickWidth) / 2;

			// creating birck
			brick = new GRect(x, y, brickWidth, BRICK_HEIGHT);
			add(brick);
			brick.setFilled(true);

			setRectColor(i, one, two, three, four, five);
			pause(20);
		}
	}

	// writes "you win" since it's last level of game
	private void winLevelThree() {
		String win = ("You Win!");
		int fontSize = FONT_SIZE_BIG;
		int offset = CENTERED;
		putUpLabel(win, Color.orange, fontSize, offset);

		String winnerAudio = (".//win.wav");
		audio(winnerAudio);
	}
}
