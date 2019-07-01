package PacmanPack;

import java.awt.image.BufferedImage;

import static PacmanPack.Pacman.Type.WALL;
import static PacmanPack.Pacman.pacmanIsDying;
import static PacmanPack.Pacman.standStill;
import static PacmanPack.SpriteMap.getSprite;

public abstract class Ghost extends Sprite {

    protected Cell homeCell1;
    protected Cell homeCell2;
    protected Cell homeCell;
    protected Cell ghostHouse;
    protected int frightenedStage = 0;
    protected boolean isFrightened = false;
    protected int danceStance;
    protected boolean isEaten = false;
    protected Cell randomCell = null;
    protected boolean isBlinking;

    protected abstract Cell chaseStrategy();

    protected abstract Cell scatterStrategy();

    public void setEaten(boolean eaten) {
        isEaten = eaten;
    }

    @Override
    public Cell move(boolean chase) {
        locateSpriteCell();
        checkIfHomeCell();

        boolean canMoveBack = false;
        Cell targetCell = randomCell;
        Cell nextCell = null;

        if (dancingInGhostHouse && isFrightened) {
            startFrightenedAnimation();
            targetCell = spriteCell;
        } else if (dancingInGhostHouse) {
            if (spriteFacing == Pacman.Facing.UP) {
                startAnimation(normalDanceAnimation[2]);
            } else if (spriteFacing == Pacman.Facing.DOWN) {
                startAnimation(normalDanceAnimation[3]);
            }
            targetCell = spriteCell;
        } else if (standStill || pacmanIsDying) {
            targetCell = spriteCell;
        } else if (isEaten) {
            targetCell = ghostHouse;
            canMoveBack = true;
        } else if (isFrightened) {
            if (spriteCell != prevCell || targetCell == null) {
                targetCell = moveToRandom();
                randomCell = targetCell;
                canMoveBack = false;
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

    protected Cell moveToRandom() {
        Cell nextCell = spriteCell;
        do {
            int rand4 = (int) (Math.random() * ((3) + 1));
            if (rand4 == 0) nextCell = spriteCell.getLeft();
            if (rand4 == 1) nextCell = spriteCell.getRight();
            if (rand4 == 2) nextCell = spriteCell.getUp();
            if (rand4 == 3) nextCell = spriteCell.getDown();
        } while (nextCell.getType() == WALL && nextCell != prevCell);
        return nextCell;
    }

    protected void startFrightenedAnimation() {
        setNormalOrEatenAnimation(0);
    }

    public Ghost(Cell cell, int spriteLevel) {
        super(cell, spriteLevel);
        loadFrightenedBlue();
        loadFrightenedBlinking();
        loadEatenEyes();
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

    public void setBlinking(boolean blinking) {
        isBlinking = blinking;
    }

    public int getFrightenedStage() {
        return frightenedStage;
    }

    public void setFrightenedStage(int frightenedStage) {
        this.frightenedStage = frightenedStage;
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
        return (cell.getType() != WALL) && (ahead > 0);
    }


    protected void setDance(Cell nextCell) {
        if (isBlinking) {
            startAnimation(blinkAnimation[0]);
        } else if (isFrightened && !isEaten) {
            startAnimation(blueAnimation[0]);
        } else if (spriteCell.getLeft() == nextCell) {
            spriteFacing = Pacman.Facing.LEFT;
            setNormalOrEatenAnimation(0);
        } else if (spriteCell.getRight() == nextCell) {
            spriteFacing = Pacman.Facing.RIGHT;
            setNormalOrEatenAnimation(1);
        } else if (spriteCell.getUp() == nextCell) {
            spriteFacing = Pacman.Facing.UP;
            setNormalOrEatenAnimation(2);
        } else if (spriteCell.getDown() == nextCell) {
            spriteFacing = Pacman.Facing.DOWN;
            setNormalOrEatenAnimation(3);
        }
    }

    private void setNormalOrEatenAnimation(int i) {
        if (isEaten) {
            startAnimation(eatenAnimation[i]);
        } else {
            startAnimation(normalDanceAnimation[i]);
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


    protected void loadDanceStances(int y) {
        danceRight = new BufferedImage[]{
                getSprite(0, y),
                getSprite(1, y),
        };
        danceLeft = new BufferedImage[]{
                getSprite(2, y),
                getSprite(3, y),
        };
        danceUp = new BufferedImage[]{
                getSprite(4, y),
                getSprite(5, y),
        };
        danceDown = new BufferedImage[]{
                getSprite(6, y),
                getSprite(7, y),
        };
        normalDanceAnimation = new Animation[]{
                new Animation(danceLeft, 2),
                new Animation(danceRight, 2),
                new Animation(danceUp, 2),
                new Animation(danceDown, 2)};
    }

    protected void loadFrightenedBlue() {
        danceBlue = new BufferedImage[]{
                getSprite(8, 4),
                getSprite(9, 4),
        };
        blueAnimation = new Animation[]{
                new Animation(danceBlue, 2),
        };
    }

    protected void loadFrightenedBlinking() {
        BufferedImage blue1 = getSprite(8, 4);
        BufferedImage blue2 = getSprite(9, 4);
        BufferedImage white1 = getSprite(10, 4);
        BufferedImage white2 = getSprite(11, 4);

        danceBlink = new BufferedImage[]{
                blue1,
                white1,
                blue2,
                white2,
        };
        blinkAnimation = new Animation[]{
                new Animation(danceBlink, 4),
        };
    }

    protected void loadEatenEyes() {
        eatenRight = new BufferedImage[]{
                getSprite(8, 5)};
        eatenLeft = new BufferedImage[]{
                getSprite(9, 5)};
        eatenUp = new BufferedImage[]{
                getSprite(10, 5)};
        eatenDown = new BufferedImage[]{
                getSprite(11, 5)};

        eatenAnimation = new Animation[]{
                new Animation(eatenLeft, 4),
                new Animation(eatenRight, 4),
                new Animation(eatenUp, 4),
                new Animation(eatenDown, 4),
        };
    }
}
