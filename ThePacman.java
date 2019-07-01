package PacmanPack;

import java.awt.image.BufferedImage;

import static PacmanPack.Pacman.Facing.*;
import static PacmanPack.Pacman.pacmanIsDying;
import static PacmanPack.Pacman.standStill;
import static PacmanPack.SpriteMap.*;

public class ThePacman extends Sprite {

    private BufferedImage[] staticDanceRight;
    private BufferedImage[] staticDanceLeft;
    private BufferedImage[] staticDanceUp;
    private BufferedImage[] staticDanceDown;

    public ThePacman(Cell cell, int spriteLevel) {
        super(cell, spriteLevel);
        startingCell = cell;
        loadDanceStances();
        loadDeathAnimation();

        normalDanceAnimation = new Animation[]{
                new Animation(danceLeft, 2),
                new Animation(danceRight, 2),
                new Animation(danceUp, 2),
                new Animation(danceDown, 2)};
        startAnimation(normalDanceAnimation[0]);

        staticDanceAnimation = new Animation[]{
                new Animation(staticDanceLeft, 2),
                new Animation(staticDanceRight, 2),
                new Animation(staticDanceUp, 2),
                new Animation(staticDanceDown, 2)};
        startAnimation(staticDanceAnimation[0]);

        spriteCell = startingCell;
        spriteFacing = LEFT;
        prevCell = startingCell.getRight();
        spriteSpeed = 0.125;
    }

    private void loadDeathAnimation() {

        spriteDeath = new BufferedImage[]{
                getSprite(2, 0),
                getSprite(3, 0),
                getSprite(4, 0),
                getSprite(5, 0),
                getSprite(6, 0),
                getSprite(7, 0),
                getSprite(8, 0),
                getSprite(9, 0),
                getSprite(10, 0),
                getSprite(11, 0),
                getSprite(12, 0),
                getSprite(13, 0),
                getSprite(13, 1),
        };
    }

    private void loadDanceStances() {
        BufferedImage staticRight = getSprite(1, 0);
        BufferedImage staticLeft = getSprite(1, 1);
        BufferedImage staticUp = getSprite(1, 2);
        BufferedImage staticDown = getSprite(1, 3);
        BufferedImage closedPacman = getSprite(2, 0);

        staticDanceRight = new BufferedImage[]{
                staticRight};
        staticDanceLeft = new BufferedImage[]{
                staticLeft};
        staticDanceUp = new BufferedImage[]{
                staticUp};
        staticDanceDown = new BufferedImage[]{
                staticDown};
        danceRight = new BufferedImage[]{
                getSprite(0, 0),
                getSprite(1, 0),
                closedPacman};
        danceLeft = new BufferedImage[]{
                getSprite(0, 1),
                getSprite(1, 1),
                closedPacman};
        danceUp = new BufferedImage[]{
                getSprite(0, 2),
                getSprite(1, 2),
                closedPacman};
        danceDown = new BufferedImage[]{
                getSprite(0, 3),
                getSprite(1, 3),
                closedPacman};

        dance = staticDanceLeft;
    }

    public Cell move(boolean chase) {
        if (!pacmanIsDying && !standStill) {
            locateSpriteCell();
            Cell nextCell = calcNextCell();
            if (isNextCellOK(nextCell)) {
                moveToNextCell(nextCell);
            }
        }
        return null;
    }

    private Cell calcNextCell() {
        if (spriteFacing == LEFT) {
            setDance(spriteCell.getLeft(), 0);
            return spriteCell.getLeft();
        }
        if (spriteFacing == Pacman.Facing.RIGHT) {
            setDance(spriteCell.getRight(), 1);
            return spriteCell.getRight();
        }
        if (spriteFacing == Pacman.Facing.UP) {
            setDance(spriteCell.getUp(), 2);
            return spriteCell.getUp();
        }
        if (spriteFacing == Pacman.Facing.DOWN) {
            setDance(spriteCell.getDown(), 3);
            return spriteCell.getDown();
        }
        return spriteCell;
    }

    private void setDance(Cell nextCell, int direction) {
        if (nextCell.getType() == Pacman.Type.DOT) {
            startAnimation(normalDanceAnimation[direction]);
        } else {
            startAnimation(staticDanceAnimation[direction]);
        }
    }

    public void tryLeft(boolean keyPressed) {
        if (keyPressed) {
            if (isNextCellOK(spriteCell.getLeft()) && prevCell.getLeft() != spriteCell.getLeft()) {
                spriteFacing = LEFT;
                prevCell = spriteCell;
            }
        }
    }

    public void tryRight(boolean keyPressed) {
        if (keyPressed) {
            if (isNextCellOK(spriteCell.getRight()) && prevCell.getRight() != spriteCell.getRight()) {
                spriteFacing = RIGHT;
                prevCell = spriteCell;
            }
        }
    }

    public void tryUp(boolean keyPressed) {
        if (keyPressed) {
            if (isNextCellOK(spriteCell.getUp()) && prevCell.getUp() != spriteCell.getUp()) {
                spriteFacing = UP;
                prevCell = spriteCell;
            }
        }
    }

    public void tryDown(boolean keyPressed) {
        if (keyPressed) {
            if (isNextCellOK(spriteCell.getDown()) && prevCell.getDown() != spriteCell.getDown()) {
                spriteFacing = DOWN;
                prevCell = spriteCell;
            }
        }
    }

    private boolean isNextCellOK(Cell cell) {

        return cell.getType() != Pacman.Type.WALL &&
                cell.getType() != Pacman.Type.GHOST_HOUSE;
    }

    public Pacman.Facing getFacing() {
        return spriteFacing;
    }
}
