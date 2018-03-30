package uk.ac.nott.cs.g53dia.multidemo;

import java.util.*;

/**
 * Multiagent planning
 */
public class Multiplanner extends Planner {
    @Override
    public Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap, Hashtable<Integer, List<CoreEntity>> entities,
                                  Deque<CoreEntity> moves, TankerCoordinator tc, long timestep) {
        List<ClusterEntity> cm = clustermap.getClusterMap();
        if(cm.isEmpty()) { // No plan if there is no cluster
            return null;
        }

        // Check if there is a cluster available
        boolean[] availableCluster = new boolean[cm.size()];
        Arrays.fill(availableCluster, false);
        int i = 0; // counter
        for(ClusterEntity ce : cm) {
            if(isAvailableCluster(ce, DemoFleet.allTankers, tc.getTankerID()) && ce.enabledCluster(timestep)) {
                availableCluster[i] = true;
            }
            i++;
        }
        i = 0;
        List<CoreEntity> clusterNominee = new ArrayList<>();
        for(boolean ac : availableCluster) {
            if(ac) {
                clusterNominee.add(cm.get(i).asEntityNode());
            }
            i++;
        }

        if(clusterNominee.isEmpty()) { // No available cluster
            return null;
        }
        else {
            // Bid for the closest cluster
            CoreEntity desiredCluster = getClosestEntityTo(clusterNominee, tc.getEntityUnderTanker());
            int dist = tc.getTankerCoordinate().distanceToCoordinate(desiredCluster.getCoord());
            if(super.acceptableFuelLevel(tc.getFuelLevel(), dist)) { // Is immediately reacheable with current tanker fuel level
                if(!moves.isEmpty() && moves.peekLast().isDirectionalEntity()) {
                    moves.removeLast();
                }
                moves.add(desiredCluster);
                clustermap.setTankerMovingTowards(desiredCluster, tc.getTankerID(), dist);
                tc.setMovingTowardsCluster();
            }
            else {
                List<CoreEntity> fuelpumps = map.getEntityMap(EntityChecker.FUELPUMP);
                List<CoreEntity> path = sourceToDestination(tc.getEntityUnderTanker(), desiredCluster,
                        fuelpumps, ASTAR_ALGORITHM, tc.getFuelLevel(), tc.getWasteLevel());
                if(path.isEmpty()) { // No path found from current tanker position to the closest cluster entity
                    return null;
                }
                else {
                    tc.setMovingTowardsCluster();
                    if(!moves.isEmpty() && moves.peekLast().isDirectionalEntity()) {
                        moves.removeLast();
                    }
                    moves.addAll(path);
                }
            }
        }

        return moves;
    }

    private boolean isAvailableCluster(CoreEntity fuelpump, Hashtable<Integer, TankerCoordinator> allTankers, int tankerID) {
        if(fuelpump.isDirectionalEntity() || fuelpump.getHasTankerMoveTowards()) {
            return false;
        }

        for(int i = 0; i < allTankers.size(); i++) {
            if(i == tankerID) {
                continue;
            }
            if(fuelpump.getCoord().distanceToCoordinate(allTankers.get(i).getTankerCoordinate()) <= ClusterMapBuilder.CLUSTER_RANGE) {
                return false;
            }
        }

        return true;
    }
}
