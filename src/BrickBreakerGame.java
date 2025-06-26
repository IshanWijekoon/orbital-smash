import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class BrickBreakerGame extends JFrame implements ActionListener{

	 private GamePanel game;
	    private Timer gameTimer;
	    private JLabel scoreLabel, livesLabel, levelLabel;
	    private int currentLevel = 1;

	    public BrickBreakerGame() {
	        super("Brick Breaker");
	        setSize(700, 600);
	        setLayout(null);
	        setDefaultCloseOperation(EXIT_ON_CLOSE);

	        // Game panel setup
	        game = new GamePanel();
	        game.setBounds(25, 25, 500, 500);
	        add(game);

	        // UI elements setup
	        Font gameFont = new Font("Arial", Font.BOLD, 24);
	        levelLabel = createStatusLabel("Level: " + currentLevel, 550, 50, gameFont);
	        scoreLabel = createStatusLabel("Score: 0", 550, 150, gameFont);
	        livesLabel = createStatusLabel("Lives: 3", 550, 250, gameFont);

	        // Game timer
	        gameTimer = new Timer(10, this);
	        gameTimer.start();

	        setVisible(true);
	    }

	    private JLabel createStatusLabel(String text, int x, int y, Font font) {
	        JLabel label = new JLabel(text);
	        label.setFont(font);
	        label.setForeground(Color.black);
	        label.setBounds(x, y, 200, 30);
	        add(label);
	        return label;
	    }

	    public void actionPerformed(ActionEvent e) {
	        game.updateGameState();
	        scoreLabel.setText("Score: " + game.getScore());
	        livesLabel.setText("Lives: " + game.getLives());
	        
	        if (game.isLevelComplete()) {
	            currentLevel++;
	            levelLabel.setText("Level: " + currentLevel);
	            game.nextLevel(currentLevel);
	        }
	    }

		
	

}

class GamePanel extends JPanel implements KeyListener {
    private Paddle paddle;
    private Ball ball;
    private Brick[][] bricks;
    private int score = 0;
    private int lives = 3;
    private boolean gameRunning = true;

    public GamePanel() {
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        initializeGame();
    }

    private void initializeGame() {
        paddle = new Paddle(200, 450, 80, 10);
        ball = new Ball(250, 430, 15);
        generateBricks(3, 7);
    }

    private void generateBricks(int rows, int cols) {
        bricks = new Brick[rows][cols];
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++) {
                bricks[i][j] = new Brick(j * 70 + 10, i * 30 + 50, 60, 20);
            }
        }
    }

    public void updateGameState() {
        if (!gameRunning) return;
        
        ball.move();
        checkCollisions();
        repaint();
    }

    private void checkCollisions() {
        // Wall collisions
        if (ball.getX() <= 0 || ball.getX() >= 485) ball.reverseX();
        if (ball.getY() <= 0) ball.reverseY();
        
        // Paddle collision
        if (ball.getBounds().intersects(paddle.getBounds())) {
            ball.reverseY();
            ball.adjustAngle(paddle.getX());
        }
        
        // Brick collisions
        for(int i = 0; i < bricks.length; i++) {
            for(int j = 0; j < bricks[i].length; j++) {
                if (bricks[i][j].isVisible() && ball.getBounds().intersects(bricks[i][j].getBounds())) {
                    bricks[i][j].setVisible(false);
                    ball.reverseY();
                    score += 10;
                }
            }
        }
        
        // Bottom boundary check
        if (ball.getY() >= 485) {
            loseLife();
        }
    }

    private void loseLife() {
        lives--;
        if (lives <= 0) {
            gameOver();
        } else {
            resetBall();
        }
    }

    private void resetBall() {
        ball.setPosition(250, 430);
        ball.resetSpeed();
    }

    private void gameOver() {
        gameRunning = false;
        JOptionPane.showMessageDialog(this, "Game Over! Final Score: " + score);
        System.exit(0);
    }

    public void nextLevel(int level) {
        ball.increaseSpeed(level * 0.5);
        generateBricks(level + 2, 7);
        resetBall();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paddle.draw(g);
        ball.draw(g);
        for(Brick[] row : bricks) {
            for(Brick brick : row) {
                if (brick.isVisible()) brick.draw(g);
            }
        }
    }

    // KeyListener methods
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT && paddle.getX() > 0) {
            paddle.move(-20);
        }
        if (key == KeyEvent.VK_RIGHT && paddle.getX() < 420) {
            paddle.move(20);
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // Getters
    public int getScore() { return score; }
    public int getLives() { return lives; }
    public boolean isLevelComplete() {
        for(Brick[] row : bricks) {
            for(Brick brick : row) {
                if (brick.isVisible()) return false;
            }
        }
        return true;
    }
}

// Helper classes
class Paddle {
    private int x, y, width, height;
    
    public Paddle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void move(int dx) {
        x += dx;
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, width, height);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public int getX() { return x; }
}

class Ball {
    private double x, y, dx = 2, dy = -3;
    private int diameter;
    
    public Ball(int x, int y, int diameter) {
        this.x = x;
        this.y = y;
        this.diameter = diameter;
    }
    
    public void move() {
        x += dx;
        y += dy;
    }
    
    public void reverseX() { dx *= -1; }
    public void reverseY() { dy *= -1; }
    
    public void adjustAngle(int paddleX) {
        double hitPosition = (x + diameter/2 - paddleX) / 80.0;
        dx = hitPosition * 5;
    }
    
    public void increaseSpeed(double factor) {
        dx += (dx > 0 ? factor : -factor);
        dy += (dy > 0 ? factor : -factor);
    }
    
    public void resetSpeed() {
        dx = 2;
        dy = -3;
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.white);
        g.fillOval((int)x, (int)y, diameter, diameter);
    }
    
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, diameter, diameter);
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
}

class Brick {
    private int x, y, width, height;
    private boolean visible = true;
    
    public Brick(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(x, y, width, height);
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}
