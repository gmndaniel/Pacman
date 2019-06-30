package PacmanPack;

public class Blinky extends Ghost {

    public Blinky(Cell cell, int spriteLevel) {
        super(cell, spriteLevel);
        spriteBaseSpeed = 0.1;
        spriteSpeed = spriteBaseSpeed;
        homeCell1 = Pacman.field[1][24];
        homeCell2 = Pacman.field[5][24];
        homeCell = homeCell1;
        ghostHouse = Pacman.ghostHouseLeft;
        danceStance = 4;
        loadNormalDanceStances();
        loadSprite();
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
