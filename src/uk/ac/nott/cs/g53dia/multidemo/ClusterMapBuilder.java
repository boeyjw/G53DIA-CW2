package uk.ac.nott.cs.g53dia.multidemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

public class ClusterMapBuilder extends MapBuilder {
    public static int CLUSTER_RANGE = 20;

    private List<ClusterEntity> clusterMap;
    private int[] condition;

    ClusterMapBuilder() {
        clusterMap = new ArrayList<>();

        condition = new int[3];
        condition[0] = 1; // min number of fuelpump
        condition[1] = 1; // min number of wells
        condition[2] = 5; // min number of station
    }

    public int buildCluster(Hashtable<Integer, List<CoreEntity>> map) {
        int status = REJECTED;
        List<CoreEntity> mergedmap = new ArrayList<>();
        mergedmap.addAll(map.get(EntityChecker.FUELPUMP));
        mergedmap.addAll(map.get(EntityChecker.WELL));
        mergedmap.addAll(map.get(EntityChecker.STATION));

        for(CoreEntity fp : map.get(EntityChecker.FUELPUMP)) {
            status = hasCluster(fp) ? updateCluster(fp, mergedmap) : addCluster(fp, mergedmap);
        }

        return status;
    }

    private int addCluster(CoreEntity fuelpump, List<CoreEntity> entities) {
        ClusterEntity ce = new ClusterEntity(fuelpump.getEntity(), fuelpump.getCoord(), fuelpump.getFirstSeen(), condition);
        ce.add(fuelpump);

        for(CoreEntity e : entities) {
            if(isWithinClusterRange(fuelpump, e)) {
                ce.add(e);
            }
        }

        if(ce.validateCluster()) {
            clusterMap.add(ce);
            return ADD;
        }

        return REJECTED;
    }

    private int updateCluster(CoreEntity fuelpump, List<CoreEntity> entities) {
        int ind = -1;
        for(ClusterEntity ce : clusterMap) {
            ind++;
            if(ce.getEntity().equals(fuelpump.getEntity())) {
                break;
            }
        }

        ClusterEntity ce = clusterMap.get(ind);
        boolean hasAdd = false;
        for(CoreEntity e : entities) {
            if(isWithinClusterRange(ce, e)) {
                hasAdd = ce.add(e);
            }
        }

        return hasAdd ? ADD : EXIST;
    }

    private boolean isWithinClusterRange(CoreEntity fuelpump, CoreEntity entity) {
        return !fuelpump.equals(entity) && fuelpump.getCoord().distanceToCoordinate(entity.getCoord()) <= CLUSTER_RANGE;
    }

    public boolean hasCluster(CoreEntity fuelpump) {
        for(ClusterEntity ce : clusterMap) {
            if(ce.getEntity().getPoint().hashCode() == fuelpump.getEntity().getPoint().hashCode()) {
                return true;
            }
        }

        return false;
    }

    public List<ClusterEntity> getClusterMap() {
        return clusterMap;
    }

    public void setTankerMovingTowards(CoreEntity fuelpump, int tankerID, int initialDistance) {
        for(ClusterEntity ce : clusterMap) {
            if(ce.getEntityHash() == fuelpump.getEntityHash()) {
                ce.setTankerMoveTowardsInformation(tankerID, initialDistance);
            }
        }
    }

    public void setLastVisitedCluster(CoreEntity fuelpump, long currentTimestep) {
        for(ClusterEntity ce : clusterMap) {
            if(ce.getEntityHash() == fuelpump.getEntityHash()) {
                ce.setLastVisitedCluster(currentTimestep);
            }
        }
    }

    @Override
    public String toString() {
        return "Clustermap size: " + clusterMap.size();
    }
}