package uk.ac.nott.cs.g53dia.multidemo;

import uk.ac.nott.cs.g53dia.multilibrary.Tanker;

import java.util.*;

/**
 * All planning essentials
 */
public abstract class Planner {
    protected static final int DJIKSTRA_ALGORITHM = 0;
    protected static final int ASTAR_ALGORITHM = 1;
    protected final FScoreCompare fscorecompare = new FScoreCompare();

    /**
     * Core planning function the tanker makes
     *
     * @param map        All entities observed by the tanker
     * @param clustermap
     * @param tc         Tanker coordinator
     * @param timestep   The current timestep
     * @return A sequence of moves which has been planned
     */
    public abstract Deque<CoreEntity> plan(MapBuilder map, ClusterMapBuilder clustermap,
                                           Hashtable<Integer, List<CoreEntity>> entities, Deque<CoreEntity> moves,
                                           TankerCoordinator tc, long timestep);

    protected boolean acceptableFuelLevel(int estFuelLevel, int distToFuelPump) {
        return estFuelLevel > distToFuelPump + Threshold.REFUEL_ERROR_MARGIN.getThreshold();
    }

    protected boolean acceptableFuelLevel(int estFuelLevel, Coordinates tankerCoordinate, Coordinates fuelpump) {
        return acceptableFuelLevel(estFuelLevel, tankerCoordinate.distanceToCoordinate(fuelpump));
    }

    protected boolean acceptableWasteLevel(int estWasteLevel) {
        return !Threshold.HIGHEST_WASTE.hitThreshold(estWasteLevel);
    }

    /**
     * Get the distance of the closest entity
     * @param desiredEntity List of entity to check against
     * @param current The entity in question
     * @return Distance between the closest entities
     */
    protected int getClosestEntityDistanceTo(List<CoreEntity> desiredEntity, CoreEntity current) {
        if (desiredEntity.isEmpty())
            return Integer.MIN_VALUE;
        else if (desiredEntity.size() == 1)
            return Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(0).getCoord());

