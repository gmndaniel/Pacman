package PacmanPack;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import static PacmanPack.Pacman.Type.*;
import static PacmanPack.SpriteMap.getSprite;
import static java.awt.event.KeyEvent.*;

public class Pacman extends Applet implements Runnable, KeyListener {

    final static int VIRT_BORDERS = 2;
    final static int FIELD_WIDTH = 28 + VIRT_BORDERS;
    final static int R_PORTAL_COL = FIELD_WIDTH - 1;
    final static int FIELD_HEIGHT = 31;
    private final static int PIXELS_WIDTH = 550;
    private final static int PIXELS_HEIGHT = 600;
    private final static int PIXELS_BAR_HEIGHT = 40;
    private final static double SPRITE_R = 32.0 / 2.0;

    final static double W_COEFF = (double) PIXELS_WIDTH / (double) (FIELD_WIDTH - VIRT_BORDERS);
    final static double H_COEFF = (double) (PIXELS_HEIGHT - PIXELS_BAR_HEIGHT) / (double) (FIELD_HEIGHT);


    private static Mixer mixer;
    private static Clip eatDotsClip;
    private static Clip sirenClip;
    private static Clip pacmanDeathClip;
    private static Clip eatSuperClip;
    private static Clip eatGhostClip;
    private static Clip gameStartClip;
    private static AudioInputStream audioStream;

    Graphics2D gfx;
    private BufferedImage mazeImg;
    private BufferedImage readyImg;
    private Cell blinkyPath;
    private Cell pinkyPath;
    private Cell inkyPath;
    private Cell clydePath;
    private Cell lastPacmanCell;

    public final static int KEY_UP = 0;
    public final static int KEY_DOWN = 1;
    public final static int KEY_LEFT = 2;
    public final static int KEY_RIGHT = 3;
    public final static int KEY_RESTART = 4;
    private boolean[] keysPressed;
    private int lives;
    private boolean pacmanIsAlive = true;
    private boolean readySign = true;
    private BufferedImage pacmanLife;
    private BufferedImage gameOverImg;

    public enum Type {WALL, DOT, EMPTY, SUPER, L_PORTAL, R_PORTAL, GHOST_HOUSE}

    public enum Facing {UP, DOWN, LEFT, RIGHT}

    static Cell[][] field;
    static int[][] fieldMatrix;

    static ThePacman pacman;
    static Ghost blinky;
    static Ghost pinky;
    static Ghost inky;
    static Ghost clyde;
    GameMode gameMode;
    static boolean pacmanIsDying;
    static boolean standStill;

    private int framesOnSameCell = 0;
    int score;
    static int dots = 0;
    static boolean muteSound = false;

    @Override
    public void init() {
        this.resize(PIXELS_WIDTH, PIXELS_HEIGHT);
        setBackground(Color.BLACK);
        loadReadyImg();
        loadGameOverImg();
        loadPacmanLifeImg();
        setupSound();
        gameMode = new GameMode();
        keysPressed = new boolean[5];
        Thread gameThread = new Thread(this);
        gameThread.start();
        this.addKeyListener(this);
    }

