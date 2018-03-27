package uk.ac.nott.cs.g53dia.store;

import uk.ac.nott.cs.g53dia.multidemo.CoreEntity;
import uk.ac.nott.cs.g53dia.multidemo.EntityChecker;
import uk.ac.nott.cs.g53dia.multidemo.MapBuilder;
import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

@Deprecated
public class ClusterMapBuilder extends MapBuilder {
    public static int CLUSTER_RADIUS = Tanker.VIEW_RANGE - 5;

    private List<ClusterEntity> clustermap, blacklistCluster;

    ClusterMapBuilder() {
        clustermap = new ArrayList<>();
        blacklistCluster = new ArrayList<>();
    }

    public boolean hasCluster(CoreEntity fuelpump) {
        for(ClusterEntity c : clustermap) {
            if(c.getEntityHash() == fuelpump.getEntityHash()) {
                return true;
            }
        }

        return false;
    }

    public List<ClusterEntity> getClustermap() {
        return clustermap;
    }

    public List<ClusterEntity> getBlacklistCluster() {
        return blacklistCluster;
    }

    public Set<CoreEntity> getCluster(CoreEntity fuelpump) {
        int ind = -1;
        for(ClusterEntity c : clustermap) {
            ind++;
            if(c.getEntityHash() == fuelpump.getEntityHash()) {
                break;
            }
        }

        return ind == -1 ? null : clustermap.get(ind).getCluster();
    }

    public int buildCluster(Hashtable<Integer, List<CoreEntity>> map) {
        int status = REJECTED;
        List<CoreEntity> mergedmap = new ArrayList<>();
        mergedmap.addAll(map.get(EntityChecker.STATION));
        mergedmap.addAll(map.get(EntityChecker.WELL));
        mergedmap.addAll(map.get(EntityChecker.FUELPUMP));

        for(CoreEntity c : map.get(EntityChecker.FUELPUMP)) {
            if(hasCluster(c)) {
                status = updateCluster(mergedmap, c);
            }
            else {
                status = createCluster(mergedmap, c);
            }
        }

        return status;
    }

    private int updateCluster(List<CoreEntity> mergedmap, CoreEntity fuelpump) {
        int status = REJECTED;
        Set<CoreEntity> storedCluster = getCluster(fuelpump);
        List<CoreEntity> entitiesToBeAdded = new ArrayList<>();
        if(storedCluster == null || !mergedmap.containsAll(storedCluster)) {
            return status;
        }

        for(CoreEntity c : mergedmap) {
            if(!storedCluster.add(c)) {
                if(entityWithinClusterRadius(fuelpump, c)) {
                    entitiesToBeAdded.add(c);
                    status = ADD;
                }
            }
        }
        if(entitiesToBeAdded.size() > 0) {
            boolean setStatus = clustermap.get(clustermap.indexOf(fuelpump)).updateCluster(entitiesToBeAdded);
            return setStatus ? status : REJECTED;
        }
        else {
            return EXIST;
        }
    }

    private int createCluster(List<CoreEntity> mergedmap, CoreEntity fuelpump) {
        List<CoreEntity> cluster = new ArrayList<>();
        boolean hasWell = false;
        int numberOfStations = 0;

        for(CoreEntity c : mergedmap) {
            if(entityWithinClusterRadius(fuelpump, c)) {
                cluster.add(c);
                if(EntityChecker.isWell(c.getEntity())) {
                    hasWell = true;
                }
                else if(EntityChecker.isStation(c.getEntity())) {
                    numberOfStations++;
                }
            }
        }

        if(cluster.size() > 0 && hasWell && numberOfStations >= 5) {
            clustermap.add(new ClusterEntity(fuelpump, cluster, CLUSTER_RADIUS));
            return ADD;
        }
        else {
            if(cluster.size() > 0) {
                blacklistCluster.add(new ClusterEntity(fuelpump, cluster, CLUSTER_RADIUS));
            }
            return REJECTED;
        }
    }

    private boolean entityWithinClusterRadius(CoreEntity fuelpump, CoreEntity entity) {
        return !fuelpump.equals(entity) && fuelpump.getCoord().distanceToCoordinate(entity.getCoord()) <= CLUSTER_RADIUS;
    }

    public boolean isBlacklistedCluster(CoreEntity fuelpump) {
        for(ClusterEntity ce : blacklistCluster) {
            if(ce.getEntity().equals(fuelpump)) {
                return true;
            }
        }

        return false;
    }
}
