package arkanoid2;

import java.awt.*;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Arkanoid extends JPanel implements KeyListener, Runnable {

    private static final int FPS = 60;
    private int paddleBroad = 60;
    private final int WIDTH = 840;
    private final int HEIGHT = 600;
    private int paddleX = (WIDTH - paddleBroad) / 2;
    private int ballX = (WIDTH - paddleBroad / 2) / 2;
    private int ballY = HEIGHT - 40;
    private int ballSpeedX = 3;
    private int ballSpeedY = -3;
    private int ballSize = 10;
    private boolean gameOver, pause;
    private int bricksX = 12;
    private int bricksY = 6;
    private int paddleSpeed = 35;
    public ArrayList<Block> blocks = new ArrayList<>();
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private int lives = 3;
    private int score = 0;
    private int level = 1;

    private Thread gameThread;
    private Thread paddleThread;

    public Arkanoid() {
        this.setPreferredSize(new Dimension(WIDTH + 240, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        startGame();
        startThread();
        startPaddleThread();
    }

    private void startThread() {
    	gameThread = new Thread(this);
    	gameThread.start();
    }

    private void startPaddleThread() {
        paddleThread = new Thread(() -> {
            while (gameOver == false) {
            	 if (pause) {
                     try {
                         Thread.sleep(50);
                        
                     } catch (InterruptedException ex) {
                         ex.printStackTrace();
                     }
                     continue;
                 }
                 if (gameOver) {
                    GameOver();
                    break;
                 }
                if (movingLeft && paddleX > 0) {
                    paddleX --;
                }
                if (movingRight && paddleX < WIDTH - paddleBroad) {
                    paddleX ++;
                }
                repaint();
                Toolkit.getDefaultToolkit().sync();

                try {
                    Thread.sleep((int)75/paddleSpeed);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        paddleThread.start();
    }

    private void populateBlocks() {
        for (int j = 0; j < bricksY; j++) {
            for (int i = 0; i < bricksX; i++) {
                Block block = new Block(((i + 1) * Block.size_X), (j + 1) * Block.size_Y, Color.BLUE);
                blocks.add(block);
            }
        }
    }

    @Override
    public void run() {
        double drawInterval = 1000.0 / FPS;
        double delta = 0;
        long lastTime = System.currentTimeMillis();
        long currentTime;

        while (gameThread != null && !gameOver) {
            currentTime = System.currentTimeMillis();
            delta += (double) (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                moveBall();
                repaint();
                Toolkit.getDefaultToolkit().sync();
                delta--;
            }
        }
        GameOver();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT && paddleX > 0) {
            movingLeft = true;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && paddleX < WIDTH - paddleBroad) {
            movingRight = true;
        }
    

         else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            pause = !pause;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            movingLeft = false;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            movingRight = false;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);

        // Paddle     
        g.fillRect(paddleX, HEIGHT - 20, paddleBroad, 10);

        // Ball
        g.fillOval(ballX, ballY, ballSize, ballSize);

        // Info panel
        g.drawRect(WIDTH, 0, 240, HEIGHT);
        g.setColor(Color.WHITE);
        g.drawString("Lives: " + lives, WIDTH + 20, 20);
        g.drawString("Score: " + score, WIDTH + 20, 40);
        g.drawString("Level: " + level, WIDTH + 20, 60);

        // Blocks
        for (Block block : blocks) {
            block.Draw((Graphics2D) g);
        }
        if (pause) {
            g.setColor(Color.YELLOW);
            g.setFont((new Font("Arial", Font.BOLD, 50)));
            g.drawString("PAUSE", WIDTH/2 - 100, HEIGHT/2);
        }
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", WIDTH/2 - 100, HEIGHT/2);
        }
    }

    private void GameOver() {
        // pause = true;
        gameOver = true;
        System.out.println("GAME OVER");
    }

    private void moveBall() {
        if (ballX + ballSize / 2 <= 0 || ballX + ballSize >= WIDTH) {
            ballSpeedX = -ballSpeedX;
        }
        if (ballY + ballSize / 2 <= 0 || ((ballY + 20 + 10 >= HEIGHT) && (ballX >= paddleX && ballX <= paddleX + paddleBroad))) {
            ballSpeedY = -ballSpeedY;
        }
        if (ballY >= HEIGHT) {
        	lives -= 1;
        	if (lives <= 0) {
                lives = 0;
        		gameOver = true;
        	}
        	else {
        		gameOver = false;
                pause = true;
                launchBall();
        	}
        }

        if (!pause && !gameOver) {
            ballX += ballSpeedX;
            ballY += ballSpeedY;

            // Vérifier la collision avec les blocs
            for (int i = 0; i < blocks.size(); i++) {
                Block block = blocks.get(i);

                // Définir la hitbox de la balle comme un Rectangle
                Rectangle ballRect = new Rectangle(ballX, ballY, ballSize, ballSize);
                Rectangle blockRect = new Rectangle(block.x, block.y, Block.size_X, Block.size_Y);

                if (ballRect.intersects(blockRect)) {
                    // Supprimer le bloc
                    blocks.remove(i);
                    score ++;
                    i--;
                    if (blocks.size() == 0) {
                        level ++;
                        populateBlocks();
                        launchBall();
                        pause = true;
                        //i = blocks.size();
                    }

                    // Détection précise du côté touché
                    boolean hitFromLeft = ballX + ballSize - ballSpeedX <= block.x;
                    boolean hitFromRight = ballX - ballSpeedX >= block.x + Block.size_X;
                    boolean hitFromTop = ballY + ballSize - ballSpeedY <= block.y;
                    boolean hitFromBottom = ballY - ballSpeedY >= block.y + Block.size_Y;

                    if ((hitFromLeft && hitFromRight) || (hitFromTop && hitFromBottom)) {
                        ballSpeedX = -ballSpeedX;
                        ballSpeedY = -ballSpeedY;
                    } else if (hitFromLeft || hitFromRight) {
                        ballSpeedX = -ballSpeedX;
                    } else {
                        ballSpeedY = -ballSpeedY;
                    }
                    break;
                }
            }

            repaint();
            Toolkit.getDefaultToolkit().sync();
        }
    }

    private void startGame() {
        populateBlocks();
    }
    private void launchBall(){
        ballX = (paddleX + paddleBroad / 2);
        ballY = HEIGHT - 40;
        if (ballSpeedY > 0)
        ballSpeedY = -ballSpeedY ; 
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Kanoid");
        Arkanoid game = new Arkanoid();
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
