package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;

import java.util.*;

/**
 * Builds a representation of the Tanker's observed state
 */
public class MapBuilder {
    public static final int ADD = 1;
    public static final int EXIST = 2;
    public static final int REJECTED = -1;

    private Hashtable<Integer, List<CoreEntity>> globalmap;

    public MapBuilder() {
        globalmap = new Hashtable<>();
        globalmap.put(EntityChecker.FUELPUMP, new ArrayList<>());
        globalmap.put(EntityChecker.WELL, new ArrayList<>());
        globalmap.put(EntityChecker.STATION, new ArrayList<>());
        globalmap.put(EntityChecker.TANKER, new ArrayList<>());
        globalmap.put(EntityChecker.TASKEDSTATION, new LinkedList<>());
    }

    public Hashtable<Integer, List<CoreEntity>> getGlobalmap() { return globalmap; }

    public List<CoreEntity> getEntityMap(int entityType) { return globalmap.get(entityType); }

    /**
     * Adds an entity without duplicate into the representation
     * @param entity Observed entity
     * @return A {@link TwoNumberTuple} containing (index-status, entityType)
     */
    public NumberTuple addNonDuplicativePositions(CoreEntity entity) {
        int entityType = EntityChecker.getEntityType(entity.getEntity(), true);
        NumberTuple t = new TwoNumberTuple(REJECTED, entityType);

        if(EntityChecker.isEmptyCell(entity.getEntity()))
            return t;

        List<CoreEntity> entityListToCompare = globalmap.get(entityType);
        for(int i = 0; i < entityListToCompare.size(); i++) {
            if(entityListToCompare.get(i).getEntityHash() == entity.getEntityHash()) {
                globalmap.get(entityType).get(i).setLastSeen(entity.getFirstSeen());
                globalmap.get(entityType).get(i).setBearing(entity.getBearing());
                // (index-status, entityType)
                t = new TwoNumberTuple(((i + 1) * 10) + EXIST, entityType);
            }
        }
        if(t.getValue(0) == REJECTED) {
            globalmap.get(entityType).add(entity);
            t = new TwoNumberTuple(((globalmap.get(entityType).size() + 1) * 10) + ADD, entityType);
        }
        if(t.getValue(0) != REJECTED && entityType == EntityChecker.STATION && entity.hasTask()) {
            globalmap.get(EntityChecker.TASKEDSTATION).remove(entity);
            globalmap.get(EntityChecker.TASKEDSTATION).add(globalmap.get(EntityChecker.STATION).get(Math.floorDiv(t.getValue(0), 10) - 1));
        }

        return t;
    }

    /**
     * Adds an entity ignoring duplicate into the representation
     * @param entity Observed entity
     * @return A {@link TwoNumberTuple} containing (index-status, entityType)
     */
    public NumberTuple simpleAdd(CoreEntity entity) {
        int entityType = EntityChecker.getEntityType(entity.getEntity(), true);

        if(EntityChecker.isEmptyCell(entity.getEntity()))
            return new TwoNumberTuple(REJECTED, entityType);

        globalmap.get(entityType).add(entity);
        return new TwoNumberTuple(ADD, entityType);
    }

    /**
     * Sets the last seen and/or visited for a Tanker to an entity.
     * If entity is last seen and Tanker is on the entity, both last seen and last visited will be assigned
     * If entity is last seen and Tanker is not on the entity, only last seen will be assigned
     * @param t return status from {@link MapBuilder#simpleAdd(CoreEntity)} or {@link MapBuilder#addNonDuplicativePositions(CoreEntity)}
     * @param timestep The current timestep
     * @param tankerEntityHash The entity hash where the Tanker is on
     */
    public void setLastSeenVisited(NumberTuple t, long timestep, int tankerEntityHash) {
        if(t.getValue(0) % 2 != EXIST) {
            return;
        }

        int ind = Math.floorDiv(t.getValue(0), 10) - 1;
        // Tanker visiting this entity
        if(globalmap.get(t.getValue(1)).get(ind).getEntityHash() == tankerEntityHash) {
            globalmap.get(t.getValue(1)).get(ind).setLastVisitedSeen(timestep);
            globalmap.get(t.getValue(1)).get(ind).incTimesVisited();
        }
        else { // Tanker sees this entity
            globalmap.get(t.getValue(1)).get(ind).setLastSeen(timestep);
        }
        globalmap.get(t.getValue(1)).get(ind).incTimesSeen();
    }

    /**
     * Abstraction to simplify managing of representations
     * @param entity Observed entity
     * @param replace True to allow duplicates in the representation
     * @param timestep Current timestep
     * @param tankerEntityHash Hash of the entity under the Tanker
     */
    public void update(CoreEntity entity, boolean replace, long timestep, int tankerEntityHash) {
        NumberTuple t = replace ? simpleAdd(entity) : addNonDuplicativePositions(entity);
        setLastSeenVisited(t, timestep, tankerEntityHash);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Fuelpump: ");
        for(CoreEntity e : globalmap.get(EntityChecker.FUELPUMP)) {
            sb.append(e.getPosition().toString());
            sb.append("\t");
        }
        sb.append("\n");
        sb.append("Well: ");
        for(CoreEntity e : globalmap.get(EntityChecker.WELL)) {
            sb.append(e.getPosition().toString());
            sb.append("\t");
        }
        sb.append("\n");
        sb.append("Station: ");
        for(CoreEntity e : globalmap.get(EntityChecker.STATION)) {
            sb.append(e.getPosition().toString());
            sb.append("\t");
        }
        sb.append("\n");
        sb.append("Tasked Station: ");
        for(CoreEntity e : globalmap.get(EntityChecker.TASKEDSTATION)) {
            sb.append(e.getPosition().toString());
            sb.append("\t");
        }
        sb.append("\n");
        sb.append("Tanker: ");
        for(CoreEntity e : globalmap.get(EntityChecker.TANKER)) {
            sb.append(e.getPosition().toString());
            sb.append("\t");
        }
        sb.append("\n");

        return sb.toString();
    }
}
