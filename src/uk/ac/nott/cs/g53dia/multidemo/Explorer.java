package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.MoveAction;

import java.util.*;

/**
 * Handles exploration of the tanker
 */
public class Explorer extends Planner {
    public static int CHARTING_RADIUS = 50;

    private HashMap<Integer, Integer> crossDirectionMovement;
    private Integer randomDirection;
    private Integer[] directions;

    public Explorer() {
        crossDirectionMovement = new HashMap<>();
        directions = new Integer[Threshold.TOTAL_DIRECTION_BOUND.getThreshold()];
        randomDirection = new Random().nextInt(Threshold.TOTAL_DIRECTION_BOUND.getThreshold());
        init();
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

        for(Integer dir : crossDirectionMovement.keySet()) {
            directions[dir] = dir;
        }
    }

    public CoreEntity startUpDirection() {
        int startDir = MoveAction.NORTHEAST;
        while(DemoFleet.explorationDirection.containsValue(startDir)) {
            startDir = crossDirectionMovement.get(startDir);
        }

        return new EntityNode(startDir);
    }

    @Override
    public Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap, Hashtable<Integer, List<CoreEntity>> entities,
                                  Deque<CoreEntity> moves, TankerCoordinator tc, long timestep) {
        if(moves.isEmpty()) {
            getPassbyTask(moves, tc, entities.get(EntityChecker.TASKEDSTATION));
            moves.addLast(decideDirection(map.getEntityMap(EntityChecker.FUELPUMP), tc));
        }
        else if(moves.size() == 1) {
            if(moves.peekFirst().isDirectionalEntity()) {
                getPassbyTask(moves, tc, entities.get(EntityChecker.TASKEDSTATION));
            }
            else {
                moves.addLast(decideDirection(map.getEntityMap(EntityChecker.FUELPUMP), tc));
            }
        }
        else {
            if(moves.peekLast().isDirectionalEntity()) {
                moves.removeLast();
                moves.addLast(decideDirection(map.getEntityMap(EntityChecker.FUELPUMP), tc));
            }
        }

        return moves;
    }

    public CoreEntity decideDirection(List<CoreEntity> fuelpumps, TankerCoordinator tc) {
        Set<Integer> tabuMoves = new HashSet<>(DemoFleet.explorationDirection.values()); // Disallow any tankers travelling the same direction
        if(!EntityChecker.isFuelPump(tc.getEntityUnderTanker().getEntity())) {
            return new EntityNode(DemoFleet.explorationDirection.get(tc.getTankerID()));
        }
        if(tabuMoves.size() >= Threshold.TOTAL_DIRECTION_BOUND.getThreshold()) {
            return new EntityNode(randomDirection);
        }

        // Random direction only at no tanker bearing
        while(tabuMoves.contains(randomDirection)) {
            randomDirection = crossDirectionMovement.get(randomDirection);
        }

        // Global fuel pump bearing check
        int[] fpcounter = new int[Threshold.TOTAL_DIRECTION_BOUND.getThreshold()];
        Arrays.fill(fpcounter, 0);
        for(CoreEntity fp : fuelpumps) {
            int bearing = Calculation.targetBearing(tc.getTankerCoordinate(), fp.getCoord());
            if(bearing != Calculation.ONTANKER) {
                tabuMoves.add(bearing);
                fpcounter[bearing]++;
            }
        }

        int dir = MoveAction.NORTHWEST;
        if(tabuMoves.size() >= Threshold.TOTAL_DIRECTION_BOUND.getThreshold()) {
            int min = Integer.MAX_VALUE;
            for(int i = 0; i < fpcounter.length; i++) {
                if(fpcounter[i] < min) {
                    min = fpcounter[i];
                    dir = i;
                }
            }
        }
        else {
            while(tabuMoves.contains(dir)) {
                dir = crossDirectionMovement.get(dir);
            }
        }

        return new EntityNode(dir == DemoFleet.explorationDirection.get(tc.getTankerID()) ? crossDirectionMovement.get(dir) : dir);
    }

    public void getPassbyTask(Deque<CoreEntity> moves, TankerCoordinator tc, List<CoreEntity> taskedStation) {
        if (!taskedStation.isEmpty()) {
            CoreEntity ts = taskedStation.get(taskedStation.size() - 1);
            // IF there is a station in sight AND moveset is explorer AND has sufficient waste containment space AND has sufficient fuel
            // to go to the tasked station and to the closest fuel pump AND no other tanker going to the station
            if (passByTaskCheck(moves) && super.acceptableWasteLevel(tc.getWasteLevel()) && ts.getBearing() == moves.peekFirst().getBearing() &&
                    !DemoFleet.mapper.getEntity(ts).getHasTankerMoveTowards() &&
                    super.acceptableFuelLevel(tc.getFuelLevel(), distAtoCviaB(tc.getEntityUnderTanker(), ts, tc.getClosestObservableFuelpump()))) {
                moves.addFirst(ts);
            }
        }
    }

    private boolean passByTaskCheck(Deque<CoreEntity> moves) {
        if (moves.isEmpty()) {
            return false;
        }
//        if (moves.peekFirst().isDirectionalEntity()) {
//            return true;
//        }

        return false;
    }
}
