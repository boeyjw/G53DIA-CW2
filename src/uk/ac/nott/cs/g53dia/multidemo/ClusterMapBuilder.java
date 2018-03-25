package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

import java.util.*;

public class ClusterMapBuilder extends MapBuilder {
    public static final int SMALL_CLUSTER_RADIUS = 10;
    public static final int MEDIUM_CLUSTER_RADIUS = 15;
    public static final int OBSERVABLE_CLUSTER_RADIUS = 20;

    private Hashtable<Integer, List<ClusterEntity>> clusterMap;

    ClusterMapBuilder() {
        clusterMap = new Hashtable<>();
        clusterMap.put(SMALL_CLUSTER_RADIUS, new ArrayList<>());
        clusterMap.put(MEDIUM_CLUSTER_RADIUS, new ArrayList<>());
        clusterMap.put(OBSERVABLE_CLUSTER_RADIUS, new ArrayList<>());
    }

    public boolean addCluster(CoreEntity fuelpump, List<CoreEntity> entities) {
        return false;
    }

    public boolean hasCluster(int clusterRadius, CoreEntity fuelpump) {
        return clusterMap.get(clusterRadius).contains(fuelpump);
    }

    public ClusterEntity getCluster(int clusterRadius, CoreEntity fuelpump) {
        for(ClusterEntity c : clusterMap.get(clusterRadius)) {
            if(fuelpump.equals(c)) {
                return c;
            }
        }

        return null;
    }

    private int getClosestEntityDistance(CoreEntity source, List<CoreEntity> targets) {
        if(targets.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        else if(targets.size() == 1) {
            return source.getCoord().distanceToCoordinate(targets.get(0).getCoord());
        }

        int min = Integer.MAX_VALUE;
        for(CoreEntity t : targets) {
            int dist = source.getCoord().distanceToCoordinate(t.getCoord());
            if(dist < min) {
                min = dist;
            }
        }

        return min;
    }
}
