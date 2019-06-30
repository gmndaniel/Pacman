package PacmanPack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import static PacmanPack.Pacman.*;

public abstract class Sprite implements ImageObserver {
    protected int matrixRow;
    protected int matrixCol;
    protected double virtualRow;
    protected double virtualCol;
    private BufferedImage spriteImg;
    protected Cell spriteCell;

    protected double spriteSpeed = 0;
    protected double spriteBaseSpeed = spriteSpeed;
    protected int spriteLevel;
    protected Pacman.Facing spriteFacing;

    protected BufferedImage[] dance;
    protected BufferedImage[] danceLeft;
    protected BufferedImage[] danceRight;
    protected BufferedImage[] danceUp;
    protected BufferedImage[] danceDown;
    protected BufferedImage[] spriteDeath;
    private Animation dancing;
    private Animation deathAnimation;
    private Animation animation;
    protected boolean isInGhostHouse = true;

    public abstract Cell move(boolean chase);

    public Sprite(Cell cell, int spriteLevel) {
        matrixRow = cell.getRow();
        matrixCol = cell.getCol();
        virtualRow = matrixRow;
        virtualCol = matrixCol;
        this.spriteLevel = spriteLevel;
    }

    public double getVirtualRow() {
        return virtualRow;
    }

    public double getVirtualCol() {
        return virtualCol;
    }

    protected void startDeathAnimation() {
        deathAnimation = new Animation(spriteDeath, 5);
        animation = deathAnimation;
        animation.playOnce();
    }

    protected void loadSprite() {
        dancing = new Animation(dance, 2);
        animation = dancing;
        animation.start();
    }

    public void draw(Graphics g) {
        animation.update();
        int screenX = (int) (8 + (virtualCol - VIRT_BORDERS / 2) * W_COEFF - 32 / 2);
        int screenY = (int) (8 + virtualRow * H_COEFF - 32 / 2);
        g.drawImage(animation.getSprite(), screenX, screenY, this);
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return false;
    }

    public Cell getCell() {
        return spriteCell;
    }

    protected void locateSpriteCell() {
        if (isInGhostHouse) {
            spriteCell = Pacman.ghostHouseMid;
        } else {
            spriteCell = Pacman.field[matrixRow][matrixCol];
        }
    }

    protected void moveToNextCell(Cell nextCell) {
        if (nextCell.getType() == Type.WALL) {
            nextCell = spriteCell;
        }
        double epsilon = 0.0001;
        transferThroughLimbo();
        moveVirtualCol(nextCell, epsilon);
        moveVirtualRow(nextCell, epsilon);
    }

    private void transferThroughLimbo() {
        if (spriteCell.getType() == Type.R_PORTAL && spriteFacing == Facing.RIGHT) {
            virtualCol = 0;
        } else if (spriteCell.getType() == Type.L_PORTAL && spriteFacing == Facing.LEFT) {
            virtualCol = R_PORTAL_COL;
        }
    }

    private void moveVirtualRow(Cell nextCell, double epsilon) {
        if (Math.abs(nextCell.getRow() - virtualRow) < epsilon) {
            matrixRow = (int) Math.round(virtualRow);
        } else if (virtualRow < nextCell.getRow()) {
            virtualRow += spriteSpeed;
        } else if (virtualRow > nextCell.getRow()) {
            virtualRow -= spriteSpeed;
        }
    }

    private void moveVirtualCol(Cell nextCell, double epsilon) {
        if (Math.abs(nextCell.getCol() - virtualCol) < epsilon) {
            matrixCol = (int) Math.round(virtualCol);
        } else if (virtualCol < nextCell.getCol()) {
            virtualCol += spriteSpeed;
        } else if (virtualCol > nextCell.getCol()) {
            virtualCol -= spriteSpeed;
        }
    }

    public void resetSpeed() {
        spriteSpeed = spriteBaseSpeed;
    }

    public void snapToCell() {
        virtualRow = matrixRow;
        virtualCol = matrixCol;
    }
}

