package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;

import java.net.Inet4Address;
import java.util.*;

/**
 * Handles exploration of the tanker
 */
public class Explorer extends Planner {
    public static boolean explorerMode = false;
    private HashMap<Integer, Integer> crossDirectionMovement;
    private int direction;
    private long startExplorerTimestep;
    private long endExplorerTimeStep;

    public Explorer() {
        crossDirectionMovement = new HashMap<>();
        init();
        direction = MoveAction.NORTHEAST;
        startExplorerTimestep = 0;
        endExplorerTimeStep = 0;
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

    @Override
    public Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap,
                                  Hashtable<Integer, List<CoreEntity>> entities, Deque<CoreEntity> moves,
                                  TankerCoordinator tc, long timestep) {
        if (allowAddMove(moves, EntityChecker.FUELPUMP) &&
                !super.acceptableFuelLevel(tc.getFuelLevel(), tc.getTankerCoordinate(), tc.getClosestObservableFuelpump().getCoord())) {
            moves.addFirst(tc.getClosestObservableFuelpump());
            DemoFleet.mapper.setTankerMoveTowardsEntity(moves.peekFirst(), tc.getTankerID(), tc.getTankerCoordinate().distanceToCoordinate(moves.peekFirst().getCoord()));
        } else if (allowAddMove(moves, EntityChecker.WELL) && !super.acceptableWasteLevel(tc.getWasteLevel()) && tc.getClosestObservableWell() != null &&
                super.acceptableFuelLevel(tc.getFuelLevel(),
                        distAtoCviaB(tc.getEntityUnderTanker(), tc.getClosestObservableWell(), tc.getClosestObservableFuelpump()))) {
            moves.addFirst(tc.getClosestObservableWell());
            DemoFleet.mapper.setTankerMoveTowardsEntity(moves.peekFirst(), tc.getTankerID(), tc.getTankerCoordinate().distanceToCoordinate(moves.peekFirst().getCoord()));
        }

        if (allowAddMove(moves, EntityChecker.DUMMY)) { // Unrestricted exploration to the same direction
            if(timestep <= 300) {
                moves.add(new EntityNode(direction));
            }
            else {
                moves.add(getAndUpdateDirection(DemoFleet.explorationDirection));
            }
        } else if (allowAddMove(moves, EntityChecker.STATION)) {
            getPassbyTask(moves, tc, entities.get(EntityChecker.TASKEDSTATION));
        }

        return moves;
    }

    public int getAndUpdateDirection() {
        int dir = direction;
        direction = crossDirectionMovement.get(direction);
        return dir;
    }

    public CoreEntity getAndUpdateDirection(List<Integer> tankersExploringDirection) {
        if (!tankersExploringDirection.isEmpty()) {
            int numDir = 0;
            while (tankersExploringDirection.contains(direction) && numDir++ < Threshold.TOTAL_DIRECTION_BOUND.getThreshold()) {
                getAndUpdateDirection();
            }
        }

        return new EntityNode(direction);
    }

    @Deprecated
    public int getDirectionUsingClusterAttraction(Hashtable<String, List<CoreEntity>> entities) {
        Integer[] directionCounter = new Integer[Threshold.TOTAL_DIRECTION_BOUND.getThreshold()];
        Arrays.fill(directionCounter, 0);
        for (CoreEntity e : entities.get("station")) {
            if (e.getBearing() != Integer.MIN_VALUE) {
                directionCounter[e.getBearing()]++;
            }
        }
        for (CoreEntity e : entities.get("taskedStation")) {
            if (e.getBearing() != Integer.MIN_VALUE) {
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

    public void getPassbyTask(Deque<CoreEntity> moves, TankerCoordinator tc, List<CoreEntity> taskedStation) {
        if(!taskedStation.isEmpty()) {
            CoreEntity ts = taskedStation.get(taskedStation.size() - 1);
            // IF there is a station in sight AND moveset is explorer AND has sufficient waste containment space AND has sufficient fuel
            // to go to the tasked station and to the closest fuel pump AND no other tanker going to the station
            if (passByTaskCheck(moves) && super.acceptableWasteLevel(tc.getWasteLevel()) && ts.getBearing() == moves.peekFirst().getBearing() &&
                    !DemoFleet.mapper.getEntity(ts).getHasTankerMoveTowards() &&
                    super.acceptableFuelLevel(tc.getFuelLevel(), distAtoCviaB(tc.getEntityUnderTanker(), ts, tc.getClosestObservableFuelpump()))) {
                moves.addFirst(ts);
                DemoFleet.mapper.setTankerMoveTowardsEntity(ts, tc.getTankerID(), tc.getTankerCoordinate().distanceToCoordinate(ts.getCoord()));
            }
        }
    }

    private boolean passByTaskCheck(Deque<CoreEntity> moves) {
        if(moves.isEmpty()) {
            return true;
        }
        if(moves.peekFirst().isDirectionalEntity()) {
            return true;
        }

        return false;
    }

    public long getEndExplorerTimeStep() {
        return endExplorerTimeStep;
    }

    public void setEndExplorerTimeStep(long endExplorerTimeStep) {
        this.endExplorerTimeStep = endExplorerTimeStep;
    }

}
