package com.example;

import java.util.*;

public class GameLogic {

    private final Board board;
    private Board previousBoard; // for KO

    public GameLogic(Board board) {
        this.board = board;
        this.previousBoard = null;
    }

    public MoveResult tryMove(Move move) {
        int x = move.getX();
        int y = move.getY();
        StoneColor color = move.getState();
        int size = board.getSize();

        if (!board.inBounds(x, y))
            return MoveResult.illegal(MoveError.OUT_OF_BOUNDS);

        if (board.getInterSec(x, y) != StoneColor.EMPTY_STONE)
            return MoveResult.illegal(MoveError.FIELD_OCCUPIED);

        Board snapshot = board.copy();
        board.setInterSec(x, y, color);

        List<Point> captured = new ArrayList<>();

        //Capture opponent groups adjacent to the move
        for (Point p : neighbors(x, y)) {
            if (board.getInterSec(p.x(), p.y()) == opposite(color)) {
                Set<Point> group = getGroup(p);
                if (countLiberties(group) == 0)
                    captureGroup(group, captured);
            }
        }

        //Suicide check
        Set<Point> myGroup = getGroup(new Point(x, y));
        if (countLiberties(myGroup) == 0 && captured.isEmpty()) {
            board.restoreFrom(snapshot);
            return MoveResult.illegal(MoveError.SUICIDE);
        }

        //KO rule
        if (previousBoard != null && board.equals(previousBoard)) {
            board.restoreFrom(snapshot);
            return MoveResult(MoveError.KO);
        }

        previousBoard = snapshot;
        return MoveResult.legal(captured);
    }

    /* ===================== GROUP / LIBERTIES ===================== */

    private Set<Point> getGroup(Point start) {
        StoneColor color = board.getInterSec(start.x(), start.y());
        Set<Point> group = new HashSet<>();
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];
        dfsGroup(start, color, visited, group);
        return group;
    }

    private void dfsGroup(Point p, StoneColor color, boolean[][] visited, Set<Point> group) {
        if (!board.inBounds(p.x(), p.y()))
            return;
        if (visited[p.x() - 1][p.y() - 1])
            return;
        if (board.getInterSec(p.x(), p.y()) != color)
            return;

        visited[p.x() - 1][p.y() - 1] = true;
        group.add(p);

        for (Point n : neighbors(p.x(), p.y())) {
            dfsGroup(n, color, visited, group);
        }
    }

    private int countLiberties(Set<Point> group) {
        Set<Point> liberties = new HashSet<>();

        for (Point p : group) {
            for (Point n : neighbors(p.x(), p.y())) {
                if (board.getInterSec(n.x(), n.y()) == StoneColor.EMPTY_STONE) {
                    liberties.add(n);
                }
            }
        }
        return liberties.size();
    }

    /* ===================== CAPTURE / HELPERS ===================== */

    private void captureGroup(Set<Point> group, List<Point> captured) {
        for (Point p : group) {
            board.setInterSec(p.x(), p.y(), StoneColor.EMPTY_STONE);
            captured.add(p);
        }
    }

    private List<Point> neighbors(int x, int y) {
        List<Point> n = new ArrayList<>();
        int size = board.getSize();

        if (x > 1) n.add(new Point(x - 1, y));
        if (x < size) n.add(new Point(x + 1, y));
        if (y > 1) n.add(new Point(x, y - 1));
        if (y < size) n.add(new Point(x, y + 1));

        return n;
    }

    private StoneColor opposite(StoneColor c) {
        return c == StoneColor.BLACK_STONE
                ? StoneColor.WHITE_STONE
                : StoneColor.BLACK_STONE;
    }

    /* ==================== COUNTING TERRITORY ===================== */

    public Map<StoneColor, Integer> countTerritory(Map<StoneColor, Integer> prisoners) {
        int size = board.getSize();
        boolean[][] visited = new boolean[size][size];
        Map<StoneColor, Integer> territory = new HashMap<>();

        territory.put(StoneColor.BLACK_STONE, prisoners.getOrDefault(StoneColor.BLACK_STONE, 0));
        territory.put(StoneColor.WHITE_STONE, prisoners.getOrDefault(StoneColor.WHITE_STONE, 0));

        for (int x = 1; x <= size; x++) {
            for (int y = 1; y <= size; y++) {
                if (board.getInterSec(x, y) != StoneColor.EMPTY_STONE || visited[x-1][y-1])
                    continue;

                Set<Point> region = new HashSet<>();
                Set<StoneColor> borderingColors = new HashSet<>();
                exploreEmptyRegion(x, y, region, borderingColors, visited);

                if (borderingColors.size() == 1) {
                    StoneColor owner = borderingColors.iterator().next();
                    territory.put(owner, territory.get(owner) + region.size());
                }
            }
        }
        return territory;
    }

    private void exploreEmptyRegion(int x, int y, Set<Point> region, Set<StoneColor> borderingColors, boolean[][] visited) {
        int size = board.getSize();
        if (!board.inBounds(x, y))
            return;
        if (visited[x-1][y-1])
            return;

        StoneColor cell = board.getInterSec(x, y);
        visited[x-1][y-1] = true;

        if (cell == StoneColor.EMPTY_STONE) {
            region.add(new Point(x, y));
            exploreEmptyRegion(x-1, y, region, borderingColors, visited);
            exploreEmptyRegion(x+1, y, region, borderingColors, visited);
            exploreEmptyRegion(x, y-1, region, borderingColors, visited);
            exploreEmptyRegion(x, y+1, region, borderingColors, visited);
        } else {
            borderingColors.add(cell);
        }
    }
}
