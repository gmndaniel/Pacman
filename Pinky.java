package PacmanPack;

public class Pinky extends Ghost {

    public Pinky(Cell cell, int spriteLevel) {
        super(cell, spriteLevel);
        spriteBaseSpeed = 0.1;
        spriteSpeed = spriteBaseSpeed;
        homeCell1 = Pacman.field[1][4];
        homeCell2 = Pacman.field[5][5];
        homeCell = homeCell1;
        ghostHouse = cell;
        danceStance = 5;
        loadNormalDanceStances();
        startAnimation(normalDanceAnimation[0]);
    }

    @Override
    protected Cell scatterStrategy() {
        return homeCell;
    }

    @Override
    protected Cell chaseStrategy() {
        return pinkyChaseStrategy();
    }

    private Cell pinkyChaseStrategy() {
        return cellsAhead(Pacman.pacman.getCell(), 6);
    }


}
