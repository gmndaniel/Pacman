package PacmanPack;

public class Inky extends Ghost {

    public Inky(Cell cell, int spriteLevel) {
        super(cell, spriteLevel);
        spriteBaseSpeed = 0.1;
        spriteSpeed = spriteBaseSpeed;
        homeCell1 = Pacman.field[29][22];
        homeCell2 = Pacman.field[25][22];
        homeCell = homeCell1;
        ghostHouse = Pacman.field[14][15];
        danceStance = 6;
        loadNormalDanceStances();
        loadSprite();
    }

    @Override
    protected Cell scatterStrategy() {
        return homeCell;
    }

    @Override
    protected Cell chaseStrategy() {
        return inkyChaseStrategy();
    }

    private Cell inkyChaseStrategy() {
        Cell targetCell = Pacman.pacman.getCell();

        targetCell = cellsAhead(targetCell, 2);

        Cell blinkyCell = Pacman.blinky.getCell();

        int tRow = targetCell.getRow();
        int tCol = targetCell.getCol();
        int dRow = blinkyCell.getRow() - tRow;
        int dCol = blinkyCell.getCol() - tCol;

        tRow = tRow + 2 * dRow;
        tCol = tCol + 2 * dCol;

        if (tRow < 0) {
            tRow = 0;
        } else if (tRow > 30) {
            tRow = 30;
        }
        if (tCol < 0) {
            tCol = Math.abs(28 + tCol);
        } else if (tCol > 27) {
            tCol = Math.abs(tCol % 27);
        }

        targetCell = Pacman.field[tRow][tCol];
        return targetCell;
    }
}
