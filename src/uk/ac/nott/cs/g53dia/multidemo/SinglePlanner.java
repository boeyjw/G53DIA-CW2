package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

import java.util.*;

/**
 * Single agent planner
 */
public class SinglePlanner extends Planner {
    private static final int EXTEND_VIEW_BY = 10;

    @Override
    public Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap, Hashtable<Integer, List<CoreEntity>> entities,
                                  Deque<CoreEntity> moves, TankerCoordinator tc, long timestep) {
        if (entities.get(EntityChecker.TASKEDSTATION).isEmpty()) {
            return null;
        }

        Hashtable<Integer, List<CoreEntity>> extendedEntities = new Hashtable<>(entities); // Create copy of agent observable space
        // Remove any tasked station that has a tanker visiting it
        for (Iterator<CoreEntity> iter = extendedEntities.get(EntityChecker.TASKEDSTATION).iterator(); iter.hasNext(); ) {
            CoreEntity taskedEntity = iter.next();
            CoreEntity mapStationEntity = map.getEntity(taskedEntity);
            if (mapStationEntity != null && mapStationEntity.getHasTankerMoveTowards()) {
                iter.remove();
            }
        }

        // Begin search algorithm
        EntityNode source = (EntityNode) tc.getEntityUnderTanker();
        int nextMove = EntityChecker.DUMMY;
        int estFuelLevel = tc.getFuelLevel();
        int estWasteLevel = tc.getWasteLevel();
        while (nextMove != Integer.MAX_VALUE) {
            if (allowAddMove(moves, EntityChecker.FUELPUMP, true) &&
                    !super.acceptableFuelLevel(estFuelLevel, getClosestEntityDistanceTo(extendedEntities.get(EntityChecker.FUELPUMP), source))) {
                nextMove = EntityChecker.FUELPUMP;
            } else if (allowAddMove(moves, EntityChecker.WELL, true) && !super.acceptableWasteLevel(estWasteLevel)) {
                nextMove = EntityChecker.WELL;
            } else if (!extendedEntities.get(EntityChecker.TASKEDSTATION).isEmpty()) {
                nextMove = EntityChecker.TASKEDSTATION;
            } else { // Plan completed
                nextMove = Integer.MAX_VALUE;
            }

            if (nextMove != Integer.MAX_VALUE) {
                List<EntityNode> fscorednodes = filterFeasibleNodes(calculateFScore(source, extendedEntities.get(nextMove), DJIKSTRA_ALGORITHM, estWasteLevel), estFuelLevel);
                if (fscorednodes.isEmpty()) {
                    break;
                } else {
                    fscorednodes.removeIf(entityNode -> entityNode.getHasTankerMoveTowards());
                    EntityNode nextEntity = Collections.min(fscorednodes, fscorecompare);
                    switch (nextMove) {
                        case EntityChecker.FUELPUMP:
                            estFuelLevel = 100;
                            break;
                        case EntityChecker.WELL:
                            estFuelLevel -= nextEntity.getFuelConsumption();
                            estWasteLevel = 0;
                            break;
                        case EntityChecker.TASKEDSTATION:
                            estFuelLevel -= nextEntity.getFuelConsumption();
                            int collectedWaste = estWasteLevel + nextEntity.getWasteRemaining() - Tanker.MAX_WASTE;
                            if (collectedWaste <= 0) {
                                estWasteLevel += nextEntity.getWasteRemaining();
                                extendedEntities.get(nextMove).remove(nextEntity);
                            } else {
                                estWasteLevel = Tanker.MAX_WASTE;
                                reduceWasteInStation(extendedEntities, nextEntity, nextEntity.getWasteRemaining() - collectedWaste);
                            }
                            break;
                        default:
                            nextMove = Integer.MAX_VALUE;
                            break;
                    }
                    nextEntity.setParent(source);
                    source = nextEntity;
                    // Next move is enqueued thus directional entity must be dequeue
                    if(!moves.isEmpty() && moves.peekLast().isDirectionalEntity()) {
                        moves.removeLast();
                    }
                    moves.add(nextEntity);
                }
            }
        }

        return moves;
    }

    /**
     * Get an extended view of entities (entities outside of tanker observable space)
     * @param map {@link DemoFleet#mapper}
     * @param entities Tanker observable entities
     * @param tankerCoordinate Tanker status
     * @return Entities + any entities within the extended view radii
     */
    private Hashtable<Integer, List<CoreEntity>> extendedView(Hashtable<Integer, List<CoreEntity>> map, Hashtable<Integer, List<CoreEntity>> entities,
                                                              Coordinates tankerCoordinate) {
        Hashtable<Integer, List<CoreEntity>> extendedEntities = new Hashtable<>(entities);

        for (Integer entityType : extendedEntities.keySet()) {
            for (CoreEntity e : map.get(entityType)) {
                if (!extendedEntities.get(entityType).contains(e) && tankerCoordinate.distanceToCoordinate(e.getCoord()) <= Tanker.VIEW_RANGE + EXTEND_VIEW_BY) {
                    extendedEntities.get(entityType).add(e);
                }
            }
        }

        return extendedEntities;
    }

    /**
     * Reduces waste in station in theory during deliberation
     * @param entities Modify the entities object directly
     * @param taskedStation The tasked station targeted
     * @param wasteCollected Amount of waste managed to be collected by the tanker
     */
    private void reduceWasteInStation(Hashtable<Integer, List<CoreEntity>> entities, CoreEntity taskedStation, int wasteCollected) {
        int ind = entities.get(EntityChecker.TASKEDSTATION).indexOf(taskedStation);
        if (ind == -1) {
            return;
        }

        entities.get(EntityChecker.TASKEDSTATION).get(ind).reduceWasteRemaining(wasteCollected);
    }
}
