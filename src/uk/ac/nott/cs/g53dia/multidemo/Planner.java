package uk.ac.nott.cs.g53dia.multidemo;

import java.util.Deque;
import java.util.Hashtable;
import java.util.List;

/**
 * All planning essentials
 */
public abstract class Planner {

    /**
     * Core planning function the tanker makes
     * @param map All entities observed by the tanker
     * @param tc Tanker coordinator
     * @param tankerStatus (fuelLevel, wasteLevel) of the tanker
     * @param timestep The current timestep
     * @return A sequence of moves which has been planned
     */
    public abstract Deque<CoreEntity> plan(Hashtable<Integer, List<CoreEntity>> map,
                                           TankerCoordinator tc, TwoNumberTuple tankerStatus, long timestep);

    protected boolean acceptableFuelLevel(int estFuelLevel, int distToFuelPump) {
        return estFuelLevel > distToFuelPump + Threshold.REFUEL_ERROR_MARGIN.getThreshold();
    }

    protected boolean acceptableWasteLevel(int estWasteLevel) {
        return !Threshold.HIGHEST_WASTE.hitThreshold(estWasteLevel);
    }

    protected int getClosestEntityDistanceTo(List<CoreEntity> desiredEntity, CoreEntity current) {
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