    private void setupSound() {
        Mixer.Info[] mixInfos = AudioSystem.getMixerInfo();
        mixer = AudioSystem.getMixer(mixInfos[0]);
        DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
        try {
            eatDotsClip = (Clip) mixer.getLine(dataInfo);
            sirenClip = (Clip) mixer.getLine(dataInfo);
            pacmanDeathClip = (Clip) mixer.getLine(dataInfo);
            eatSuperClip = (Clip) mixer.getLine(dataInfo);
            eatGhostClip = (Clip) mixer.getLine(dataInfo);
            gameStartClip = (Clip) mixer.getLine(dataInfo);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }

        try {
            URL eatDotsURL = Pacman.class.getResource("/PacmanPack/sounds/pacman_chomp.wav");
            URL sirenURL = Pacman.class.getResource("/PacmanPack/sounds/pacman_siren.wav");
            URL pacmanDeathURL = Pacman.class.getResource("/PacmanPack/sounds/pacman_death.wav");
            URL eatSuperURL = Pacman.class.getResource("/PacmanPack/sounds/pacman_super.wav");
            URL eatGhostURL = Pacman.class.getResource("/PacmanPack/sounds/pacman_eatghost.wav");
            URL gameStartURL = Pacman.class.getResource("/PacmanPack/sounds/pacman_beginning.wav");

            loadStream(eatDotsURL, eatDotsClip);
            loadStream(sirenURL, sirenClip);
            loadStream(pacmanDeathURL, pacmanDeathClip);
            loadStream(eatSuperURL, eatSuperClip);
            loadStream(eatGhostURL, eatGhostClip);
            loadStream(gameStartURL, gameStartClip);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void loadStream(URL eatDotsURL, Clip eatDotsClip) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        audioStream = AudioSystem.getAudioInputStream(eatDotsURL);
        eatDotsClip.open(audioStream);
    }

    private void gameModeCycle() {
        if (!gameMode.isAlive()) {
            gameMode = new GameMode();
            if (gameMode.getCycle() <= 2) {
                gameMode.setGameMode(7, 20);
            } else if (gameMode.getCycle() == 3) {
                gameMode.setGameMode(5, 20);
            } else if (gameMode.getCycle() == 4) {
                int INFINITE_CHASE = 999999;
                gameMode.setGameMode(5, INFINITE_CHASE);
            }
            gameMode.start();
        }
    }

    private void createSprites() {
        pacman = new ThePacman(field[23][15], 0);
        blinky = new Blinky(field[11][15], 1);
        pinky = new Pinky(field[14][15], 2);
        inky = new Inky(field[14][13], 3);
        clyde = new Clyde(field[14][16], 4);
    }

    @Override
    public void paint(Graphics g) {
        loadMazeImage();
        gfx = (Graphics2D) mazeImg.getGraphics();
//        drawPath(gfx, blinkyPath, 1);
//        drawPath(gfx, pinkyPath, 2);
//        drawPath(gfx, inkyPath, 3);
//        drawPath(gfx, clydePath, 4);
        drawItems(gfx);
        drawSprites(gfx);
        drawScore(gfx);
        drawLives(gfx);
        drawReadySign(gfx);
        drawGameOver(gfx);
        g.drawImage(mazeImg, 0, 0, this);
    }

    private void drawGameOver(Graphics2D gfx) {
        if (lives <= 0) {
            gfx.drawImage(gameOverImg, 180, 300, this);
        }
    }

    private void drawLives(Graphics2D g) {
        for (int i = 0; i < lives; ++i) {
            g.drawImage(pacmanLife, 10 + 30 * i, PIXELS_HEIGHT - 35, this);
        }
    }

    private void drawReadySign(Graphics2D g) {
        if (readySign) {
            g.drawImage(readyImg, 190, 295, this);
        }
    }

    private void drawScore(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Helvetica", Font.BOLD, 16));
        g.drawString("SCORE", PIXELS_WIDTH / 2 - 40, PIXELS_HEIGHT - 22);
        g.drawString("" + score, PIXELS_WIDTH / 2 - 40, PIXELS_HEIGHT - 5);

    }

    private void drawSprites(Graphics2D g) {
        pacman.draw(g);
        blinky.draw(g);
        pinky.draw(g);
        inky.draw(g);
        clyde.draw(g);
    }

    private void moveSprites() {
        pacman.move(gameMode.isChaseMode());
        blinkyPath = blinky.move(gameMode.isChaseMode());
        pinkyPath = pinky.move(gameMode.isChaseMode());
        inkyPath = inky.move(gameMode.isChaseMode());
        clydePath = clyde.move(gameMode.isChaseMode());
    }

    private void drawPath(Graphics2D g, Cell end, int spriteLevel) {
        Cell c = end;
        while (c != null) {
            int r = 5;
            int screenX = (int) (8 + (c.getCol() - VIRT_BORDERS / 2) * W_COEFF);
            int screenY = (int) (8 + c.getRow() * H_COEFF);
            g.setColor(Color.BLUE);
            g.fillRect(screenX - r, screenY - r, r * 2, r * 2);
            c = c.getParent(spriteLevel);
        }
    }

    private void loadMazeImage() {
        try {
            mazeImg = ImageIO.read(getClass().getResource("/PacmanPack/images/maze_small.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGameOverImg() {
        try {
            gameOverImg = ImageIO.read(getClass().getResource("/PacmanPack/images/game_over.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadReadyImg() {
        try {
            readyImg = ImageIO.read(getClass().getResource("/PacmanPack/images/ready.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadPacmanLifeImg() {
        pacmanLife = getSprite(1, 0);
    }

    private void makeField() {
        int d = 1; // dot
        int e = 2; // empty
        int g = 3; // right portal
        int s = 4; // super
        int L = 5; // ghostHouseLeft portal
        int R = 6; // right portal
        populateFieldMatrix(d, e, g, s, L, R);
        field = new Cell[FIELD_HEIGHT][FIELD_WIDTH];
        createCells(d, e, g, s, L, R);
        setCellsNeighbors();
    }

    private static void createCells(int d, int e, int g, int s, int l, int r) {
        for (int i = 0; i < FIELD_HEIGHT; ++i) {
            for (int j = 0; j < FIELD_WIDTH; ++j) {
                Type type = setCellType(fieldMatrix[i][j], d, e, g, s, l, r);
                field[i][j] = new Cell(i, j, type);
            }
        }
    }

    private void setCellsNeighbors() {
        for (int i = 0; i < FIELD_HEIGHT; ++i) {
            for (int j = 0; j < FIELD_WIDTH; ++j) {
                field[i][j].setCellNeighbors();
            }
        }
    }

    private static Type setCellType(int t, int d, int e, int g, int s, int L, int R) {
        if (t == d) {
            ++dots;
            return DOT;
        }
        if (t == e) return EMPTY;
        if (t == g) return GHOST_HOUSE;
        if (t == s) return SUPER;
        if (t == L) return L_PORTAL;
        if (t == R) return R_PORTAL;
        return WALL;
    }

    private void populateFieldMatrix(int d, int e, int g, int s, int L, int R) {
        fieldMatrix = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, d, d, d, d, d, d, d, d, d, d, d, d, 0, 0, d, d, d, d, d, d, d, d, d, d, d, d, 0, 0},
                {0, 0, d, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, d, 0, 0},
                {0, 0, s, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, s, 0, 0},
                {0, 0, d, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, d, 0, 0},
                {0, 0, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, 0, 0},
                {0, 0, d, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, d, 0, 0},
                {0, 0, d, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, d, 0, 0},
                {0, 0, d, d, d, d, d, d, 0, 0, d, d, d, d, 0, 0, d, d, d, d, 0, 0, d, d, d, d, d, d, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, e, 0, 0, e, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, e, 0, 0, e, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, e, e, e, e, e, e, e, e, e, e, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, e, 0, 0, 0, 0, g, 0, 0, 0, e, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, e, 0, 0, 0, e, e, 0, 0, 0, e, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {L, e, e, e, e, e, e, d, e, e, e, 0, 0, e, e, e, e, 0, 0, e, e, e, d, e, e, e, e, e, e, R},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, e, 0, 0, 0, 0, 0, 0, 0, 0, e, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, e, 0, 0, 0, 0, 0, 0, 0, 0, e, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, e, e, e, e, e, e, e, e, e, e, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, e, 0, 0, 0, 0, 0, 0, 0, 0, e, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, d, 0, 0, e, 0, 0, 0, 0, 0, 0, 0, 0, e, 0, 0, d, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, d, d, d, d, d, d, d, d, d, d, d, d, 0, 0, d, d, d, d, d, d, d, d, d, d, d, d, 0, 0},
                {0, 0, d, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, d, 0, 0},
                {0, 0, d, 0, 0, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, d, 0, 0, 0, 0, d, 0, 0},
                {0, 0, s, d, d, 0, 0, d, d, d, d, d, d, d, e, e, d, d, d, d, d, d, d, 0, 0, d, d, s, 0, 0},
                {0, 0, 0, 0, d, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, d, 0, 0, 0, 0},
                {0, 0, 0, 0, d, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, d, 0, 0, 0, 0},
                {0, 0, d, d, d, d, d, d, 0, 0, d, d, d, d, 0, 0, d, d, d, d, 0, 0, d, d, d, d, d, d, 0, 0},
                {0, 0, d, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, d, 0, 0},
                {0, 0, d, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, d, 0, 0, d, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, d, 0, 0},
                {0, 0, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, d, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        };
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }

    @Override
    public void run() {
        boolean gameRunning = true;
        while (gameRunning) {
            stopClip(eatSuperClip);
            stopClip(sirenClip);
            stopClip(eatDotsClip);
            score = 0;
            makeField();
            lives = 3;
            keysPressed[KEY_RESTART] = false;

            respawnAll();

            startingGhostsDance(false);
            playIntroMusic();
            while (!keysPressed[KEY_RESTART]) {

                if (!pacmanIsAlive && lives > 0) {
                    respawnAll();
                    new Thread(this::delayedDisableStandStillShort).start();
                }
                if (lives > 0) {
                    new Thread(this::delayedReleaseGhosts).start();
                }
                while (pacmanIsAlive && !keysPressed[KEY_RESTART]) {
                    if (!standStill) {
                        handleKeyPresses();
                        moveSprites();
                        gameModeCycle();
                        handleCollisions();
                        handleFrightenedModes();
                        eatDots();
                        eatSuper();
                    }
                    repaint();
                    sleep(16);
                }

                if (lives <= 0) {
                    standStill = true;
                }

                repaint();
                sleep(16);
            }
        }
    }

    private void respawnAll() {
        createSprites();
        pacmanIsDying = false;
        pacmanIsAlive = true;
        startingGhostsDance(true);
        standStill = true;
    }

    private void startingGhostsDance(boolean d) {
        pinky.setDancingInGhostHouse(d);
        inky.setDancingInGhostHouse(d);
        clyde.setDancingInGhostHouse(d);
    }

    private void delayedReleaseGhosts() {
        sleep(4100);
        sleep(3000);
        pinky.setDancingInGhostHouse(false);
        sleep(4000);
        inky.setDancingInGhostHouse(false);
        sleep(4000);
        clyde.setDancingInGhostHouse(false);
    }

    private void playIntroMusic() {
        sleep(1000);
        standStill = true;
        playClip(gameStartClip);

        new Thread(this::delayedDisableStandStill).start();
    }

    private void delayedDisableStandStill() {
        sleep(4100);
        startingGhostsDance(true);
        standStill = false;
        readySign = false;
    }

    private void delayedDisableStandStillShort() {
        sleep(1000);
        startingGhostsDance(true);
        standStill = false;
    }

    private void handleCollisions() {
        boolean collision = false;

        collision |= pacmanGhostCollision(blinky) & !blinky.isFrightened;
        collision |= pacmanGhostCollision(pinky) & !pinky.isFrightened;
        collision |= pacmanGhostCollision(inky) & !inky.isFrightened;
        collision |= pacmanGhostCollision(clyde) & !clyde.isFrightened;

        if (collision && !pacmanIsDying) {
            pacmanDies();
        }
    }

    private void pacmanDies() {
        pacmanIsDying = true;
        stopClip(sirenClip);
        stopClip(eatDotsClip);
        playClip(pacmanDeathClip);
        pacmanDeathAnimation();
        new Thread(this::delayedRespawn).start();
    }

    private void delayedRespawn() {
        sleep(2000);
        --lives;
        pacmanIsAlive = false;
    }

    private void pacmanDeathAnimation() {
        pacman.startDeathAnimation();
    }


    private boolean pacmanGhostCollision(Ghost ghost) {
        double dRow = Math.abs(pacman.getVirtualRow() - ghost.getVirtualRow());
        double dCol = Math.abs(pacman.getVirtualCol() - ghost.getVirtualCol());
        if (dRow <= 1.2 && dCol <= 1.2) {
            return true;
        }
        return false;
    }

    private void eatDots() {
        Cell cell = pacman.getCell();
        if (cell.getType() == DOT) {
            playLoopClip(eatDotsClip);
            score += 10;
            --dots;
            cell.setType(EMPTY);
            lastPacmanCell = cell;
        } else if (framesOnSameCell > 30 || lastPacmanCell != cell) {
            framesOnSameCell = 0;
            stopClip(eatDotsClip);
        } else if (cell == lastPacmanCell) {
            ++framesOnSameCell;
        }
    }

    private void eatSuper() {
        Cell cell = pacman.getCell();
        if (cell.getType() == SUPER) {
            cell.setType(EMPTY);
            gameMode.setFrightened(6);
            setGhostsFrightenedStage(1);
            framesOnSameCell = 0;
            stopClip(eatDotsClip);
        }
    }

    private void setGhostsFrightenedStage(int s) {
        setFrightenedStageIfNotEyes(blinky, s);
        setFrightenedStageIfNotEyes(pinky, s);
        setFrightenedStageIfNotEyes(inky, s);
        setFrightenedStageIfNotEyes(clyde, s);
    }

    private void setFrightenedStageIfNotEyes(Ghost ghost, int s) {
        if (ghost.getFrightenedStage() != 3) {
            ghost.setFrightenedStage(s);
        }
    }

    private void handleFrightenedModes() {
        if (!pacmanIsDying) {
            if (gameMode.getFrightenedTime() == 0) {
                stopClip(eatSuperClip);
                playLoopClip(sirenClip);
            } else {
                stopClip(sirenClip);
                playLoopClip(eatSuperClip);
            }
        }
        handleFrightenedMode(blinky);
        handleFrightenedMode(pinky);
        handleFrightenedMode(inky);
        handleFrightenedMode(clyde);
    }

    private void handleFrightenedMode(Ghost ghost) {

        if (ghost.getFrightenedStage() == 1) {
            ghost.setBlinking(false);
            ghost.setIsFrightened(true);
            ghost.setSpeed(0.05);
            ghost.setFrightenedStage(2);
        }

        if (ghost.getFrightenedStage() == 2 && gameMode.getFrightenedTime() == 2) {
            ghost.setBlinking(true);
        }

        if (ghost.getFrightenedStage() == 2 && gameMode.getFrightenedTime() == 0) {
            ghost.setBlinking(false);
            ghost.setIsFrightened(false);
            ghost.snapToCell();
            ghost.resetSpeed();
            ghost.setFrightenedStage(0);
        }

        if (ghost.getFrightenedStage() == 2 && pacmanGhostCollision(ghost)) {
            playClip(eatGhostClip);
            ghost.setBlinking(false);
            ghost.setEaten(true);
            ghost.snapToCell();
            ghost.setSpeed(0.2);
            ghost.setFrightenedStage(3);
        }

        if (ghost.getFrightenedStage() == 3 && ghost.isInGhostHouse()) {
            ghost.setIsFrightened(false);
            ghost.setEaten(false);
            ghost.setPrevCell(null);
            ghost.resetSpeed();
            ghost.setFrightenedStage(0);
        }
    }


    private void playClip(Clip clip) {
        if (!muteSound) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    private void playLoopClip(Clip clip) {
        if (!muteSound) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    private void stopClip(Clip clip) {
        if (!muteSound) {
            clip.stop();
        }
    }

    private void drawItems(Graphics2D g) {
        for (int i = 0; i < FIELD_HEIGHT; ++i) {
            for (int j = 0; j < FIELD_WIDTH; ++j) {
                Type type = field[i][j].getType();
                int screenX = (int) (8 + (j - VIRT_BORDERS / 2) * W_COEFF);
                int screenY = (int) (8 + i * H_COEFF);
                showDot(g, type, screenX, screenY, DOT, 2, Color.WHITE);
                showSuper(g, type, screenX, screenY, field[i][j]);
//                showWall(g, type, screenX, screenY, WALL, 6, Color.PINK);
//                showEmptyOrPortal(g, type, screenX, screenY);
            }
        }
    }

    private void showEmptyOrPortal(Graphics2D g, Type type, int screenX, int screenY) {
        if (type == EMPTY || type == L_PORTAL || type == R_PORTAL) {
            int r = 6;
            g.setColor(Color.GREEN);
            g.fillRect(screenX - r, screenY - r, r * 2, r * 2);
        }
    }

    private void showSuper(Graphics2D g, Type type, int screenX, int screenY, Cell cell) {
        if (type == SUPER) {

            double animType = cell.getAnimationType();
            double animStage = cell.getAnimationStage();
            int r = 5 + (int) animStage;

            g.setColor(Color.ORANGE);
            g.fillOval(screenX - r, screenY - r, r * 2, r * 2);

            if (animStage >= 5) {
                cell.setAnimationType(-0.4);
            } else if (animStage <= 0) {
                cell.setAnimationType(0.4);
            }
            cell.setAnimationStage(animStage + animType);


        }
    }

    private void showWall(Graphics2D g, Type type, int screenX, int screenY, Type wall, int i2, Color pink) {
        if (type == wall) {
            int r = i2;
            g.setColor(pink);
            g.fillRect(screenX - r, screenY - r, r * 2, r * 2);
        }
    }

    private void showDot(Graphics2D g, Type type, int screenX, int screenY, Type dot, int i2, Color white) {
        if (type == dot) {
            int r = i2;
            g.setColor(white);
            g.fillRect(screenX - r, screenY - r, r * 2, r * 2);
        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Does nothing
    }

    private void handleKeyPresses() {
        pacman.tryUp(keysPressed[KEY_UP]);
        pacman.tryDown(keysPressed[KEY_DOWN]);
        pacman.tryLeft(keysPressed[KEY_LEFT]);
        pacman.tryRight(keysPressed[KEY_RIGHT]);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == VK_UP) {
            keysPressed[KEY_UP] = true;
        }
        if (key == VK_DOWN) {
            keysPressed[KEY_DOWN] = true;
        }
        if (key == VK_LEFT) {
            keysPressed[KEY_LEFT] = true;
        }
        if (key == VK_RIGHT) {
            keysPressed[KEY_RIGHT] = true;
        }
        if (key == VK_R) {
            keysPressed[KEY_RESTART] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == VK_UP) {
            keysPressed[KEY_UP] = false;
        }
        if (key == VK_DOWN) {
            keysPressed[KEY_DOWN] = false;
        }
        if (key == VK_LEFT) {
            keysPressed[KEY_LEFT] = false;
        }
        if (key == VK_RIGHT) {
            keysPressed[KEY_RIGHT] = false;
        }
    }
}