        int min = 0;
        int argmin = 0;
        int[] dist = new int[desiredEntity.size()];
        for (int i = 0; i < desiredEntity.size(); i++) {
            dist[i] = Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(i).getCoord());
            if (i == 0)
                min = dist[i];
            else if (dist[i] > min) {
                argmin = i;
                min = dist[i];
            }
        }

        return dist[argmin];
    }

    /**
     * Get the closest entity
     * @param desiredEntity List of entity to check against
     * @param current The entity in question
     * @return The closest entity
     */
    protected CoreEntity getClosestEntityTo(List<CoreEntity> desiredEntity, CoreEntity current) {
        if (desiredEntity.isEmpty())
            return null;
        else if (desiredEntity.size() == 1)
            return desiredEntity.get(0);

        int min = Integer.MAX_VALUE;
        int argmin = -1;
        for (int i = 0; i < desiredEntity.size(); i++) {
            int dist = Calculation.diagonalDistance(current.getCoord(), desiredEntity.get(i).getCoord());
            if (dist < min) {
                min = dist;
                argmin = i;
            }
        }

        return argmin >= 0 ? desiredEntity.get(argmin) : null;
    }

    /**
     * Get total distance in a triangle traversal
     * @param a 1st stop
     * @param b 2nd stop
     * @param c 3rd stop
     * @return Total distance required to travel
     */
    protected int distAtoCviaB(CoreEntity a, CoreEntity b, CoreEntity c) {
        return a.getCoord().distanceToCoordinate(b.getCoord()) + b.getCoord().distanceToCoordinate(c.getCoord());
    }

    /**
     * Verifies if the move can be added to agent intention without duplicates
     * @param moves Agent intentions
     * @param entityType Integer representation of the entity to be added
     * @param isAppend True if enqueue. False if push
     * @return True is move can be safely added
     */
    protected boolean allowAddMove(Deque<CoreEntity> moves, int entityType, boolean isAppend) {
        if (moves.isEmpty()) {
            return true;
        }
        if (EntityChecker.DUMMY == entityType) {
            if(moves.peekFirst().isDirectionalEntity() || moves.peekLast().isDirectionalEntity()) {
                return false;
            }
            return true;
        }
        else {
            if(isAppend) {
                if(moves.peekLast().isDirectionalEntity()) {
                    return false;
                }
                if(EntityChecker.getEntityType(moves.peekLast().getEntity(), true) == entityType) {
                    return false;
                }
                return true;
            }
            else {
                if(moves.peekFirst().isDirectionalEntity()) {
                    return true;
                }
                if(EntityChecker.getEntityType(moves.peekFirst().getEntity(), true) == entityType) {
                    return false;
                }
                return true;
            }
        }
    }

    /**
     * Calculates the F score from one entity to another. Generally used for local planning
     * @param source Source entity
     * @param targets List of entities to consider
     * @param algorithm {@link this#DJIKSTRA_ALGORITHM} or {@link this#ASTAR_ALGORITHM}
     * @param tankerWasteLevel Tanker current or theoretical waste level
     * @return List of entity nodes which F score attached
     */
    protected List<EntityNode> calculateFScore(CoreEntity source, List<CoreEntity> targets, int algorithm, int tankerWasteLevel) {
        List<EntityNode> fscoredNodes = new ArrayList<>(targets.size());
        EntityNode src = (EntityNode) source;

        // safe init
        src.setGscore(0);
        src.setHscore(0);
        src.setWeight(1);
        src.calculateFscore();

        for (CoreEntity target : targets) {
            EntityNode t = (EntityNode) target;
            int dist = source.getCoord().distanceToCoordinate(t.getCoord());
            t.setFuelConsumption(dist);
            t.setGscore(src.getGscore() + dist);
            switch (algorithm) {
                case DJIKSTRA_ALGORITHM:
                    t.setHscore(0);
                    break;
                case ASTAR_ALGORITHM:
                    if(t.hasTask()) {
                        t.setHscore(t.getWasteRemaining());
                        int offset = tankerWasteLevel + t.getWasteRemaining() - Tanker.MAX_WASTE;
                        if(offset <= 0) {
                            t.setWeight(1 + (Math.abs(offset) * 0.001));
                        }
                        else {
//                            double weight = 1 - (offset * 0.001);
//                            if(weight < (double) 0) {
//                                weight = 0;
//                            }
//                            t.setWeight(weight);
                            t.setWeight(0);
                        }
                    }
                    else {
                        t.setHscore(0); // Fuelpumps and wells default to Djikstra
                    }
                    break;
                default:
                    throw new IllegalArgumentException("(Planner) Invalid algorithm selected!");
            }
            t.calculateFscore();
            t.setParent(src);
            fscoredNodes.add(t);
        }

        return fscoredNodes;
    }

    /**
     * Finds a path from source entity to destinaton entity
     * @param source Source entity
     * @param destination Destination entity
     * @param entities Merged/Flattened global map or specific entity type map
     * @param algorithm {@link this#DJIKSTRA_ALGORITHM} or {@link this#ASTAR_ALGORITHM}
     * @param tankerFuelLevel Tanker current fuel level
     * @param tankerWasteLevel Tanker current waste level
     * @return List of moves if path is found. Null otherwise
     */
    protected List<CoreEntity> sourceToDestination(CoreEntity source, CoreEntity destination, List<CoreEntity> entities, int algorithm,
                                                   int tankerFuelLevel, int tankerWasteLevel) {
        List<EntityNode> path = new ArrayList<>();
        List<CoreEntity> nominee = new ArrayList<>(entities);
        EntityNode src = (EntityNode) source;
        EntityNode dest = (EntityNode) destination;
        int estFuelLevel = tankerFuelLevel;
        int estWasteLevel = tankerWasteLevel;

        // Safe init
        src.setGscore(0);
        src.setHscore(0);
        src.setWeight(1);
        src.calculateFscore();

        while(src.getEntityHash() != dest.getEntityHash()) {
            List<EntityNode> fscoredNodes = new ArrayList<>();
            for (CoreEntity target : nominee) {
                EntityNode t = (EntityNode) target;
                int dist = src.getCoord().distanceToCoordinate(t.getCoord());
                t.setFuelConsumption(dist);
                t.setGscore(src.getGscore() + dist);
                switch (algorithm) {
                    case DJIKSTRA_ALGORITHM:
                        t.setHscore(0);
                        break;
                    case ASTAR_ALGORITHM:
                        t.setHscore(t.getCoord().distanceToCoordinate(destination.getCoord()));
                        break;
                    default:
                        throw new IllegalArgumentException("(Planner) Invalid algorithm selected!");
                }
                t.calculateFscore();
                t.setParent(src);
                fscoredNodes.add(t);
            }

            fscoredNodes = filterFeasibleNodes(fscoredNodes, estFuelLevel);
            if(fscoredNodes.isEmpty()) {
                return new ArrayList<>();
            }
            else {
                EntityNode minNode = Collections.min(fscoredNodes, fscorecompare);
                path.add(minNode);
                src = minNode;
                nominee.remove(minNode);

                estFuelLevel -= minNode.getFuelConsumption();
                if(EntityChecker.isFuelPump(minNode.getEntity())) {
                    estFuelLevel = 100;
                }
            }
        }

        return new ArrayList<>(path);
    }

    /**
     * Filter nodes which cannot be reached by tanker current or theoretical fuel level
     * @param fscorednodes F scored evaluated nodes
     * @param tankerFuelLevel Tanker current or theoretical fuel level
     * @return List of entity nodes that can be traversed with specified fuel level
     */
    protected List<EntityNode> filterFeasibleNodes(List<EntityNode> fscorednodes, int tankerFuelLevel) {
        List<EntityNode> feasibleFscoredNodes = new ArrayList<>(fscorednodes.size());

        for(EntityNode node : fscorednodes) {
            if(acceptableFuelLevel(tankerFuelLevel, node.getFuelConsumption())) {
                feasibleFscoredNodes.add(node);
            }
        }

        return feasibleFscoredNodes;
    }
}


class FScoreCompare implements Comparator<EntityNode> {

    @Override
    public int compare(EntityNode o1, EntityNode o2) {
        return Integer.compare(o1.getFscore(), o2.getFscore());
    }
}
