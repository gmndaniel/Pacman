package PacmanPack;

public class Blinky extends Ghost {

    public Blinky(Cell cell, int spriteLevel) {
        super(cell, spriteLevel);
        startingCell = cell;
        spriteBaseSpeed = 0.1;
        spriteSpeed = spriteBaseSpeed;
        homeCell1 = Pacman.field[1][24];
        homeCell2 = Pacman.field[5][24];
        homeCell = homeCell1;
        ghostHouse = Pacman.field[14][13];
        danceStance = 4;
        dancingInGhostHouse = false;
        loadNormalDanceStances();
        startAnimation(normalDanceAnimation[0]);
    }

    @Override
    protected Cell scatterStrategy() {
        Cell targetCell = homeCell;
        if (Pacman.dots <= 20) {
            targetCell = Pacman.pacman.getCell();
        }
        return targetCell;
    }

    @Override
    protected Cell chaseStrategy() {
        return blinkyChaseStrategy();
    }

    private Cell blinkyChaseStrategy() {
        return Pacman.pacman.getCell();
    }
}
