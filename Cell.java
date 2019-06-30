package PacmanPack;

import static PacmanPack.Pacman.R_PORTAL_COL;
import static PacmanPack.Pacman.Type.GHOST_HOUSE;
import static PacmanPack.Pacman.Type.WALL;

public class Cell {
    private int row;
    private int col;
    private double virtualRow;
    private double virtualCol;

    private static final int TOT_SPRITES = 5;

    private Cell up = null;
    private Cell down = null;
    private Cell left = null;
    private Cell right = null;
    private Cell[] parent = new Cell[TOT_SPRITES];
    private int[] f_cost = new int[TOT_SPRITES];
    private int[] h_cost = new int[TOT_SPRITES];
    private int[] g_cost = new int[TOT_SPRITES];
    private double animationStage = 0;
    private double animationType = 0.2;
    Pacman.Type type;
    private int intersectionType;

    public Cell(double virtualRow, double virtualCol) {
        this.virtualRow = virtualRow;
        this.virtualCol = virtualCol;
        this.type = GHOST_HOUSE;

        nullifyCostsAndParent();
    }

    Cell(int row, int col, Pacman.Type type) {

        this.row = row;
        this.col = col;
        this.type = type;

        nullifyCostsAndParent();

        if (type == Pacman.Type.WALL) {
            for (int i = 0; i < TOT_SPRITES; ++i) {
                g_cost[i] = 9999;
            }
        }
    }

    public int getIntersectionType() {
        return intersectionType;
    }

    private void nullifyCostsAndParent() {
        for (int i = 0; i < TOT_SPRITES; ++i) {
            f_cost[i] = 0;
            h_cost[i] = 0;
            g_cost[i] = 0;
            parent[i] = null;
        }
    }

//    public Cell(Cell ghostHouseLeft, Cell right) {
//        row = 14;
//        col = -1;
//        this.ghostHouseLeft = ghostHouseLeft;
//        this.right = right;
//        type = LIMBO;
//        nullifyCostsAndParent();
//    }

    public double getAnimationType() {
        return animationType;
    }

    public void setAnimationType(double animationType) {
        this.animationType = animationType;
    }


    public double getAnimationStage() {
        return animationStage;
    }

    public void setAnimationStage(double animationStage) {
        this.animationStage = animationStage;
    }

    public int getH_cost(int l) {
        return h_cost[l];
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setH_cost(int h_cost, int l) {
        this.h_cost[l] = h_cost;
    }


    public int getG_cost(int l) {
        return g_cost[l];
    }

    public void setG_cost(int g_cost, int l) {
        this.g_cost[l] = g_cost;
    }


    Pacman.Type getType() {
        return type;
    }


    public void setF_cost(int f_cost, int l) {
        this.f_cost[l] = f_cost;
    }

    int getF_cost(int l) {
        return f_cost[l];
    }

    public void setGhostHouseNeighbors() {
        if (virtualRow == 11 && virtualCol == 14.5) {
            up = null;
            down = Pacman.ghostHouseMid;
            left = Pacman.field[11][14];
            right = Pacman.field[11][15];
            Pacman.field[11][15].left = this;
            Pacman.field[11][14].right = this;
        } else if (virtualRow == 14 && virtualCol == 14.5) {
            up = Pacman.ghostHouseDoorMat;
            down = null;
            left = Pacman.ghostHouseLeft;
            right = Pacman.ghostHouseRight;
        } else if (virtualRow == 14 && virtualCol == 13.5) {
            up = null;
            down = null;
            left = null;
            right = Pacman.ghostHouseMid;
        } else if (virtualRow == 14 && virtualCol == 15.5) {
            up = null;
            down = null;
            left = Pacman.ghostHouseLeft;
            right = null;
        }
    }

    public void setCellNeighbors() {
        int width = Pacman.FIELD_WIDTH;
        int height = Pacman.FIELD_HEIGHT;

        if (row > 0) {
            up = Pacman.field[row - 1][col];
        }
        if (row < height - 1) {
            down = Pacman.field[row + 1][col];
        }
        if (col > 0) {
            left = Pacman.field[row][col - 1];
        }
        if (col < width - 1) {
            right = Pacman.field[row][col + 1];
        }
        if (row == 14 && col == 0) {
            left = Pacman.field[row][R_PORTAL_COL];
        }
        if (row == 14 && col == R_PORTAL_COL) {
            right = Pacman.field[row][0];
        }

        boolean leftWall = (left != null && left.getType() == WALL);
        boolean rightWall = (right != null && right.getType() == WALL);
        boolean upWall = (up != null && up.getType() == WALL);
        boolean downWall = (down != null && down.getType() == WALL);

        intersectionType = 0;
        if (!leftWall && !rightWall && !upWall && !downWall) {
            intersectionType = 1;
        } else if (leftWall && !rightWall && !upWall && !downWall) {
            intersectionType = 2;
        } else if (!leftWall && !rightWall && upWall && !downWall) {
            intersectionType = 3;
        } else if (!leftWall && rightWall && !upWall && !downWall) {
            intersectionType = 4;
        } else if (!leftWall && !rightWall && !upWall && downWall) {
            intersectionType = 5;
        } else if (leftWall && !rightWall && upWall && !downWall) {
            intersectionType = -1;
        } else if (!leftWall && rightWall && upWall && !downWall) {
            intersectionType = -2;
        } else if (leftWall && !rightWall && !upWall && downWall) {
            intersectionType = -3;
        } else if (!leftWall && rightWall && !upWall && downWall) {
            intersectionType = -4;
        }
    }


    Cell getUp() {
        return up;
    }

    Cell getDown() {
        return down;
    }

    Cell getLeft() {
        return left;
    }

    Cell getRight() {
        return right;
    }

    Cell getParent(int l) {
        return parent[l];
    }

    void setParent(Cell parent, int l) {
        this.parent[l] = parent;
    }

    public void setType(Pacman.Type type) {
        this.type = type;
    }
}
