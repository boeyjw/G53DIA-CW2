package uk.ac.nott.cs.g53dia.multidemo;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Builder class to create collection of clusters
 */
public class ClusterMapBuilder extends MapBuilder {
    // All entities within this constant will be added into the cluster
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

    /**
     * Creates or updates a cluster based on {@link DemoFleet#mapper}
     * @param map {@link DemoFleet#mapper} hashtable object
     * @return {@link MapBuilder#REJECTED} if no fuel pump in {@link DemoFleet#mapper} or if cluster is invalid.
     * {@link MapBuilder#ADD} if a new cluster is added or {@link MapBuilder#EXIST} if an existing cluster is updated
     */
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

    /**
     * Adds a new cluster into cluster map
     * @param fuelpump cluster centre
     * @param entities A merged/flattened {@link DemoFleet#mapper}
     * @return {@link MapBuilder#ADD} if a new cluster is registered. {@link MapBuilder#REJECTED} if cluster does not satisfy conditions
     */
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

    /**
     * Updates an existing cluster
     * @param fuelpump The centre of the cluster
     * @param entities A merged/flattened {@link DemoFleet#mapper}
     * @return {@link MapBuilder#ADD} if updated. {@link MapBuilder#EXIST} if no changes are made
     */
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

    /**
     * Checks if an entity is within the cluster radii
     * @param fuelpump Cluster centre
     * @param entity Entity to be checked against
     * @return True if within range
     */
    private boolean isWithinClusterRange(CoreEntity fuelpump, CoreEntity entity) {
        return !fuelpump.equals(entity) && fuelpump.getCoord().distanceToCoordinate(entity.getCoord()) <= CLUSTER_RANGE;
    }

    /**
     * Checks if the fuel pump is a centre of any cluster
     * @param fuelpump Fuel pump to be checked against
     * @return True if a cluster exist for the fuel pump
     */
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

    /**
     * Tags the tanker that is moving towards a specific cluster. Always execute this function when a tanker deliberates
     * to the cluster
     * @param fuelpump Cluster centre
     * @param tankerID Tanker ID
     * @param initialDistance Distance between tanker and the cluster
     */
    public void setTankerMovingTowards(CoreEntity fuelpump, int tankerID, int initialDistance) {
        for(ClusterEntity ce : clusterMap) {
            if(ce.getEntityHash() == fuelpump.getEntityHash()) {
                ce.setTankerMoveTowardsInformation(tankerID, initialDistance);
            }
        }
    }

    /**
     * Sets the last visited timestep of a specific cluster. Always execute this function when a cluster
     * has been visited by a tanker
     * @param fuelpump Cluster centre
     * @param currentTimestep Current timestep
     */
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