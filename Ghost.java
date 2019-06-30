package PacmanPack;

import java.awt.image.BufferedImage;

import static PacmanPack.Pacman.Type.GHOST_HOUSE;
import static PacmanPack.Pacman.pacmanIsDying;
import static PacmanPack.Pacman.standStill;
import static PacmanPack.SpriteMap.getSprite;

public abstract class Ghost extends Sprite {

    protected Cell homeCell1;
    protected Cell homeCell2;
    protected Cell homeCell;
    protected Cell ghostHouse;
    protected Cell prevCell = null;
    protected int frightenedStage = 0;
    protected boolean isFrightened = false;
    protected int danceStance;
    protected boolean isEaten = false;
    protected Cell randomCell = null;

    protected abstract Cell chaseStrategy();
    protected abstract Cell scatterStrategy();

    @Override
    public Cell move(boolean chase) {
        locateSpriteCell();
        checkIfHomeCell();

//        if (spriteCell.getType() == GHOST_HOUSE) {
//            System.out.println(spriteCell);
//        }

        boolean canMoveBack = false;
        Cell targetCell = randomCell;
        Cell nextCell = null;

        if (standStill || pacmanIsDying) {
            targetCell = spriteCell;
        } else if (isEaten) {
            targetCell = ghostHouse;
            canMoveBack = true;
        } else if (isFrightened) {
            if (spriteCell != prevCell || targetCell == null) {
                targetCell = moveToRandom();
                randomCell = targetCell;
                canMoveBack = true;
            }
        } else if (!chase) {
            targetCell = scatterStrategy();
        } else {
            targetCell = chaseStrategy();
        }

        if (spriteCell != prevCell) {
            Pathfinder.findPath(spriteCell, prevCell, targetCell, spriteLevel, canMoveBack);
            prevCell = spriteCell;
        }
        nextCell = findNextCell(targetCell, spriteLevel);
        setDance(nextCell);
        moveToNextCell(nextCell);
        return targetCell;
    }


    public Ghost(Cell cell, int spriteLevel) {
        super(cell, spriteLevel);
    }


    protected Cell findNextCell(Cell targetCell, int l) {

        while (targetCell.getParent(l) != null && targetCell.getParent(l).getParent(l) != null) {
            targetCell = targetCell.getParent(l);
        }
        return targetCell;
    }

    protected void checkIfHomeCell() {
        if (spriteCell == homeCell1) {
            homeCell = homeCell2;
        } else if (spriteCell == homeCell2) {
            homeCell = homeCell1;
        }
    }

    protected Cell moveToRandom() {
        int rand4 = (int) (Math.random() * ((3) + 1));
        int rand3 = (int) (Math.random() * ((2) + 1));
        int rand2 = (int) (Math.random() * ((1) + 1));
        if (spriteCell.getIntersectionType() == 1) {
            if (rand4 == 0) return spriteCell.getLeft();
            if (rand4 == 1) return spriteCell.getRight();
            if (rand4 == 2) return spriteCell.getUp();
            if (rand4 == 3) return spriteCell.getDown();
        } else if (spriteCell.getIntersectionType() == 2) {
            if (rand3 == 0) return spriteCell.getRight();
            if (rand3 == 1) return spriteCell.getUp();
            if (rand3 == 2) return spriteCell.getDown();
        } else if (spriteCell.getIntersectionType() == 3) {
            if (rand3 == 0) return spriteCell.getLeft();
            if (rand3 == 1) return spriteCell.getRight();
            if (rand3 == 2) return spriteCell.getDown();
        } else if (spriteCell.getIntersectionType() == 4) {
            if (rand3 == 0) return spriteCell.getUp();
            if (rand3 == 1) return spriteCell.getLeft();
            if (rand3 == 2) return spriteCell.getDown();
        } else if (spriteCell.getIntersectionType() == 5) {
            if (rand3 == 0) return spriteCell.getUp();
            if (rand3 == 1) return spriteCell.getLeft();
            if (rand3 == 2) return spriteCell.getRight();
        } else if (spriteCell.getIntersectionType() == -1) {
            if (rand2 == 0) return spriteCell.getDown();
            if (rand2 == 1) return spriteCell.getRight();
        } else if (spriteCell.getIntersectionType() == -2) {
            if (rand2 == 0) return spriteCell.getDown();
            if (rand2 == 1) return spriteCell.getLeft();
        } else if (spriteCell.getIntersectionType() == -3) {
            if (rand2 == 0) return spriteCell.getUp();
            if (rand2 == 1) return spriteCell.getLeft();
        } else if (spriteCell.getIntersectionType() == -4) {
            if (rand2 == 0) return spriteCell.getUp();
            if (rand2 == 1) return spriteCell.getRight();
        }

        if (spriteFacing == Pacman.Facing.UP) {
            return spriteCell.getUp();
        } else if (spriteFacing == Pacman.Facing.DOWN) {
            return spriteCell.getDown();
        } else if (spriteFacing == Pacman.Facing.LEFT) {
            return spriteCell.getLeft();
        } //else if (spriteFacing == Pacman.Facing.RIGHT)
        return spriteCell.getRight();
    }


