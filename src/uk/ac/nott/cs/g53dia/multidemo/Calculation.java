package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multidemo.Coordinates;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;

import java.util.Hashtable;

/**
 * Mainly a static class that handles calculations
 */
public class Calculation {
    public static final int ONTANKER = 8;

    private static Hashtable<Integer, Integer> directionReflect;

    static {
        directionReflect = new Hashtable<>();
        directionReflect.put(MoveAction.NORTH, MoveAction.SOUTH);
        directionReflect.put(MoveAction.SOUTH, MoveAction.NORTH);
        directionReflect.put(MoveAction.EAST, MoveAction.WEST);
        directionReflect.put(MoveAction.WEST, MoveAction.EAST);
        directionReflect.put(MoveAction.NORTHEAST, MoveAction.SOUTHEAST);
        directionReflect.put(MoveAction.SOUTHEAST, MoveAction.NORTHWEST);
        directionReflect.put(MoveAction.NORTHWEST, MoveAction.SOUTHWEST);
        directionReflect.put(MoveAction.SOUTHWEST, MoveAction.NORTHWEST);
    }
    /**
     * Calculate Euclidean distnace between Coordinates
     * @param source The source coordinate
     * @param target The target coordinate
     * @param doSqrt Whether to complete the entire Euclidean distance equation with a square root
     * @return Euclidean distance
     */
    public static double calculateEuclideanDistance(Coordinates source, Coordinates target, boolean doSqrt) {
        return doSqrt ? Math.sqrt(Math.pow((target.getValue(0) - source.getValue(0)), 2) + Math.pow((target.getValue(1) - source.getValue(1)), 2)) :
                Math.pow((target.getValue(0) - source.getValue(0)), 2) + Math.pow((target.getValue(1) - source.getValue(1)), 2);
    }

    /**
     * Checks if the given value is a perfect square (ie 4, 9, 16, 25)
     * @param value The value to be checked
     * @return True if its a perfect square, false otherwise
     */
    public static boolean isPerfectSquare(long value) {
        long sqrt = (long) Math.sqrt(value);
        return value == sqrt * sqrt;
    }

    /**
     * Checks the direction in which the target coordinate is from source
     * @param source The source coordinate
     * @param target The target coordinate
     * @return Bearing of the target from source
     */
    public static int targetBearing(Coordinates source, Coordinates target) {
        int bearing = Integer.MIN_VALUE;
        int xdiff = target.getValue(0) - source.getValue(0);
        int ydiff = target.getValue(1) - source.getValue(1);

        if(xdiff == 0 && ydiff != 0) {
            bearing = ydiff > 0 ? MoveAction.NORTH : MoveAction.SOUTH;
        }
        else if(xdiff != 0 && ydiff == 0) {
            bearing = xdiff >0 ? MoveAction.EAST : MoveAction.WEST;
        }
        else {
            if(xdiff < 0 && ydiff > 0)
                bearing = MoveAction.NORTHWEST;
            else if(xdiff > 0 && ydiff > 0)
                bearing = MoveAction.NORTHEAST;
            else if(xdiff < 0 && ydiff < 0)
                bearing = MoveAction.SOUTHWEST;
            else if(xdiff > 0 && ydiff < 0)
                bearing = MoveAction.SOUTHEAST;
            else
                bearing = ONTANKER;
        }

        return bearing;
    }

    /**
     * Gets string representation of the direction to be taken.
     * Direct copy from {@link MoveAction#toString()}
     * @param direction The constant representation of the direction
     * @return Direction in string
     */
    public static String directionToString(int direction) {
        String dirStr[] = {
                "NORTH", "SOUTH", "EAST", "WEST", "NORTHEAST",
                "NORTHWEST", "SOUTHEAST", "SOUTHWEST", "ONTANKER"
        };
        return dirStr[direction];
    }

    /**
     * Gets the closest value in array to the value supplied
     * @param arr The array containing values
     * @param value The value to be checked against
     * @return The closest value in the array against the value to be checked against
     */
    public static long getClosestValue(long[] arr, long value) {
        int low = 0;
        int high = arr.length - 1;

        if(high < 0)
            return Integer.MIN_VALUE;

        while(low < high) {
            int mid = (low + high) / 2;
            long diff1 = Math.abs(arr[mid] - value);
            long diff2 = Math.abs(arr[mid + 1] - value);

            if(diff2 <= diff1)
                low = mid + 1;
            else
                high = mid;
        }

        return arr[high];
    }

    /**
     * Calculate Manhatten distance between two coordinates which takes into account diagonal movements.
     * @param source The source coordinate
     * @param target The target coordinate
     * @return The distance between two coordinates
     */
    public static int diagonalDistance(Coordinates source, Coordinates target) {
        return Math.max(
                Math.abs(target.getValue(0) - source.getValue(0)),
                Math.abs(target.getValue(1) - source.getValue(1)));
    }

    public static TwoNumberTuple diagonalDistanceTuple(Coordinates source, Coordinates target) {
        return new TwoNumberTuple(
                Math.abs(target.getValue(0) - source.getValue(0)),
                Math.abs(target.getValue(1) - source.getValue(1)));
    }

    /**
     * Generates an array of values which are perfect sqaures within a minimum and maximum bound
     * @param min Minimum bound
     * @param max Maximum bound
     * @return Array of perfect squares
     */
    public long[] generatePerfectSquareListFromRange(int min, int max) {
        long[] perfSqr = new long[max - min + 1];
        int c = 0;

        for(int i = min; i <= max; i++) {
            if((Math.pow(i + 1, 2) - Math.pow(i, 2)) == ((2 * i) + 1)) {
                perfSqr[c++] = i;
            }
        }

        return perfSqr;
    }

    /**
     * Find argmax for int types
     * @param arr Array to traverse
     * @return Argmax
     */
    public static Integer argmax_int(Integer[] arr) {
        Integer argmax = 0;
        Integer max = Integer.MIN_VALUE;
        for(int i = 0; i < arr.length; i++) {
            if(arr[i] > max) {
                max = arr[i];
                argmax = i;
            }
        }

        return argmax;
    }
}
