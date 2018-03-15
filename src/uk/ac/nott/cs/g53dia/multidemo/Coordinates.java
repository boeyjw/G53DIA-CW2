package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

/**
 * Utility class to store coordinates in tuple form
 */
public class Coordinates extends TwoNumberTuple {
    private static final Coordinates tankerCoordinate = new Coordinates(Tanker.VIEW_RANGE, Tanker.VIEW_RANGE);

    Coordinates(int x, int y) {
        super(x, y);
    }

    public void modifyCoordinates(int x, int y) {
        if(x > 0)
            this.x = x;
        if(y > 0)
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

    public static Coordinates getTankerCoordinate() {
        return tankerCoordinate;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
