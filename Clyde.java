package PacmanPack;

public class Clyde extends Ghost {

    public Clyde(Cell cell, int spriteLevel) {
        super(cell, spriteLevel);
        spriteBaseSpeed = 0.1;
        spriteSpeed = spriteBaseSpeed;
        homeCell1 = Pacman.field[29][8];
        homeCell2 = Pacman.field[25][7];
        homeCell = homeCell1;
        ghostHouse = Pacman.field[14][16];
        danceStance = 7;
        loadNormalDanceStances();
        loadSprite();
    }

    @Override
    protected Cell scatterStrategy() {
        return homeCell;
    }

    @Override
    protected Cell chaseStrategy() {
        return clydeChaseStrategy();
    }

    private Cell clydeChaseStrategy() {
        Cell targetCell;
        targetCell = Pacman.pacman.getCell();
        if (distToPacman() <= 8.0) {
            targetCell = homeCell;
        }
        return targetCell;
    }

    private double distToPacman() {
        int dCol = Math.abs(Pacman.pacman.getCell().getCol() - spriteCell.getCol());
        int dRow = Math.abs(Pacman.pacman.getCell().getRow() - spriteCell.getRow());

        return Math.hypot(dRow, dCol);
    }
}
