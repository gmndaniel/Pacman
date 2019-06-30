package PacmanPack;

import java.awt.image.BufferedImage;

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

        loadDanceStances();
        loadDeathAnimation();
        loadSprite();

        spriteCell = cell;
        spriteFacing = Pacman.Facing.LEFT;
        spriteSpeed = 0.2;
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
                staticRight,
                staticRight,
                staticRight};
        staticDanceLeft = new BufferedImage[]{
                staticLeft,
                staticLeft,
                staticLeft};
        staticDanceUp = new BufferedImage[]{
                staticUp,
                staticUp,
                staticUp};
        staticDanceDown = new BufferedImage[]{
                staticDown,
                staticDown,
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
//        dance = danceLeft;
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
        if (spriteFacing == Pacman.Facing.LEFT) {
            setDance(spriteCell.getLeft(), danceLeft, staticDanceLeft);
            return spriteCell.getLeft();
        }
        if (spriteFacing == Pacman.Facing.RIGHT) {
            setDance(spriteCell.getRight(), danceRight, staticDanceRight);
            return spriteCell.getRight();
        }
        if (spriteFacing == Pacman.Facing.UP) {
            setDance(spriteCell.getUp(), danceUp, staticDanceUp);
            return spriteCell.getUp();
        }
        if (spriteFacing == Pacman.Facing.DOWN) {
            setDance(spriteCell.getDown(), danceDown, staticDanceDown);
            return spriteCell.getDown();
        }
        return spriteCell;
    }

    private void setDance(Cell nextCell, BufferedImage[] danceDir, BufferedImage[] staticDanceDir) {
        if (nextCell.getType() == Pacman.Type.DOT) {
            if (dance != danceDir) {
                dance = danceDir;
                loadSprite();
            }
        } else {
            if (dance != staticDanceDir) {
                dance = staticDanceDir;
                loadSprite();
            }
        }
    }


    public void tryUp(boolean keyPressed) {
        if (keyPressed) {
            if (isNextCellOK(spriteCell.getUp())) {
                spriteFacing = Pacman.Facing.UP;
            }
        }
    }

    public void tryDown(boolean keyPressed) {
        if (keyPressed) {
            if (isNextCellOK(spriteCell.getDown())) {
                spriteFacing = Pacman.Facing.DOWN;
            }
        }
    }

    private boolean isNextCellOK(Cell cell) {
        return cell.getType() != Pacman.Type.WALL &&
                cell.getType() != Pacman.Type.GHOST_HOUSE;
    }

    public void tryLeft(boolean keyPressed) {
        if (keyPressed) {
            if (isNextCellOK(spriteCell.getLeft())) {
                spriteFacing = Pacman.Facing.LEFT;
            }
        }
    }

    public void tryRight(boolean keyPressed) {
        if (keyPressed) {
            if (isNextCellOK(spriteCell.getRight())) {
                spriteFacing = Pacman.Facing.RIGHT;
            }
        }
    }

    public Pacman.Facing getFacing() {
        return spriteFacing;
    }
}
