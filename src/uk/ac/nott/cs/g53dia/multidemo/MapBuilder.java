package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Cell;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MapBuilder extends Mapper {
    public static final int ADD = 1;
    public static final int EXIST = 2;
    public static final int REJECTED = -1;

    private Hashtable<Integer, List<CoreEntity>> globalmap;

    public MapBuilder() {
        globalmap = new Hashtable<>();
        globalmap.put(EntityChecker.FUELPUMP, new ArrayList<>());
        globalmap.put(EntityChecker.WELL, new ArrayList<>());
        globalmap.put(EntityChecker.STATION, new ArrayList<>());
    }


    public Hashtable<Integer, List<CoreEntity>> getGlobalmap() {
        return globalmap;
    }

    public int addNonDuplicativePositions(CoreEntity entity) {
        int entityType = EntityChecker.getEntityType(entity.getEntity());

        if(entityType == EntityChecker.EMPTYCELL)
            return REJECTED;
        for(CoreEntity e : globalmap.get(entityType)) {
            if(e.getEntityHash() == entity.getEntityHash())
                return EXIST;
        }
        globalmap.get(entityType).add(entity);
        return ADD;
    }

    public int simpleAdd(CoreEntity entity) {
        int entityType = EntityChecker.getEntityType(entity.getEntity());

        if(entityType == EntityChecker.EMPTYCELL)
            return REJECTED;

        globalmap.get(entityType).add(entity);
        return ADD;
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

        return sb.toString();
    }
}
