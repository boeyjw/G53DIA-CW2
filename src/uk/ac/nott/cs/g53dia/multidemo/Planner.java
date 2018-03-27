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
     * @param clustermap
     * @param tc Tanker coordinator
     * @param timestep The current timestep
     * @return A sequence of moves which has been planned
     */
    public abstract Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap,
                                           Hashtable<Integer, List<CoreEntity>> entities, Deque<CoreEntity> moves,
                                           TankerCoordinator tc, long timestep);

    protected boolean acceptableFuelLevel(int estFuelLevel, int distToFuelPump) {
        return estFuelLevel > distToFuelPump + Threshold.REFUEL_ERROR_MARGIN.getThreshold();
    }

    protected boolean acceptableFuelLevel(int estFuelLevel, Coordinates tankerCoordinate, Coordinates fuelpump) {
        return acceptableFuelLevel(estFuelLevel, tankerCoordinate.distanceToCoordinate(fuelpump));
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

    protected CoreEntity getClosestEntityTo(List<CoreEntity> desiredEntity, CoreEntity current) {
        if(desiredEntity.isEmpty())
            return null;
        else if(desiredEntity.size() == 1)
            return desiredEntity.get(0);

        int min = Integer.MAX_VALUE;
        int argmin = -1;
        for(int i = 0; i < desiredEntity.size(); i++) {
            int dist = Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(i).getCoord());
            if(dist < min) {
                min = dist;
                argmin = i;
            }
        }

        return argmin >= 0 ? desiredEntity.get(argmin) : null;
    }

    protected int distAtoCviaB(CoreEntity a, CoreEntity b, CoreEntity c) {
        return a.getCoord().distanceToCoordinate(b.getCoord()) + b.getCoord().distanceToCoordinate(c.getCoord());
    }

    protected boolean allowAddMove(Deque<CoreEntity> moves, int entityType) {
        if(moves.isEmpty()) {
            return true;
        }
        if(moves.peekFirst().isDirectionalEntity()) {
            if(EntityChecker.DUMMY == entityType) {
                return false;
            }
            return true;
        }
        if(EntityChecker.getEntityType(moves.peekFirst().getEntity(), true) == entityType) {
            return false;
        }

        return false;
    }
}