    public int getFrightenedStage() {
        return frightenedStage;
    }

    public void setFrightenedStage(int frightenedStage) {
        this.frightenedStage = frightenedStage;
//        System.out.println("stage " + this.frightenedStage);
    }

    protected Cell cellsAhead(Cell targetCell, int ahead) {

        Pacman.Facing facing = Pacman.pacman.getFacing();

        if (facing == Pacman.Facing.LEFT) {
            while (notHitWall(ahead, targetCell.getLeft())) {
                targetCell = targetCell.getLeft();
                if (targetCell == spriteCell) {
                    return Pacman.pacman.getCell();
                }
                --ahead;
            }
        } else if (facing == Pacman.Facing.RIGHT) {
            while (notHitWall(ahead, targetCell.getRight())) {
                targetCell = targetCell.getRight();
                if (targetCell == spriteCell) {
                    return Pacman.pacman.getCell();
                }
                --ahead;
            }
        } else if (facing == Pacman.Facing.UP) {
            while (notHitWall(ahead, targetCell.getUp())) {
                targetCell = targetCell.getUp();
                if (targetCell == spriteCell) {
                    return Pacman.pacman.getCell();
                }
                --ahead;
            }
        } else if (facing == Pacman.Facing.DOWN) {
            while (notHitWall(ahead, targetCell.getDown())) {
                targetCell = targetCell.getDown();
                if (targetCell == spriteCell) {
                    return Pacman.pacman.getCell();
                }
                --ahead;
            }
        }
        return targetCell;
    }


    private boolean notHitWall(int ahead, Cell cell) {
        return (cell.getType() != Pacman.Type.WALL) && (ahead > 0);
    }

    protected void loadDanceStances(int y) {
        danceRight = new BufferedImage[]{
                getSprite(0, y),
                getSprite(1, y)};
        danceLeft = new BufferedImage[]{
                getSprite(2, y),
                getSprite(3, y)};
        danceUp = new BufferedImage[]{
                getSprite(4, y),
                getSprite(5, y)};
        danceDown = new BufferedImage[]{
                getSprite(6, y),
                getSprite(7, y)};
        dance = danceLeft;
    }

    protected void loadFrightenedBlue() {

        BufferedImage blueFrightened1 = getSprite(8, 4);
        BufferedImage blueFrightened2 = getSprite(9, 4);
        danceRight = new BufferedImage[]{
                blueFrightened1,
                blueFrightened2};
        danceLeft = new BufferedImage[]{
                blueFrightened1,
                blueFrightened2};
        danceUp = new BufferedImage[]{
                blueFrightened1,
                blueFrightened2};
        danceDown = new BufferedImage[]{
                blueFrightened1,
                blueFrightened2};
    }

    protected void loadEatenEyes() {
        danceRight = new BufferedImage[]{
                getSprite(8, 5),
                getSprite(8, 5)};
        danceLeft = new BufferedImage[]{
                getSprite(9, 5),
                getSprite(9, 5)};
        danceUp = new BufferedImage[]{
                getSprite(10, 5),
                getSprite(10, 5)};
        danceDown = new BufferedImage[]{
                getSprite(11, 5),
                getSprite(11, 5)};
        isEaten = true;
    }

    protected void setDance(Cell nextCell) {
        if (spriteCell.getRight() == nextCell) {
            if (dance != danceRight) {
                spriteFacing = Pacman.Facing.RIGHT;
                dance = danceRight;
                loadSprite();
            }
        } else if (spriteCell.getLeft() == nextCell) {
            if (dance != danceLeft) {
                spriteFacing = Pacman.Facing.LEFT;
                dance = danceLeft;
                loadSprite();
            }
        } else if (spriteCell.getUp() == nextCell) {
            if (dance != danceUp) {
                spriteFacing = Pacman.Facing.UP;
                dance = danceUp;
                loadSprite();
            }
        } else if (spriteCell.getDown() == nextCell) {
            if (dance != danceDown) {
                spriteFacing = Pacman.Facing.DOWN;
                dance = danceDown;
                loadSprite();
            }
        }
    }

    public void setIsFrightened(boolean isFrightened) {
        this.isFrightened = isFrightened;
        prevCell = null;
    }

    public void loadNormalDanceStances() {
        isEaten = false;
        loadDanceStances(danceStance);
    }

    protected void setSpeed(double speed) {
        spriteSpeed = speed;
    }

    public boolean isInGhostHouse() {
        return spriteCell == ghostHouse;
    }

    public void setPrevCell(Cell prevCell) {
        this.prevCell = prevCell;
    }
}
