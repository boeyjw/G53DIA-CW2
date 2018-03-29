package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

import java.util.List;

/**
 * Utility class to store coordinates in tuple form
 */
public class Coordinates extends TwoNumberTuple {
    Coordinates(int x, int y) {
        super(x, y);
    }

    public void modifyCoordinates(int x, int y) {
        if(x != Integer.MIN_VALUE)
            this.x = x;
        if(y != Integer.MIN_VALUE)
            this.y = y;
    }

    public void modifyX(int x) {
        this.x = x;
    }

    public void modifyY(int y) {
        this.y = y;
    }

    /**
     * Calculate using diagonal distance between two known coordinates
     * @param target Target coordinate
     * @return The distance between both points
     */
    public int distanceToCoordinate(Coordinates target) {
        return Calculation.diagonalDistance(this, target);
    }

    /**
     * Gets the Manhatten absolute before completing the formula with a subtraction
     * @param target Target coordinate
     * @return A tuple containing diagonal and straight values
     */
    public TwoNumberTuple manhattenAbsolute(Coordinates target) {
        return new TwoNumberTuple(Math.abs(target.getValue(0) - this.getValue(0)),
                Math.abs(target.getValue(1) - this.getValue(1)));
    }

    @Deprecated
    public Coordinates coordinateShiftBy(NumberTuple otherNumberTuple, char operation) {
        switch (operation) {
            case PLUS:
                this.x += otherNumberTuple.getValue(0);
                this.y += otherNumberTuple.getValue(1);
                break;
            case MINUS:
                this.x -= otherNumberTuple.getValue(0);
                this.y -= otherNumberTuple.getValue(1);
                break;
            default:
                throw new IllegalArgumentException("Operation out of scope");
        }

        return this;
    }

    public static Coordinates coordinateShiftBy(NumberTuple source, NumberTuple target, char operation) {
        int x = source.getValue(0);
        int y = source.getValue(1);

        switch (operation) {
            case PLUS:
                x += target.getValue(0);
                y += target.getValue(1);
                break;
            case MINUS:
                x -= target.getValue(0);
                y -= target.getValue(1);
                break;
            default:
                throw new IllegalArgumentException("Operation out of scope");
        }

        return new Coordinates(x, y);
    }

    public int getClosestEntityDistance(List<CoreEntity> targets) {
        if(targets.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        else if(targets.size() == 1) {
            return this.distanceToCoordinate(targets.get(0).getCoord());
        }

        int min = Integer.MAX_VALUE;
        for(CoreEntity t : targets) {
            int dist = this.distanceToCoordinate(t.getCoord());
            if(dist < min) {
                min = dist;
            }
        }

        return min;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Coordinates)) {
            return false;
        }
        Coordinates c = (Coordinates) obj;
        return this.x == c.getValue(0) && this.y == c.getValue(1);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    protected Coordinates clone() {
        return new Coordinates(x, y);
    }
}
