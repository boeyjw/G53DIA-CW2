package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multidemo.Calculation;
import uk.ac.nott.cs.g53dia.multidemo.Coordinates;
import uk.ac.nott.cs.g53dia.multidemo.CoreEntity;
import uk.ac.nott.cs.g53dia.multidemo.EntityNode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public abstract class Mapper {

    protected boolean acceptableFuelLevel(int estFuelLevel, int distToFuelPump) {
        return estFuelLevel > distToFuelPump + Threshold.REFUEL_ERROR_MARGIN.getThreshold();
    }

    protected boolean acceptableWasteLevel(int estWasteLevel) {
        return !Threshold.HIGHEST_WASTE.hitThreshold(estWasteLevel);
    }

    /**
     * Creates a queue of moves to make diagonally and straight movements manually to mvoe from source to target coordinates
     * @param source Source coordinate
     * @param target Target coordinate
     * @return Queue containing number of diagonal and straight steps to reach target from given source
     */
    @Deprecated
    protected Deque<Integer> planMoveTo(Coordinates source, Coordinates target) {
        ArrayDeque<Integer> moves = new ArrayDeque<>();
        TwoNumberTuple preManhatten = source.manhattenAbsolute(target);

        int diagonal = preManhatten.getMin();
        int straight = preManhatten.getMax() - preManhatten.getMin();
        int bearing = Calculation.targetBearing(source, target);

        if(diagonal != 0) {
            moves.add(Calculation.targetBearing(source, target));
            moves.add(diagonal);
        }
        if(straight != 0) {
            moves.add(Calculation.targetBearing((Coordinates) source.simpleOperation(diagonal, null, "+"), target));
            moves.add(straight);
        }

        return moves;
    }

    protected int getClosestEntityDistanceTo(List<CoreEntity> desiredEntity, EntityNode current) {
        if(desiredEntity.isEmpty())
            return Integer.MIN_VALUE;
        else if(desiredEntity.size() == 1)
            return Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(0).getCoord());

        int min = 0;
        int argmin = 0;
        int[] dist = new int[desiredEntity.size()];
        for(int i = 0; i < desiredEntity.size(); i++) {
            dist[i] = Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(i).getCoord());
            if(i == 0)
                min = dist[i];
            else if(dist[i] > min) {
                argmin = i;
                min = dist[i];
            }
        }

        return dist[argmin];
    }
}
