package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Station;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

/**
 * Builds a representation of the Tanker's observed state
 */
public class MapBuilder {
    public static final int ADD = 10;
    public static final int EXIST = 11;
    public static final int REJECTED = -1;

    private Hashtable<Integer, List<CoreEntity>> map;

    public MapBuilder() {
        map = new Hashtable<>();
        map.put(EntityChecker.FUELPUMP, new ArrayList<>());
        map.put(EntityChecker.WELL, new ArrayList<>());
        map.put(EntityChecker.STATION, new ArrayList<>());
        map.put(EntityChecker.TASKEDSTATION, new LinkedList<>());
    }

    public Hashtable<Integer, List<CoreEntity>> getMap() { return map; }

    public List<CoreEntity> getEntityMap(int entityType) { return map.get(entityType); }

    public int addEntity(CoreEntity entity) {
        if(entity.isDirectionalEntity()) {
            return REJECTED;
        }

        int entityType = EntityChecker.getEntityType(entity.getEntity(), true);
        int status = REJECTED;

        if(entityType == EntityChecker.STATION) {
            status = adder(entity, entityType);
            if(entity.hasTask()) {
                status = adder(entity, EntityChecker.TASKEDSTATION);
            }

            return status;
        }
        else {
            return adder(entity, entityType);
        }
    }

    private int adder(CoreEntity entity, int entityType) {
        if(map.get(entityType).isEmpty()) {
            map.get(entityType).add(entity);

            return ADD;
        }
        else {
            if(map.get(entityType).contains(entity)) {
                int ind = map.get(entityType).indexOf(entity);
                if(entity.getBearing() == Calculation.ONTANKER) {
                    map.get(entityType).get(ind).setLastVisitedSeen(entity.getFirstVisited());
                    map.get(entityType).get(ind).incTimesVisited();
                }
                else {
                    map.get(entityType).get(ind).setLastSeen(entity.getFirstSeen());
                }
                map.get(entityType).get(ind).incTimesSeen();
                if(EntityChecker.isStation(entity.getEntity()) && ((Station) entity.getEntity()).getTask() != null) {
                    map.get(entityType).get(ind).setWasteRemaining(((Station) entity.getEntity()).getTask().getWasteRemaining());
                }

                return EXIST;
            }
            else {
                map.get(entityType).add(entity);

                return ADD;
            }
        }
    }

    public boolean removeTaskedStation(CoreEntity taskedStation) {
        return map.get(EntityChecker.TASKEDSTATION).remove(taskedStation);
    }

    public CoreEntity getEntity(CoreEntity entity) {
        int entityType = EntityChecker.getEntityType(entity.getEntity(), true);
        int ind = map.get(entityType).indexOf(entity);

        return ind == -1 ? null : map.get(entityType).get(ind);
    }

    public void setTankerMoveTowardsEntity(CoreEntity entity, int tankerID, int tankerDistanceToEntity) {
        int entityType = EntityChecker.getEntityType(entity.getEntity(), true);
        int ind = map.get(entityType).indexOf(entity);

        if(ind != -1) {
            map.get(entityType).get(ind).setTankerMoveTowardsInformation(tankerID, tankerDistanceToEntity);
        }
    }

    public void unsetTankerMoveTowardsEntity(CoreEntity entity) {
        int entityType = EntityChecker.getEntityType(entity.getEntity(), true);
        int ind = map.get(entityType).indexOf(entity);

        if(ind != -1) {
            map.get(entityType).get(ind).tankerIsHere();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Fuelpump: ");
        for(CoreEntity e : map.get(EntityChecker.FUELPUMP)) {
            sb.append(e.getEntity().getPoint());
            sb.append("\t");
        }
        sb.append("\n");
        sb.append("Well: ");
        for(CoreEntity e : map.get(EntityChecker.WELL)) {
            sb.append(e.getEntity().getPoint());
            sb.append("\t");
        }
        sb.append("\n");
        sb.append("Station: ");
        for(CoreEntity e : map.get(EntityChecker.STATION)) {
            sb.append(e.getEntity().getPoint());
            sb.append("\t");
        }
        sb.append("\n");
        sb.append("Tasked Station: ");
        for(CoreEntity e : map.get(EntityChecker.TASKEDSTATION)) {
            sb.append(e.getEntity().getPoint());
            sb.append("\t");
        }
        sb.append("\n");

        return sb.toString();
    }
}
