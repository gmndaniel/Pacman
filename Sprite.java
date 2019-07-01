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
    protected Cell spriteCell;
    protected Cell prevCell = null;
    protected Cell startingCell;

    protected double spriteSpeed = 0;
    protected double spriteBaseSpeed = spriteSpeed;
    protected int spriteLevel;
    protected Pacman.Facing spriteFacing;

    protected BufferedImage[] dance;
    protected BufferedImage[] danceLeft;
    protected BufferedImage[] danceRight;
    protected BufferedImage[] danceUp;
    protected BufferedImage[] danceDown;
    protected BufferedImage[] danceBlue;
    protected BufferedImage[] spriteDeath;
    protected BufferedImage[] danceBlink;
    protected BufferedImage[] eatenLeft;
    protected BufferedImage[] eatenRight;
    protected BufferedImage[] eatenUp;
    protected BufferedImage[] eatenDown;

    protected Animation[] normalDanceAnimation;
    protected Animation[] blueAnimation;
    protected Animation[] blinkAnimation;
    protected Animation[] eatenAnimation;
    protected Animation[] staticDanceAnimation;
    protected Animation pacmanDeathAnimation;

    private Animation animation;
    protected boolean dancingInGhostHouse = false;
    private double ghostHouseDanceOffset = 0;
    private int ghostHouseDanceDir = 0;

    public abstract Cell move(boolean chase);

    protected Sprite(Cell cell, int spriteLevel) {
        spriteCell = cell;
        matrixRow = cell.getRow();
        matrixCol = cell.getCol();
        virtualRow = matrixRow;
        virtualCol = matrixCol;
        this.spriteLevel = spriteLevel;
    }

    public void setDancingInGhostHouse(boolean dancingInGhostHouse) {
        this.dancingInGhostHouse = dancingInGhostHouse;
    }

    public double getVirtualRow() {
        return virtualRow;
    }

    public double getVirtualCol() {
        return virtualCol;
    }

    protected void startDeathAnimation() {
        pacmanDeathAnimation = new Animation(spriteDeath, 5);
        animation = pacmanDeathAnimation;
        animation.playOnce();
    }

    protected void startAnimation(Animation animation) {
        this.animation = animation;
        animation.start();
    }

    public void draw(Graphics g) {
        animation.update();
        int screenY = (int) (8 + overrideRow() * H_COEFF - 32 / 2);
        int screenX = (int) (8 + (overrideCol() - VIRT_BORDERS / 2) * W_COEFF - 32 / 2);
        g.drawImage(animation.getSpriteFrame(), screenX, screenY, this);
    }

    protected double overrideRow() {
        double overridenRow = virtualRow;
        double danceRad = 0.65;
        double offsetStep = 0.05;
        if (dancingInGhostHouse && !standStill) {
            if (ghostHouseDanceOffset < -danceRad) {
                spriteFacing = Facing.DOWN;
                ghostHouseDanceDir = 0;
            } else if (ghostHouseDanceOffset > danceRad) {
                spriteFacing = Facing.UP;
                ghostHouseDanceDir = 1;
            }

            if (ghostHouseDanceDir == 0) {
                ghostHouseDanceOffset += offsetStep;
            } else {
                ghostHouseDanceOffset -= offsetStep;
            }
            overridenRow += ghostHouseDanceOffset;
        }
        return overridenRow;
    }

    protected double overrideCol() {
        double overridenCol = virtualCol;

        if (matrixRow == 14) {
            if (virtualCol == 13) {
                overridenCol = 12.8;
            } else if (virtualCol == 15) {
                overridenCol = 14.6;
            } else if (virtualCol == 16) {
                overridenCol = 16.5;
            }
        } else if (12 <= matrixRow && matrixRow <= 13) {
            if (14 <= virtualCol && virtualCol <= 15) {
                overridenCol = 14.5;
            }
        }
        return overridenCol;

    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        return false;
    }

    public Cell getCell() {
        return spriteCell;
    }

    protected void locateSpriteCell() {
        spriteCell = Pacman.field[matrixRow][matrixCol];
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

