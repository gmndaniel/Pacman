package PacmanPack;

import java.util.ArrayList;

public class Pathfinder {
    private static ArrayList<Cell> openSet = new ArrayList<Cell>();
    private static ArrayList<Cell> closedSet = new ArrayList<Cell>();
    private static ArrayList<Cell> neighbors = new ArrayList<>();

    static Cell findPath(Cell start, Cell prev, Cell end, int spriteLevel, boolean canMoveBack) {
        emptySets();

        if (prev != null && !canMoveBack) {
            prev.setG_cost(9999, spriteLevel);
        }
        openSet.add(start);
        start.setParent(null, spriteLevel);

        while (!openSet.isEmpty()) {
            int winner = 0;
            for (int i = 0; i < openSet.size(); ++i) {
                if (openSet.get(i).getF_cost(spriteLevel) < openSet.get(winner).getF_cost(spriteLevel)) {
                    winner = i;
                }
            }

            Cell current = openSet.get(winner);
            openSet.remove(current);
            closedSet.add(current);

            if (current == end) {
                openSet.removeAll(openSet);
            }

            ArrayList<Cell> neighbors = getCellNeighbors(current);
            for (Cell n : neighbors) {
                if (closedSet.contains(n)) {
                    continue;
                }
                int tempG_cost = n.getG_cost(spriteLevel) + 1;
                n.setG_cost(tempG_cost, spriteLevel);
                n.setParent(current, spriteLevel);
                n.setH_cost(calcHCost(n, end), spriteLevel);
                n.setF_cost(n.getG_cost(spriteLevel) + n.getH_cost(spriteLevel), spriteLevel);

                if (openSet.contains(n)) {
                    if (n.getG_cost(spriteLevel) > openSet.get(openSet.indexOf(n)).getG_cost(spriteLevel)) {
                        continue;
                    }
                }
                openSet.add(n);
            }
        }
        nullifyCostsClosedSetCells();
        return end;
    }

    private static void emptySets() {
        openSet.removeAll(openSet);
        closedSet.removeAll(closedSet);
        neighbors.removeAll(neighbors);
    }

    private static void nullifyCostsClosedSetCells() {

        for (Cell n : closedSet) {
            for (int i = 0; i < 5; ++i) {
                if (n.getType() != Pacman.Type.WALL) {
                    n.setF_cost(0, i);
                    n.setH_cost(0, i);
                    n.setG_cost(0, i);
                }
            }
        }
    }

    private static ArrayList<Cell> getCellNeighbors(Cell cell) {

        neighbors.removeAll(neighbors);

        if (cell.getUp() != null) {
            neighbors.add(cell.getUp());
        }
        if (cell.getDown() != null) {
            neighbors.add(cell.getDown());
        }
        if (cell.getLeft() != null) {
            neighbors.add(cell.getLeft());
        }
        if (cell.getRight() != null) {
            neighbors.add(cell.getRight());
        }

        return neighbors;
    }

    private static int calcHCost(Cell n, Cell end) {

        int[] to = {end.getRow(), end.getCol()};
        int[] L = {14, 0};
        int[] R = {14, 27};

        int[] from = {n.getRow(), n.getCol()};

        int hueRaw = calcRawHCost(from, to);
        int hueLPortal = calcRawHCost(from, L) + calcRawHCost(R, to);
        int hueRPortal = calcRawHCost(from, R) + calcRawHCost(L, to);

        return minHCost(hueRaw, hueLPortal, hueRPortal);
    }


    private static int calcRawHCost(int[] from, int[] to) {
        int dRow = Math.abs(from[0] - to[0]);
        int dCol = Math.abs(from[1] - to[1]);
        return dRow + dCol;
    }

    private static int minHCost(int hueRaw, int hueLPortal, int hueRPortal) {
        return Math.min(hueRaw, Math.min(hueLPortal, hueRPortal));
    }
}
