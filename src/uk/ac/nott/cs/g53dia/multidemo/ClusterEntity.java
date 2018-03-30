package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * An entity that represents a cluster/exploitable area
 */
public class ClusterEntity extends CoreEntity {
    public static long VISIT_TIMESTEP_DELAY = 300;

    private Hashtable<Integer, List<CoreEntity>> cluster;
    private int[] condition;
    private long lastVisitedCluster;

    /**
     * Create a cluster with only the fuel pump centre
     * @param entity Centre fuel pump
     * @param coord Coordinate of the fuel pump
     * @param firstSeen First seen fuel pump
     * @param condition Sized 3 int[] where [number of fuel pump, number of well, number of station] condition to meet to qualify as a cluster
     */
    ClusterEntity(Cell entity, Coordinates coord, long firstSeen, int[] condition) {
        super(entity, coord, firstSeen);
        if(condition.length != 3) {
            throw new IllegalArgumentException("Condition must on be length of 3 where number of [fuelpump, well, station]");
        }

        cluster = new Hashtable<>();
        cluster.put(EntityChecker.FUELPUMP, new ArrayList<>());
        cluster.put(EntityChecker.WELL, new ArrayList<>());
        cluster.put(EntityChecker.STATION, new ArrayList<>());
        this.condition = condition;
        lastVisitedCluster = 1;
    }

    /**
     * Adds a single entity to a cluster
     * @param entity
     * @return True if added. False otherwise
     */
    public boolean add(CoreEntity entity) {
        int entityType = EntityChecker.getEntityType(entity.getEntity(), true);

        if(cluster.get(entityType).contains(entity)) {
            return false;
        }
        else {
            cluster.get(entityType).add(entity);
            return true;
        }
    }

    public List<CoreEntity> getEntityCluster(int entityType) {
        return cluster.get(entityType);
    }

    /**
     * Get the single closest entity from a cluster to a tanker
     * @param tankerCoordinate Tanker coordinate
     * @param entityType well/fuel pump/station
     * @return Closest selected entity
     */
    public CoreEntity getClosestEntity(Coordinates tankerCoordinate, int entityType) {
        int ind = -1;
        int argmin = -1;
        int min = Integer.MAX_VALUE;

        for(CoreEntity c : cluster.get(entityType)) {
            ind++;
            int dist = tankerCoordinate.distanceToCoordinate(c.getCoord());
            if(dist < min) {
                argmin = ind;
                min = dist;
            }
        }

        return cluster.get(entityType).get(argmin);
    }

    /**
     * Validate the cluster built
     * @return True if all conditions are satisfied.
     */
    public boolean validateCluster() {
        int[] n = getNumberOfEntities();

        return n[0] >= condition[0] && n[1] >= condition[1] && n[2] >= condition[2];
    }

    /**
     * Get the number of entities in a cluster
     * @return Sized 4 int[] where [number of fuel pump, number of well, number or station, total number of entities]
     */
    public int[] getNumberOfEntities() {
        int[] n = new int[4];
        n[0] = cluster.get(EntityChecker.FUELPUMP).size();
        n[1] = cluster.get(EntityChecker.WELL).size();
        n[2] = cluster.get(EntityChecker.STATION).size();
        n[3] = n[0] + n[1] + n[2];

        return n;
    }

    /**
     * Get the cluster centre as an {@link EntityNode}
     * @return An {@link EntityNode} of the cluster centre
     */
    public EntityNode asEntityNode() {
        return new EntityNode(getEntity(), getCoord(), getFirstSeen());
    }

    /**
     * Checks if the cluster has a task
     * @return True if cluster has task
     */
    public boolean clusterHasTask() {
        for(CoreEntity station : cluster.get(EntityChecker.STATION)) {
            for(CoreEntity mapStation : DemoFleet.mapper.getEntityMap(EntityChecker.STATION)) {
                if(station.equals(mapStation) && mapStation.hasTask()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cluster: ").append(getCoord()).append("\n");
        sb.append("Number of: ").append(Arrays.toString(getNumberOfEntities())).append("\n");
        sb.append("Has tanker move towards: ").append(hasTankerMoveTowards).append("\n");

        return sb.toString();
    }

    public long getLastVisitedCluster() {
        return lastVisitedCluster;
    }

    public void setLastVisitedCluster(long lastVisitedCluster) {
        this.lastVisitedCluster = lastVisitedCluster;
    }

    /**
     * Checks if the cluster is available to be visited
     * @param currentTimestep Current timestep
     * @return True if at least {@link ClusterEntity#VISIT_TIMESTEP_DELAY} has passed since current timestep.
     */
    public boolean enabledCluster(long currentTimestep) {
        return currentTimestep - lastVisitedCluster >= VISIT_TIMESTEP_DELAY;
    }
}
