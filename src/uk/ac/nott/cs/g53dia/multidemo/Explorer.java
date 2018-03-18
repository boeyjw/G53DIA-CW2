package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multidemo.CoreEntity;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;
import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;

import java.util.*;

/**
 * Handles exploration of the tanker
 */
public class Explorer extends Mapper {
    public static boolean explorerMode = false;
    private HashMap<Integer, Integer> crossDirectionMovement;
    private int direction;
    private long startExplorerTimestep;
    private long endExplorerTimeStep;

    private MapBuilder exploitableAreas;

    public Explorer(Random r) {
        crossDirectionMovement = new HashMap<>();
        init();
        direction = r.nextInt(Threshold.TOTAL_DIRECTION_BOUND.getThreshold());
        startExplorerTimestep = 0;
        endExplorerTimeStep = 0;

        exploitableAreas = new MapBuilder();
    }

    /**
     * HashTable of cross directions to explore.
     * If the entire HasThTable is traversed, the tanker essentially moved all 8 directions
     */
    private void init() {
        crossDirectionMovement.put(MoveAction.NORTH, MoveAction.EAST);
        crossDirectionMovement.put(MoveAction.EAST, MoveAction.WEST);
        crossDirectionMovement.put(MoveAction.WEST, MoveAction.SOUTH);
        crossDirectionMovement.put(MoveAction.SOUTH, MoveAction.NORTHEAST);
        crossDirectionMovement.put(MoveAction.NORTHEAST, MoveAction.NORTHWEST);
        crossDirectionMovement.put(MoveAction.NORTHWEST, MoveAction.SOUTHEAST);
        crossDirectionMovement.put(MoveAction.SOUTHEAST, MoveAction.SOUTHWEST);
        crossDirectionMovement.put(MoveAction.SOUTHWEST, MoveAction.NORTH);
    }

    public int getAndUpdateDirection() {
        int dir = direction;
        direction = crossDirectionMovement.get(direction);
        return dir;
    }

    public int getDirectionUsingClusterAttraction(Hashtable<String, List<CoreEntity>> entities) {
        Integer[] directionCounter = new Integer[Threshold.TOTAL_DIRECTION_BOUND.getThreshold()];
        Arrays.fill(directionCounter, 0);
        for(CoreEntity e: entities.get("station")) {
            if(e.getBearing() != Integer.MIN_VALUE) {
                directionCounter[e.getBearing()]++;
            }
        }
        for(CoreEntity e : entities.get("taskedStation")) {
            if(e.getBearing() != Integer.MIN_VALUE) {
                directionCounter[e.getBearing()] += 2;
            }
        }
        int dir = Calculation.argmax_int(directionCounter);
        direction = dir == direction ? getAndUpdateDirection() : dir;

        return direction;
    }

    public long getStartExplorerTimestep() {
        return startExplorerTimestep;
    }

    public void setStartExplorerTimestep(long startExplorerTimestep) {
        this.startExplorerTimestep = startExplorerTimestep;
    }

    /**
     * Loads waste whenever possible during exploration
     * @param moves Inject into moveset
     * @param wasteLevel Current agent waste level
     * @param station Stations with task that has been seen
     */
    public void getPassbyTask(Deque<Cell> moves, int wasteLevel, List<CoreEntity> station) {
        if(explorerMode && !station.isEmpty() && moves.isEmpty() && super.acceptableWasteLevel(wasteLevel)) {
                moves.addLast(station.get(station.size() - 1).getEntity());
        }
    }

    public long getEndExplorerTimeStep() {
        return endExplorerTimeStep;
    }

    public void setEndExplorerTimeStep(long endExplorerTimeStep) {
        this.endExplorerTimeStep = endExplorerTimeStep;
    }

    public int saveExploitableAreas(Hashtable<Integer, List<CoreEntity>> entities) {
        return -1;
    }
}
