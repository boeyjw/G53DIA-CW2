package uk.ac.nott.cs.g53dia.multidemo;

import com.sun.istack.internal.Nullable;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;

import java.util.*;

public class ClusterEntity extends CoreEntity {
    private List<CoreEntity> cluster;
    private boolean hasVisited;
    private int numberOfFuelPumps;
    private int numberOfWells;
    private int numberOfStations;
    private int effectiveClusterRadius;

    ClusterEntity(Cell entity, int x, int y, long firstSeen) {
        this(entity, x, y, firstSeen, null, Integer.MIN_VALUE);
    }

    ClusterEntity(Cell entity, int x, int y, long firstSeen, List<CoreEntity> cluster, int effectiveClusterRadius) {
        super(entity, x, y, firstSeen);
        this.cluster = new ArrayList<>();
        this.hasVisited = false;
        this.effectiveClusterRadius = effectiveClusterRadius;


    }

    public void addCluster(List<CoreEntity> cluster) {
        CoreEntity c = cluster.get(0);
        for(Iterator<CoreEntity> iter = cluster.iterator(); iter.hasNext(); ) {
            c = iter.next();
            if(super.getEntity().equals(c.getEntity())) {
                iter.remove();
                break;
            }
        }
        this.cluster.add(c);
        this.cluster.addAll(cluster);
    }

    public List<CoreEntity> getCluster() {
        return cluster;
    }

    public int getEffectiveClusterRadius() {
        return effectiveClusterRadius;
    }

    public int getNumberOfStations() {
        return numberOfStations;
    }

    public int getNumberOfWells() {
        return numberOfWells;
    }

    public int getNumberOfFuelPumps() {
        return numberOfFuelPumps;
    }

    public int getNumberOfEntities() {
        return numberOfFuelPumps + numberOfWells + numberOfStations;
    }
}
