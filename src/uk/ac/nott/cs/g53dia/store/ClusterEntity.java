package uk.ac.nott.cs.g53dia.store;

import uk.ac.nott.cs.g53dia.multidemo.CoreEntity;
import uk.ac.nott.cs.g53dia.multidemo.EntityChecker;
import uk.ac.nott.cs.g53dia.multilibrary.Cell;

import java.util.*;

@Deprecated
public class ClusterEntity extends CoreEntity {
    private Set<CoreEntity> cluster;
    private boolean hasVisited;
    private int numberOfFuelPumps;
    private int numberOfWells;
    private int numberOfStations;
    private int effectiveClusterRadius;

    ClusterEntity(CoreEntity fuelpump, List<CoreEntity> cluster, int effectiveClusterRadius) {
        this(fuelpump.getEntity(), fuelpump.getCoord().getValue(0), fuelpump.getCoord().getValue(1), fuelpump.getFirstSeen(),
                cluster, effectiveClusterRadius);
    }

    ClusterEntity(Cell entity, int x, int y, long firstSeen, List<CoreEntity> cluster, int effectiveClusterRadius) {
        super(entity, x, y, firstSeen);
        this.cluster = new HashSet<>();
        if(cluster != null) {
            addCluster(cluster);
        }
        this.hasVisited = false;
        this.effectiveClusterRadius = effectiveClusterRadius;
        this.numberOfStations = this.numberOfFuelPumps = this.numberOfWells = 0;
    }

    public boolean addCluster(List<CoreEntity> cluster) {
        CoreEntity c = cluster.get(0);
        for(Iterator<CoreEntity> iter = cluster.iterator(); iter.hasNext(); ) {
            c = iter.next();
            if(super.getEntity().equals(c.getEntity())) {
                iter.remove();
                break;
            }
        }
        this.cluster.add(c);
        boolean setStatus = this.cluster.addAll(cluster);
        countEntities();

        return setStatus;
    }

    public boolean updateCluster(List<CoreEntity> entities) {
        boolean setStatus = this.cluster.addAll(entities);
        this.numberOfStations = this.numberOfFuelPumps = this.numberOfWells = 0;
        countEntities();

        return setStatus;
    }

    private void countEntities() {
        for(CoreEntity c : cluster) {
            switch (EntityChecker.getEntityType(c.getEntity(), true)) {
                case EntityChecker.FUELPUMP:
                    numberOfFuelPumps++;
                    break;
                case EntityChecker.WELL:
                    numberOfWells++;
                    break;
                case EntityChecker.STATION:
                    numberOfStations++;
                    break;
                default:
                    break;
            }
        }
    }

    public Set<CoreEntity> getCluster() {
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
